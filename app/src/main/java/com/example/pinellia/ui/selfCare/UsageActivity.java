package com.example.pinellia.ui.selfCare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.pinellia.R;
import com.example.pinellia.databinding.ActivitySymptomBinding;
import com.example.pinellia.databinding.ActivityUsageBinding;

public class UsageActivity extends AppCompatActivity {

    private ActivityUsageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUsageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Usage");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}