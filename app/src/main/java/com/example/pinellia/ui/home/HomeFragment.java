package com.example.pinellia.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinellia.HerbDetails;
import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.databinding.FragmentHomeBinding;
import com.example.pinellia.model.Herb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HerbAdapter herbAdapter;
    private List<Herb> herbList;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.recyclerViewHerbs.setLayoutManager(new LinearLayoutManager(getActivity()));
        herbList = new ArrayList<>();
        herbAdapter = new HerbAdapter(herbList);
        binding.recyclerViewHerbs.setAdapter(herbAdapter);

        homeViewModel.getHerbData().observe(getViewLifecycleOwner(), herbs -> {
            // Update the RecyclerView when data changes
            herbList.clear();
            herbList.addAll(herbs);
            herbAdapter.notifyDataSetChanged();
        });

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

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            // Handle the error message
            Toast.makeText(getActivity(), "Failed to retrieve data: " + errorMessage, Toast.LENGTH_SHORT).show();
        });

        // Set up the search bar
        setHasOptionsMenu(true); // Add this line to indicate that the fragment has its own menu items.


        // Set up the search bar
//        SearchView searchView = binding.searchView;
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // Filter the herb list based on the search query
//                filterHerbs(newText);
//                return true;
//            }
//        });

        return root;
    }

    // Helper method to filter the herb list based on the search query
//    private void filterHerbs(String query) {
//        List<Herb> filteredHerbs = new ArrayList<>();
//
//        for (Herb herb : herbList) {
//            if (herb.getName().toLowerCase().contains(query.toLowerCase())) {
//                filteredHerbs.add(herb);
//            }
//        }
//
//        // Update the adapter with the filtered list
//        herbAdapter.setFilter(filteredHerbs);
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.recyclerViewHerbs.setAdapter(null);
        herbAdapter = null;
    }

}