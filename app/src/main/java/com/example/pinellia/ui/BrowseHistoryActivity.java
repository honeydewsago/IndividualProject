package com.example.pinellia.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.databinding.ActivityBrowseHistoryBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.ui.herbDetails.HerbDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class BrowseHistoryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_HISTORY = "historyHerbIds";
    private ActivityBrowseHistoryBinding binding;
    private HerbAdapter herbAdapter;
    private List<String> historyHerbIdsList;
    private List<Herb> historyHerbList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBrowseHistoryBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

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
        retrieveHistoryHerbIds();

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        herbAdapter = new HerbAdapter(historyHerbList);
        binding.recyclerViewHistory.setAdapter(herbAdapter);

        // Handle click event for each herb item
        herbAdapter.setOnItemClickListener(new HerbAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Herb herb) {
                // Launch HerbDetails activity and pass the clicked herb data
                Intent intent = new Intent(BrowseHistoryActivity.this, HerbDetails.class);
                intent.putExtra("herb", herb);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // Retrieve the latest history herb IDs from SharedPreferences
        retrieveHistoryHerbIds();

        // Fetch and update the recycler view
        fetchHistoryHerbs();
    }


    private void fetchHistoryHerbs() {
        // Clear existing history herb list
        historyHerbList.clear();

        // Firebase reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("herbs");

        // Iterate through history herb IDs and fetch corresponding herbs from Firebase
        for (String herbId : historyHerbIdsList) {
            databaseReference.child(herbId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Herb herb = dataSnapshot.getValue(Herb.class);
                    if (herb != null) {
                        historyHerbList.add(herb);
                        herbAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error if needed
                    Log.e("FetchHistory", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }


    private void retrieveHistoryHerbIds() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String historyHerbsJson = preferences.getString(KEY_HISTORY, null);

        historyHerbIdsList.clear();

        if (historyHerbsJson != null) {
            List<String> historyHerbIds = new Gson().fromJson(historyHerbsJson, new TypeToken<List<String>>() {}.getType());
            historyHerbIdsList.addAll(historyHerbIds);
        }

        updateViews();
    }

    private void updateViews() {
        if (historyHerbIdsList.isEmpty()) {
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