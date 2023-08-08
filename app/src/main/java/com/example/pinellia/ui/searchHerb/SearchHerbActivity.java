package com.example.pinellia.ui.searchHerb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.pinellia.databinding.ActivitySearchHerbBinding;
import com.example.pinellia.ui.HerbDetails;
import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.model.Herb;

import java.util.ArrayList;
import java.util.List;

public class SearchHerbActivity extends AppCompatActivity {

    private ActivitySearchHerbBinding binding;
    private SearchHerbViewModel searchHerbViewModel;
    private HerbAdapter searchResultsAdapter;
    private List<Herb> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchHerbBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            setupSearchView(); // Call method to set up the search view in action bar
        }

        // Initialize the ViewModel
        searchHerbViewModel = new ViewModelProvider(this).get(SearchHerbViewModel.class);

        binding.recyclerViewSearchHerbs.setLayoutManager(new LinearLayoutManager(this));

        // Reuse the HerbAdapter for search results
        searchResultsAdapter = new HerbAdapter(searchResults);
        binding.recyclerViewSearchHerbs.setAdapter(searchResultsAdapter);

        // Set item click listener for search results
        searchResultsAdapter.setOnItemClickListener(new HerbAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Herb herb) {
                // Handle item click in search results to launch HerbDetails activity
                Intent intent = new Intent(SearchHerbActivity.this, HerbDetails.class);
                intent.putExtra("herb", herb);
                startActivity(intent);
            }
        });

        // Observe changes in search results LiveData
        searchHerbViewModel.getSearchResultsLiveData().observe(this, new Observer<List<Herb>>() {
            @Override
            public void onChanged(List<Herb> herbs) {
                // Update the UI with the new search results
                searchResults.clear();
                searchResults.addAll(herbs);
                searchResultsAdapter.notifyDataSetChanged();
            }
        });

        searchHerbViewModel.getErrorMessage().observe(this, errorMessage -> {
            // Handle the error message
            Toast.makeText(this, "Failed to retrieve data: " + errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearchView() {
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Set a listener for query text changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle query submission
                // Close the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update search results based on query text change
                searchHerbViewModel.performSearch(newText);
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