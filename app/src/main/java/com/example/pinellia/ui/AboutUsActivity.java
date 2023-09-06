package com.example.pinellia.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.pinellia.R;
import com.example.pinellia.databinding.ActivityAboutUsBinding;
import com.example.pinellia.databinding.ActivityHerbDetailsBinding;

public class AboutUsActivity extends AppCompatActivity {

    private ActivityAboutUsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        binding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Update the action bar title to the herb name
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About");
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