package com.example.pinellia.ui;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class HerbDetailsViewModel  extends ViewModel {
    private MutableLiveData<Boolean> isFavoriteLiveData = new MutableLiveData<>();
    private MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();

    // Getter for isFavorite LiveData
    public LiveData<Boolean> getIsFavoriteLiveData() {
        return isFavoriteLiveData;
    }

    // Getter for toastMessage LiveData
    public LiveData<String> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    // Method to toggle the favorite status
    public void toggleFavorite(boolean isCurrentlyFavorite) {
        boolean newFavoriteStatus = !isCurrentlyFavorite;
        isFavoriteLiveData.setValue(newFavoriteStatus);

        // Display Toast message
        if (newFavoriteStatus) {
            toastMessageLiveData.setValue("Added to favorites");
        } else {
            toastMessageLiveData.setValue("Removed from favorites");
        }
    }

    // Method to save the favorite list to SharedPreferences
    public void saveFavoriteList(String herbId, boolean isFavorite, SharedPreferences preferences, String keyFavoriteHerbs) {
        String favoriteHerbsJson = preferences.getString(keyFavoriteHerbs, null);

        List<String> favoriteHerbIds = new ArrayList<>();

        if (favoriteHerbsJson != null) {
            favoriteHerbIds = new Gson().fromJson(favoriteHerbsJson, new TypeToken<List<String>>() {}.getType());
        }

        if (isFavorite) {
            if (!favoriteHerbIds.contains(herbId)) {
                favoriteHerbIds.add(0, herbId); // Add at the beginning to maintain order
            }
        } else {
            favoriteHerbIds.remove(herbId);
        }

        String updatedFavoriteHerbsJson = new Gson().toJson(favoriteHerbIds);
        preferences.edit().putString(keyFavoriteHerbs, updatedFavoriteHerbsJson).apply();
    }

    public void updateFavoriteButtonState(String herbId, SharedPreferences preferences,  String keyFavoriteHerbs) {
        String favoriteHerbsJson = preferences.getString(keyFavoriteHerbs, null);

        List<String> favoriteHerbIds = new ArrayList<>();

        if (favoriteHerbsJson != null) {
            favoriteHerbIds.addAll(new Gson().fromJson(favoriteHerbsJson, new TypeToken<List<String>>() {}.getType()));
        }

        isFavoriteLiveData.setValue(favoriteHerbIds.contains(herbId));
    }

    public void saveBrowseHistory(String herbId, SharedPreferences preferences,  String keyHistory) {
        String historyHerbsJson = preferences.getString(keyHistory, null);

        List<String> historyHerbIds = new ArrayList<>();

        if (historyHerbsJson != null) {
            historyHerbIds = new Gson().fromJson(historyHerbsJson, new TypeToken<List<String>>() {}.getType());
        }

        // Remove the herbId if it already exists to ensure uniqueness
        historyHerbIds.remove(herbId);

        // Add the herbId at the beginning to maintain order
        historyHerbIds.add(0, herbId);

        String updatedHistoryHerbsJson = new Gson().toJson(historyHerbIds);
        preferences.edit().putString(keyHistory, updatedHistoryHerbsJson).apply();
    }

}