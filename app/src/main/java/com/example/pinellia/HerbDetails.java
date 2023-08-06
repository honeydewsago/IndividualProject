package com.example.pinellia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pinellia.databinding.ActivityHerbDetailsBinding;
import com.example.pinellia.databinding.ActivitySearchHerbBinding;
import com.example.pinellia.model.Herb;

public class HerbDetails extends AppCompatActivity {

    private ActivityHerbDetailsBinding binding;
    private Herb mHerb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHerbDetailsBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // Get the data passed from HomeFragment
        Intent intent = getIntent();

        if (intent != null) {
            mHerb = (Herb) getIntent().getSerializableExtra("herb");

            // Update the action bar title to the herb name
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mHerb.getName());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            Toast.makeText(this, ""+mHerb.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}