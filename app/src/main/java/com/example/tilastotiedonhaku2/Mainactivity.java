package com.example.tilastotiedonhaku2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Mainactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button searchActivityButton = findViewById(R.id.SearchActivityButton);
        Button listInfoActivityButton = findViewById(R.id.ListInfoActivityButton);

        searchActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(Mainactivity.this, Searchactivity.class);
            startActivity(intent);
        });

        listInfoActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(Mainactivity.this, Listinfoactivity.class);
            startActivity(intent);
        });
    }
}