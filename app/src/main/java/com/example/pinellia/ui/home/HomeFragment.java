package com.example.pinellia.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinellia.HerbDetails;
import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.databinding.FragmentHomeBinding;
import com.example.pinellia.model.Herb;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HerbAdapter herbAdapter;
    private List<Herb> herbList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}