package com.example.pinellia.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinellia.R;
import com.example.pinellia.adapter.MeridianTropismAdapter;
import com.example.pinellia.databinding.ActivityHerbDetailsBinding;
import com.example.pinellia.databinding.ActivitySearchHerbBinding;
import com.example.pinellia.model.Herb;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HerbDetails extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_FAVORITE_HERBS = "favoriteHerbIds";
    private static final String KEY_HISTORY = "historyHerbIds";
    private ActivityHerbDetailsBinding binding;
    private Herb mHerb;
    private boolean isFavorite = false;
    private String herbId;

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

            // Initialize herbId with the ID of the current herb
            herbId = mHerb.getId();

            saveBrowseHistory(herbId); // Save the herbId to history data

            binding.buttonFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isFavorite = !isFavorite;
                    updateFavoriteButtonIcon();
                    saveFavoriteList();

                    // Display Toast message
                    if (isFavorite) {
                        Toast.makeText(HerbDetails.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HerbDetails.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Load the image from Firebase Storage using Glide
            String imageLink = mHerb.getImageLink();

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache image
                    .placeholder(R.drawable.bg_light_green_gradient) // Placeholder while loading
                    .error(R.drawable.bg_light_green_gradient); // Error placeholder

            Glide.with(this)
                    .load(imageLink)
                    .apply(requestOptions)
                    .into(binding.imageViewHerb);

            // Display herb data
            binding.textViewNameScientific.setText(mHerb.getNameScientific());
            binding.textViewNameCN.setText(mHerb.getNameCN()+" "+mHerb.getNamePinyin());

            // Update the color of the box based on herb property
            Drawable backgroundDrawable = getBackgroundDrawableForProperty(mHerb.getProperty());
            binding.herbPropertyLayout.setBackground(backgroundDrawable);

            // Initialize RecyclerView with FlexboxLayoutManager to display meridian tropism
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
            layoutManager.setFlexWrap(FlexWrap.WRAP); // Enable item wrapping
            layoutManager.setJustifyContent(JustifyContent.FLEX_START); // Align items to the start of the container

            binding.recyclerViewMeridianTropism.setLayoutManager(layoutManager);

            // Create an adapter and set it to the RecyclerView
            MeridianTropismAdapter adapter = new MeridianTropismAdapter(mHerb.getMeridianTropism());
            binding.recyclerViewMeridianTropism.setAdapter(adapter);


            // Initialize RecyclerView with new FlexboxLayoutManager to display herb flavour
            FlexboxLayoutManager layoutManager2 = new FlexboxLayoutManager(this);
            layoutManager2.setFlexWrap(FlexWrap.WRAP); // Enable item wrapping
            layoutManager2.setJustifyContent(JustifyContent.FLEX_START); // Align items to the start of the container

            binding.recyclerViewFlavour.setLayoutManager(layoutManager2);

            // Use meridian tropism adapter to display flavour and set it to the RecyclerView
            MeridianTropismAdapter flavourAdapter = new MeridianTropismAdapter(mHerb.getFlavour());
            binding.recyclerViewFlavour.setAdapter(flavourAdapter);

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

            // Update isFavorite based on saved favorite herb IDs
            updateFavoriteButtonState();
            updateFavoriteButtonIcon();
        }
    }

    private void saveBrowseHistory(String herbId) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String historyHerbsJson = preferences.getString(KEY_HISTORY, null);

        List<String> historyHerbIds = new ArrayList<>();

        if (historyHerbsJson != null) {
            historyHerbIds = new Gson().fromJson(historyHerbsJson, new TypeToken<List<String>>() {}.getType());
        }

        historyHerbIds.add(0, herbId); // Add at the beginning to maintain order

        String updatedHistoryHerbsJson = new Gson().toJson(historyHerbIds);
        preferences.edit().putString(KEY_HISTORY, updatedHistoryHerbsJson).apply();
    }


    private void saveFavoriteList() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String favoriteHerbsJson = preferences.getString(KEY_FAVORITE_HERBS, null);

        List<String> favoriteHerbIds = new ArrayList<>();

        if (favoriteHerbsJson != null) {
            favoriteHerbIds = new Gson().fromJson(favoriteHerbsJson, new TypeToken<List<String>>() {}.getType());
        }

        if (isFavorite) {
            if (!favoriteHerbIds.contains(herbId)) {
                favoriteHerbIds.add(0, herbId); // Add at the beginning to maintain order
            }
        } else {
            favoriteHerbIds.remove(herbId);
        }

        String updatedFavoriteHerbsJson = new Gson().toJson(favoriteHerbIds);
        preferences.edit().putString(KEY_FAVORITE_HERBS, updatedFavoriteHerbsJson).apply();
    }

    private void updateFavoriteButtonState() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String favoriteHerbsJson = preferences.getString(KEY_FAVORITE_HERBS, null);

        List<String> favoriteHerbIds = new ArrayList<>();

        if (favoriteHerbsJson != null) {
            favoriteHerbIds.addAll(new Gson().fromJson(favoriteHerbsJson, new TypeToken<List<String>>() {}.getType()));
        }

        isFavorite = favoriteHerbIds.contains(herbId);
    }


    private void updateFavoriteButtonIcon() {
        ImageButton buttonFavorite = findViewById(R.id.buttonFavorite);
        int iconRes = isFavorite ? R.drawable.baseline_star_50 : R.drawable.baseline_star_border_50;
        buttonFavorite.setImageResource(iconRes);
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