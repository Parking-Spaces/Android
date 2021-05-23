package com.example.parkingspaces;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartApp extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_start);

        Button button_start = findViewById(R.id.button_start);

        button_start.setOnClickListener(v -> {
            Intent openApp = new Intent(StartApp.this, MainActivity.class);
            startActivity(openApp);
        });
    }
}