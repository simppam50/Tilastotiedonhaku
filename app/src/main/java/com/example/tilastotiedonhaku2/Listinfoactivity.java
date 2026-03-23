package com.example.tilastotiedonhaku2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Listinfoactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_info);

        TextView cityText    = findViewById(R.id.CityText);
        TextView yearText    = findViewById(R.id.YearText);
        TextView carInfoText = findViewById(R.id.CarInfoText);
        Button backButton    = findViewById(R.id.BackToMainButton);

        backButton.setOnClickListener(v -> finish());

        Cardatastorage storage = Cardatastorage.getInstance();

        String city = storage.getCity();
        int year    = storage.getYear();
        ArrayList<Cardata> carDataList = storage.getCardata();

        cityText.setText((city != null && !city.isEmpty()) ? city : "–");
        yearText.setText(year > 0 ? String.valueOf(year) : "–");

        if (carDataList != null && !carDataList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int total = 0;
            for (Cardata cd : carDataList) {
                sb.append(cd.getType()).append(": ").append(cd.getAmount()).append("\n");
                total += cd.getAmount();
            }
            sb.append("\nYhteensä: ").append(total);
            carInfoText.setText(sb.toString());
        } else {
            carInfoText.setText("Ei tietoja saatavilla.");
        }
    }
}