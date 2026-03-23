package com.example.tilastotiedonhaku2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Searchactivity extends AppCompatActivity {

    private static final String API_URL =
            "https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px";

    private static final Map<String, String> CAR_TYPE_NAMES = new HashMap<>();
    static {
        CAR_TYPE_NAMES.put("01", "Henkilöautot");
        CAR_TYPE_NAMES.put("02", "Pakettiautot");
        CAR_TYPE_NAMES.put("03", "Kuorma-autot");
        CAR_TYPE_NAMES.put("04", "Linja-autot");
        CAR_TYPE_NAMES.put("05", "Erikoisautot");
    }

    private EditText cityNameEdit;
    private EditText yearEdit;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        cityNameEdit = findViewById(R.id.CityNameEdit);
        yearEdit     = findViewById(R.id.YearEdit);
        statusText   = findViewById(R.id.StatusText);

        Button searchButton          = findViewById(R.id.SearchButton);
        Button listInfoActivityButton = findViewById(R.id.ListInfoActivityButton);
        Button backButton            = findViewById(R.id.BackToMainButton);

        searchButton.setOnClickListener(v -> onSearchButtonClick());

        listInfoActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(Searchactivity.this, Listinfoactivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void onSearchButtonClick() {
        String cityName = cityNameEdit.getText().toString().trim();
        String yearStr  = yearEdit.getText().toString().trim();

        if (cityName.isEmpty()) {
            statusText.setText("Haku epäonnistui, kaupungin nimi ei voi olla tyhjä.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            statusText.setText("Haku epäonnistui, vuosi ei ole kelvollinen numero.");
            return;
        }

        statusText.setText("Haetaan...");
        getData(this, cityName, year);
    }

    public void getData(Context context, String city, int year) {
        new Thread(() -> {
            try {
                // Step 1: fetch metadata
                String metaJson = fetchGet(API_URL);
                JSONObject meta = new JSONObject(metaJson);
                JSONArray variables = meta.getJSONArray("variables");

                String cityCode = null;

                for (int i = 0; i < variables.length(); i++) {
                    JSONObject var = variables.getJSONObject(i);
                    if (var.getString("code").equals("Alue")) {
                        JSONArray valuesArr = var.getJSONArray("values");
                        JSONArray textsArr  = var.getJSONArray("valueTexts");
                        for (int j = 0; j < textsArr.length(); j++) {
                            if (textsArr.getString(j).equalsIgnoreCase(city)) {
                                cityCode = valuesArr.getString(j);
                                break;
                            }
                        }
                        break;
                    }
                }

                if (cityCode == null) {
                    runOnUiThread(() -> statusText.setText(
                            "Haku epäonnistui, kaupunkia ei olemassa tai se on kirjoitettu väärin."));
                    return;
                }

                final String finalCityCode = cityCode;

                // Step 2: build POST query
                JSONObject query = new JSONObject();
                JSONArray queryArray = new JSONArray();

                queryArray.put(buildFilter("Alue",           new String[]{finalCityCode}));
                queryArray.put(buildFilter("Ajoneuvoluokka", new String[]{"01","02","03","04","05"}));
                queryArray.put(buildFilter("Liikennekäyttö", new String[]{"0"}));
                queryArray.put(buildFilter("Vuosi",          new String[]{String.valueOf(year)}));

                query.put("query", queryArray);
                JSONObject responseFormat = new JSONObject();
                responseFormat.put("format", "json-stat2");
                query.put("response", responseFormat);

                // Step 3: POST and parse
                String resultJson = fetchPost(API_URL, query.toString());
                JSONObject result = new JSONObject(resultJson);

                JSONObject dimension      = result.getJSONObject("dimension");
                JSONObject ajoneuvoluokka = dimension.getJSONObject("Ajoneuvoluokka");
                JSONObject indexObj       = ajoneuvoluokka.getJSONObject("category")
                        .getJSONObject("index");
                JSONArray valuesArray     = result.getJSONArray("value");

                Cardatastorage storage = Cardatastorage.getInstance();
                storage.clearData();
                storage.setCity(city);
                storage.setYear(year);

                String[] codes = {"01", "02", "03", "04", "05"};
                for (String code : codes) {
                    String typeName = CAR_TYPE_NAMES.getOrDefault(code, code);
                    int amount = 0;
                    if (indexObj.has(code)) {
                        int idx = indexObj.getInt(code);
                        if (!valuesArray.isNull(idx)) {
                            amount = valuesArray.getInt(idx);
                        }
                    }
                    storage.addCarData(new Cardata(typeName, amount));
                }

                runOnUiThread(() -> statusText.setText("Haku onnistui"));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> statusText.setText("Haku epäonnistui: " + e.getMessage()));
            }
        }).start();
    }

    private JSONObject buildFilter(String code, String[] values) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("code", code);
        JSONObject selection = new JSONObject();
        selection.put("filter", "item");
        JSONArray arr = new JSONArray();
        for (String v : values) arr.put(v);
        selection.put("values", arr);
        obj.put("selection", selection);
        return obj;
    }

    private String fetchGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private String fetchPost(String urlString, String body) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}