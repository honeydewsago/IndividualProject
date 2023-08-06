package com.example.pinellia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.os.Bundle;
import android.view.View;

import com.example.pinellia.databinding.ActivitySearchHerbBinding;

public class SearchHerbActivity extends AppCompatActivity {

    private ActivitySearchHerbBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchHerbBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the title
            setupSearchView(); // Call method to set up the search view
        }

    }

    private void setupSearchView() {
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Set a listener for query text changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle query submission
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle query text changes
                return true;
            }
        });

        // Expand the SearchView by default
        searchView.setIconifiedByDefault(false);

        // Request focus and open the keyboard
        searchView.requestFocus();

        getSupportActionBar().setCustomView(searchView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}