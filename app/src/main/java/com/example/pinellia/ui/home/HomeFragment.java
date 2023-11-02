package com.example.pinellia.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinellia.ui.HerbDetailsActivity;
import com.example.pinellia.R;
import com.example.pinellia.ui.SearchHerbActivity;
import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.databinding.FragmentHomeBinding;
import com.example.pinellia.model.Herb;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HerbAdapter herbAdapter;
    private List<Herb> herbList;
    private HomeViewModel homeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the search bar
        setHasOptionsMenu(true);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Hide the RecyclerView and show the TextView initially
        binding.recyclerViewHerbs.setVisibility(View.GONE);
        binding.textViewNoHerbs.setVisibility(View.VISIBLE);

        // Initialize the RecyclerView and data list
        binding.recyclerViewHerbs.setLayoutManager(new LinearLayoutManager(getActivity()));
        herbList = new ArrayList<>();
        herbAdapter = new HerbAdapter(herbList);
        binding.recyclerViewHerbs.setAdapter(herbAdapter);

        // Observe changes in the herb data
        homeViewModel.getHerbData().observe(getViewLifecycleOwner(), herbs -> {
            // Update the RecyclerView when data changes
            herbAdapter.updateData(herbs);
            updateViews(herbs.isEmpty());
        });

        // Handle click event for each herb item
        herbAdapter.setOnItemClickListener(new HerbAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Herb herb) {
                // Launch HerbDetailsActivity activity and pass the clicked herb data
                Intent intent = new Intent(getActivity(), HerbDetailsActivity.class);
                intent.putExtra("herb", herb);
                startActivity(intent);
            }
        });

        // Observe and handle error messages
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            // Handle the error message
            Toast.makeText(getActivity(), "Failed to retrieve data: " + errorMessage, Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    private void updateViews(boolean noHerbs) {
        if (noHerbs) {
            binding.textViewNoHerbs.setVisibility(View.VISIBLE);
            binding.recyclerViewHerbs.setVisibility(View.GONE);
        } else {
            binding.textViewNoHerbs.setVisibility(View.GONE);
            binding.recyclerViewHerbs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu_home_fragment.xml file into the menu
        inflater.inflate(R.menu.fragment_home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item clicks
        if (item.getItemId() == R.id.action_search) {
            // Launch SearchHerbActivity
            Intent intent = new Intent(getActivity(), SearchHerbActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.recyclerViewHerbs.setAdapter(null);
        herbAdapter = null;
    }
}