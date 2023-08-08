package com.example.pinellia.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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

            // Display herb data
            binding.textViewName.setText(mHerb.getName());
            binding.textViewNameScientific.setText(mHerb.getNameScientific());
            binding.textViewNameCN.setText(mHerb.getNameCN()+" "+mHerb.getNamePinyin());
            binding.textViewProperty.setText(mHerb.getProperty());
//            binding.textViewMeridianTropism.setText(mHerb.getMeridianTropism());
//            binding.textViewFlavor.setText(mHerb.getFlavor());
            binding.textViewToxicology.setText(mHerb.getToxicology());
            binding.textViewStorage.setText(mHerb.getStorage());
            binding.textViewCharacteristics.setText(mHerb.getCharacteristics());
            binding.textViewPlaceOrigin.setText(mHerb.getPlaceOfOrigin());
            binding.textViewMedicinePart.setText(mHerb.getMedicinePart());
            binding.textViewMethod.setText(mHerb.getMethod());
            binding.textViewEffect.setText(mHerb.getEffect());
            binding.textViewUsage.setText(mHerb.getUsage());
            binding.textViewDosage.setText(mHerb.getDosage());
            binding.textViewProhibition.setText(mHerb.getProhibition());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}