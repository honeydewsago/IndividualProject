package com.example.pinellia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class HerbDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_herb_details);

        // Get the data passed from HomeFragment
        Intent intent = getIntent();

        if (intent != null) {
            String herbName = intent.getStringExtra("herbName");

            Toast.makeText(this, ""+herbName, Toast.LENGTH_SHORT).show();
        }
    }
}