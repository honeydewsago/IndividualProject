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

    public MutableLiveData<List<Herb>> getFavoriteHerbsLiveData() {
        return favoriteHerbsLiveData;
    }

    public MutableLiveData<Boolean> getNoFavoritesLiveData() {
        return noFavoritesLiveData;
    }

    public MutableLiveData<List<String>> getFavoriteHerbIdsLiveData() {
        return favoriteHerbsIdsLiveData;
    }

    public void retrieveFavoriteHerbIds(SharedPreferences preferences, String keyFavoriteHerbs) {
        List<String> favoriteHerbIdsList = new ArrayList<>();

        String favoriteHerbsJson = preferences.getString(keyFavoriteHerbs, null);

        if (favoriteHerbsJson != null) {
            List<String> favoriteHerbIds = new Gson().fromJson(favoriteHerbsJson, new TypeToken<List<String>>() {}.getType());
            favoriteHerbIdsList.addAll(favoriteHerbIds);
        }

        favoriteHerbsIdsLiveData.setValue(favoriteHerbIdsList);
    }

    public void fetchFavoriteHerbs(List<String> favoriteHerbIdsList) {
        List<Herb> favoriteHerbList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("herbs");

        for (String herbId : favoriteHerbIdsList) {
            databaseReference.child(herbId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Herb herb = dataSnapshot.getValue(Herb.class);
                    if (herb != null) {
                        favoriteHerbList.add(herb);
                        favoriteHerbsLiveData.setValue(favoriteHerbList);
                        noFavoritesLiveData.setValue(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error if needed
                }
            });
        }

        if (favoriteHerbIdsList.isEmpty()) {
            noFavoritesLiveData.setValue(true);
        }
    }
}
