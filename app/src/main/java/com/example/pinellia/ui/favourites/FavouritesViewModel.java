package com.example.pinellia.ui.favourites;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pinellia.model.Herb;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FavouritesViewModel extends ViewModel {

    private final MutableLiveData<List<String>> favoriteHerbsIdsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Herb>> favoriteHerbsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> noFavoritesLiveData = new MutableLiveData<>();

    // LiveData to observe the list of favorite herbs
    public MutableLiveData<List<Herb>> getFavoriteHerbsLiveData() {
        return favoriteHerbsLiveData;
    }

    // LiveData to observe whether there are no favorite herbs
    public MutableLiveData<Boolean> getNoFavoritesLiveData() {
        return noFavoritesLiveData;
    }

    // LiveData to observe the list of favorite herb IDs
    public MutableLiveData<List<String>> getFavoriteHerbIdsLiveData() {
        return favoriteHerbsIdsLiveData;
    }

    // Method to retrieve favorite herb IDs from SharedPreferences
    public void retrieveFavoriteHerbIds(SharedPreferences preferences, String keyFavoriteHerbs) {
        List<String> favoriteHerbIdsList = new ArrayList<>();

        // Retrieve a JSON string containing favorite herb IDs from SharedPreferences
        String favoriteHerbsJson = preferences.getString(keyFavoriteHerbs, null);

        if (favoriteHerbsJson != null) {
            // Parse the JSON string into a list of favorite herb IDs
            List<String> favoriteHerbIds = new Gson().fromJson(favoriteHerbsJson, new TypeToken<List<String>>() {}.getType());
            favoriteHerbIdsList.addAll(favoriteHerbIds);
        }

        // Update the LiveData with the list of favorite herb IDs
        favoriteHerbsIdsLiveData.setValue(favoriteHerbIdsList);
    }

    // Method to fetch favorite herbs from Firebase Database
    public void fetchFavoriteHerbs(List<String> favoriteHerbIdsList) {
        List<Herb> favoriteHerbList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("herbs");

        for (String herbId : favoriteHerbIdsList) {
            databaseReference.child(herbId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Herb herb = dataSnapshot.getValue(Herb.class);

                    if (herb != null) {
                        // Add the herb to the list of favorite herbs
                        favoriteHerbList.add(herb);

                        // Update the LiveData with the updated list of favorite herbs
                        favoriteHerbsLiveData.setValue(favoriteHerbList);

                        // Set noFavoritesLiveData to indicate there are favorites herbs
                        noFavoritesLiveData.setValue(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }

        // Set noFavoritesLiveData to indicate no favorites
        if (favoriteHerbIdsList.isEmpty()) {
            noFavoritesLiveData.setValue(true);
        }
    }
}
