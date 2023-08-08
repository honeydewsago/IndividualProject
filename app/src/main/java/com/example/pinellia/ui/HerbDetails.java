package com.example.pinellia.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pinellia.R;
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
            binding.textViewNameScientific.setText(mHerb.getNameScientific());
            binding.textViewNameCN.setText(mHerb.getNameCN()+" "+mHerb.getNamePinyin());

            // Update the color of the box based on the property
            Drawable backgroundDrawable = getBackgroundDrawableForProperty(mHerb.getProperty());
            binding.herbPropertyLayout.setBackground(backgroundDrawable);
            binding.textViewProperty.setText(mHerb.getProperty());

            if (mHerb.getToxicology().isEmpty()) {
                binding.textViewToxicology.setText(R.string.not_identified);
            }
            else {
                binding.textViewToxicology.setText(mHerb.getToxicology());
            }

            binding.textViewStorage.setText(mHerb.getStorage());
            binding.textViewCharacteristics.setText(mHerb.getCharacteristics());
            binding.textViewPlaceOrigin.setText(mHerb.getPlaceOfOrigin());
            binding.textViewMedicinePart.setText(mHerb.getMedicinePart());
            binding.textViewMethod.setText(mHerb.getMethod());
            binding.textViewEffect.setText(mHerb.getEffect());
            binding.textViewUsage.setText(mHerb.getUsage());
            binding.textViewDosage.setText(mHerb.getDosage());
            binding.textViewProhibition.setText(mHerb.getProhibition());

//            binding.textViewMeridianTropism.setText(mHerb.getMeridianTropism());
//            binding.textViewFlavor.setText(mHerb.getFlavor());
        }
    }

    private Drawable getBackgroundDrawableForProperty(String property) {
        int drawableResId = R.drawable.rounded_green_box; // Default drawable resource

        if (property.contains("Hot")) {
            drawableResId = R.drawable.rounded_red_box;
        } else if (property.contains("Warm")) {
            drawableResId = R.drawable.rounded_orange_box;
        } else if (property.contains("Cool")) {
            drawableResId = R.drawable.rounded_light_blue_box;
        } else if (property.contains("Cold")) {
            drawableResId = R.drawable.rounded_blue_box;
        }

        return ContextCompat.getDrawable(this, drawableResId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}