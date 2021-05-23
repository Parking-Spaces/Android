package com.example.parkingspaces;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Initial Page
public class MainActivity extends AppCompatActivity {

    Button menu;
    Button slot1,slot2,slot3,slot4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
