package com.example.pinellia.ui.herbDetails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinellia.R;
import com.example.pinellia.adapter.MeridianTropismAdapter;
import com.example.pinellia.databinding.ActivityHerbDetailsBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.ui.favourites.FavouritesViewModel;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class HerbDetails extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_FAVORITE_HERBS = "favoriteHerbIds";
    private static final String KEY_HISTORY = "historyHerbIds";
    private ActivityHerbDetailsBinding binding;
    private HerbDetailsViewModel herbDetailsViewModel;
    private Herb mHerb;
    private boolean isFavorite = false;
    private String herbId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHerbDetailsBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        herbDetailsViewModel = new ViewModelProvider(this).get(HerbDetailsViewModel.class);

        // Get the data passed from HomeFragment
        Intent intent = getIntent();

        if (intent != null) {
            mHerb = (Herb) getIntent().getSerializableExtra("herb");
        }

        // Update the action bar title to the herb name
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mHerb.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize herbId with the ID of the current herb
        herbId = mHerb.getId();

        herbDetailsViewModel.saveBrowseHistory(herbId, getSharedPreferences(PREFS_NAME, MODE_PRIVATE), KEY_HISTORY); // Save the herbId to history data

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
        binding.textViewStorage.setText(mHerb.getStorage());
        binding.textViewCharacteristics.setText(mHerb.getCharacteristics());
        binding.textViewPlaceOrigin.setText(mHerb.getPlaceOfOrigin());
        binding.textViewMedicinePart.setText(mHerb.getMedicinePart());
        binding.textViewMethod.setText(mHerb.getMethod());
        binding.textViewEffect.setText(mHerb.getEffect());
        binding.textViewUsage.setText(mHerb.getUsage());
        binding.textViewDosage.setText(mHerb.getDosage());
        binding.textViewProhibition.setText(mHerb.getProhibition());

        // Display different box color for different herb property
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

        // Create another meridian tropism adapter to display flavour and set it to the RecyclerView
        MeridianTropismAdapter flavourAdapter = new MeridianTropismAdapter(mHerb.getFlavour());
        binding.recyclerViewFlavour.setAdapter(flavourAdapter);

        binding.textViewProperty.setText(mHerb.getProperty());

        // Display herb toxicology
        if (mHerb.getToxicology().isEmpty()) {
            binding.textViewToxicology.setText(R.string.not_identified);
        }
        else {
            binding.textViewToxicology.setText(mHerb.getToxicology());
        }

        // Update isFavorite based on saved favorite herb IDs
        herbDetailsViewModel.updateFavoriteButtonState(herbId, getSharedPreferences(PREFS_NAME, MODE_PRIVATE), KEY_FAVORITE_HERBS);

        // Observe the isFavorite LiveData
        herbDetailsViewModel.getIsFavoriteLiveData().observe(this, isFavorite -> {
            this.isFavorite = isFavorite;
            herbDetailsViewModel.saveFavoriteList(herbId, isFavorite, getSharedPreferences(PREFS_NAME, MODE_PRIVATE), KEY_FAVORITE_HERBS);
        });

        herbDetailsViewModel.getToastMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateFavoriteMenuItemIcon(MenuItem menuItem) {
        int iconRes = isFavorite ? R.drawable.baseline_star_50 : R.drawable.baseline_star_border_50;
        menuItem.setIcon(iconRes);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_herb_details_menu, menu);
        MenuItem favoriteMenuItem = menu.findItem(R.id.action_favourite);
        updateFavoriteMenuItemIcon(favoriteMenuItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favourite) {
            herbDetailsViewModel.toggleFavorite(isFavorite);
            invalidateOptionsMenu(); // Refresh the action bar menu
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}