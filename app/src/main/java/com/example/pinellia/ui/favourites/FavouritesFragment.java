package com.example.pinellia.ui.favourites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinellia.R;
import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.databinding.FragmentFavouritesBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.ui.HerbDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavouritesFragment extends Fragment {

    private FragmentFavouritesBinding binding;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_FAVORITE_HERBS = "favoriteHerbIds";
    private List<String> favoriteHerbIdsList;
    private List<Herb> favoriteHerbList;
    private HerbAdapter herbAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the search bar
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FavouritesViewModel favouritesViewModel = new ViewModelProvider(this).get(FavouritesViewModel.class);

        binding = FragmentFavouritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        favoriteHerbIdsList = new ArrayList<>();
        favoriteHerbList = new ArrayList<>();

        // Retrieve favorite herb IDs from SharedPreferences and populate the list
        retrieveFavoriteHerbIds();

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewFavourites.setLayoutManager(new LinearLayoutManager(getActivity()));
        herbAdapter = new HerbAdapter(favoriteHerbList);
        binding.recyclerViewFavourites.setAdapter(herbAdapter);

        // Handle click event for each herb item
        herbAdapter.setOnItemClickListener(new HerbAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Herb herb) {
                // Launch HerbDetails activity and pass the clicked herb data
                Intent intent = new Intent(getActivity(), HerbDetails.class);
                intent.putExtra("herb", herb);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Retrieve the latest favorite herb IDs from SharedPreferences
        retrieveFavoriteHerbIds();

        // Fetch and update the recycler view
        fetchAndUpdateFavoriteHerbs();
    }

    private void fetchAndUpdateFavoriteHerbs() {
        // Clear existing favorite herb list
        favoriteHerbList.clear();

        // Firebase reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("herbs");

        // Iterate through favorite herb IDs and fetch corresponding herbs from Firebase
        for (String herbId : favoriteHerbIdsList) {
            databaseReference.child(herbId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Herb herb = dataSnapshot.getValue(Herb.class);
                    if (herb != null) {
                        favoriteHerbList.add(herb);
                        herbAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error if needed
                    Log.e("FetchAndUpdate", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }


    private void retrieveFavoriteHerbIds() {
        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String favoriteHerbsJson = preferences.getString(KEY_FAVORITE_HERBS, null);

        favoriteHerbIdsList.clear();

        if (favoriteHerbsJson != null) {
            List<String> favoriteHerbIds = new Gson().fromJson(favoriteHerbsJson, new TypeToken<List<String>>() {}.getType());
            favoriteHerbIdsList.addAll(favoriteHerbIds);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_favourites_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_browsing_history) {
            // Handle Browsing History click
            Toast.makeText(requireContext(), "Browsing History clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_settings) {
            // Handle Settings click
            Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_about) {
            // Handle About click
            Toast.makeText(requireContext(), "About clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}