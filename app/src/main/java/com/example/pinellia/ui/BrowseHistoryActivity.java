package com.example.pinellia.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.databinding.ActivityBrowseHistoryBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.viewmodel.BrowseHistoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class BrowseHistoryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_HISTORY = "historyHerbIds";
    private ActivityBrowseHistoryBinding binding;
    private BrowseHistoryViewModel historyViewModel;
    private HerbAdapter herbAdapter;
    private List<String> historyHerbIdsList;
    private List<Herb> historyHerbList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBrowseHistoryBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        historyViewModel = new ViewModelProvider(this).get(BrowseHistoryViewModel.class);

        // Enable action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Browsing History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Hide the RecyclerView and show the TextView initially
        binding.recyclerViewHistory.setVisibility(View.GONE);
        binding.textViewNoHistory.setVisibility(View.VISIBLE);

        historyHerbIdsList = new ArrayList<>();
        historyHerbList = new ArrayList<>();

        // Retrieve history herb IDs from SharedPreferences and populate the list
        historyViewModel.retrieveHistoryHerbIds(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), KEY_HISTORY);

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        herbAdapter = new HerbAdapter(historyHerbList);
        binding.recyclerViewHistory.setAdapter(herbAdapter);

        // Handle click event for each herb item
        herbAdapter.setOnItemClickListener(new HerbAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Herb herb) {
                // Launch HerbDetailsActivity activity and pass the clicked herb data
                Intent intent = new Intent(BrowseHistoryActivity.this, HerbDetailsActivity.class);
                intent.putExtra("herb", herb);
                startActivity(intent);
            }
        });

        // Observe the LiveData from ViewModel to retrieve history herb IDs
        historyViewModel.getHistoryHerbIdsLiveData().observe(this, historyHerbIds -> {
            // Handle the retrieved history herb IDs here
            historyHerbIdsList.clear();
            historyHerbIdsList.addAll(historyHerbIds);
        });

        // Observe the LiveData from ViewModel to update the UI with fetched history herbs
        historyViewModel.getHistoryHerbListLiveData().observe(this, historyHerbs -> {
            // Handle the fetched history herb list here
            herbAdapter.updateData(historyHerbs);
            updateViews(historyHerbs.isEmpty());
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // Retrieve the latest history herb IDs from SharedPreferences
        historyViewModel.retrieveHistoryHerbIds(getSharedPreferences(PREFS_NAME, MODE_PRIVATE), KEY_HISTORY);

        // Fetch and update the recycler view
        historyViewModel.fetchHistoryHerbs(historyHerbIdsList);
    }

    private void updateViews(boolean noHistory) {
        if (noHistory) {
            binding.textViewNoHistory.setVisibility(View.VISIBLE);
            binding.recyclerViewHistory.setVisibility(View.GONE);
        } else {
            binding.textViewNoHistory.setVisibility(View.GONE);
            binding.recyclerViewHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}