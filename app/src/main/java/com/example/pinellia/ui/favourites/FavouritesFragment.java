package com.example.pinellia.ui.favourites;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.example.pinellia.R;
import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.databinding.FragmentFavouritesBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.ui.AboutUsActivity;
import com.example.pinellia.ui.BrowseHistoryActivity;
import com.example.pinellia.ui.HerbDetailsActivity;
import com.example.pinellia.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_FAVORITE_HERBS = "favoriteHerbIds";
    private FragmentFavouritesBinding binding;
    private FavouritesViewModel favouritesViewModel;
    private List<String> favoriteHerbIdsList;
    private HerbAdapter herbAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the search bar
        setHasOptionsMenu(true);

        favouritesViewModel = new ViewModelProvider(this).get(FavouritesViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavouritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Hide the RecyclerView and show the TextView initially
        binding.recyclerViewFavourites.setVisibility(View.GONE);
        binding.textViewNoFavourites.setVisibility(View.VISIBLE);

        favoriteHerbIdsList = new ArrayList<>();

        // Retrieve favorite herb IDs from SharedPreferences and populate the list
        favouritesViewModel.retrieveFavoriteHerbIds(requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), KEY_FAVORITE_HERBS);

        // Observe the LiveData from ViewModel to retrieve favorite herb IDs
        favouritesViewModel.getFavoriteHerbIdsLiveData().observe(getViewLifecycleOwner(), favoriteHerbIds -> {
            // Handle the retrieved favorite herb IDs
            favoriteHerbIdsList.clear();
            favoriteHerbIdsList.addAll(favoriteHerbIds);

        });

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewFavourites.setLayoutManager(new LinearLayoutManager(getActivity()));
        herbAdapter = new HerbAdapter(new ArrayList<>());
        binding.recyclerViewFavourites.setAdapter(herbAdapter);

        // Observe the LiveData from ViewModel
        favouritesViewModel.getFavoriteHerbsLiveData().observe(getViewLifecycleOwner(), favoriteHerbs -> {
            herbAdapter.updateData(favoriteHerbs);
            updateViews(favoriteHerbs.isEmpty());
        });

        favouritesViewModel.getNoFavoritesLiveData().observe(getViewLifecycleOwner(), this::updateViews);

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

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Retrieve the latest favorite herb IDs from SharedPreferences
        favouritesViewModel.retrieveFavoriteHerbIds(requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), KEY_FAVORITE_HERBS);

        // Fetch and update the recycler view
        favouritesViewModel.fetchFavoriteHerbs(favoriteHerbIdsList);
    }

    private void updateViews(boolean noFavorites) {
        if (noFavorites) {
            binding.textViewNoFavourites.setVisibility(View.VISIBLE);
            binding.recyclerViewFavourites.setVisibility(View.GONE);
        } else {
            binding.textViewNoFavourites.setVisibility(View.GONE);
            binding.recyclerViewFavourites.setVisibility(View.VISIBLE);
        }
    }

    private void showAlertDialog() {
        // Create and show the AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("User Acknowledgement")
                .setMessage("This app is designed for informational purposes and as a supplement to your wellness journey. While it provides insights into Traditional Chinese Medicine (TCM) and herbal remedies, it is not a substitute for professional medical advice, diagnosis, or treatment. Always consult with a qualified healthcare provider for any health-related concerns.")
                .setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the "OK" button click
                        dialog.dismiss(); // Dismiss the dialog
                    }
                })
                .setCancelable(false) // Make the dialog non-cancelable
                .create();

        alertDialog.show(); // Show the AlertDialog
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
            // Launch BrowseHistoryActivity
            Intent intent = new Intent(requireContext(), BrowseHistoryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_acknowledge) {
            // Handle Settings click
            showAlertDialog();
            return true;
        }else if (id == R.id.action_settings) {
            // Launch SettingsActivity
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            // Launch AboutUsActivity
            Intent intent = new Intent(requireContext(), AboutUsActivity.class);
            startActivity(intent);
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