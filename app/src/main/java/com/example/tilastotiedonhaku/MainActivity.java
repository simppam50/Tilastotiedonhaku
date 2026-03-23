package com.example.tilastotiedonhaku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button searchActivityButton = findViewById(R.id.SearchActivityButton);
        Button listInfoActivityButton = findViewById(R.id.ListInfoActivityButton);

        searchActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        listInfoActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListInfoActivity.class);
            startActivity(intent);
        });
    }
}