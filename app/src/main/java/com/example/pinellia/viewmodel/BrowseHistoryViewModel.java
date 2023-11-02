package com.example.pinellia.viewmodel;

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

public class BrowseHistoryViewModel extends ViewModel {

    // LiveData for history herb IDs and history herb list
    private MutableLiveData<List<String>> historyHerbIdsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Herb>> historyHerbListLiveData = new MutableLiveData<>();

    public LiveData<List<String>> getHistoryHerbIdsLiveData() {
        return historyHerbIdsLiveData;
    }

    public LiveData<List<Herb>> getHistoryHerbListLiveData() {
        return historyHerbListLiveData;
    }

    // Retrieve and set the history herb IDs from SharedPreferences
    public void retrieveHistoryHerbIds(SharedPreferences preferences, String keyHistory) {
        List<String> historyHerbIdsList = new ArrayList<>();

        String historyHerbsJson = preferences.getString(keyHistory, null);

        if (historyHerbsJson != null) {
            // Deserialize the JSON string into a list of history herb IDs
            List<String> historyHerbIds = new Gson().fromJson(historyHerbsJson, new TypeToken<List<String>>() {}.getType());
            historyHerbIdsList.addAll(historyHerbIds);
        }

        // Set the LiveData with the history herb IDs
        historyHerbIdsLiveData.setValue(historyHerbIdsList);
    }

    // Fetch history herbs from Firebase
    public void fetchHistoryHerbs(List<String> historyHerbIds) {
        List<Herb> historyHerbList = new ArrayList<>();

        // Firebase reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("herbs");

        // Fetch corresponding herbs from Firebase
        for (String herbId : historyHerbIds) {
            databaseReference.child(herbId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Herb herb = dataSnapshot.getValue(Herb.class);
                    if (herb != null) {
                        // Add the retrieved herb to the history herb list
                        historyHerbList.add(herb);
                        // Set the LiveData with the updated history herb list
                        historyHerbListLiveData.setValue(historyHerbList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Log.e("FetchHistory", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }
}
