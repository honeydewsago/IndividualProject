package com.example.pinellia.ui.history;

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
    private MutableLiveData<List<String>> historyHerbIdsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Herb>> historyHerbListLiveData = new MutableLiveData<>();

    public LiveData<List<String>> getHistoryHerbIdsLiveData() {
        return historyHerbIdsLiveData;
    }

    public LiveData<List<Herb>> getHistoryHerbListLiveData() {
        return historyHerbListLiveData;
    }

    public void retrieveHistoryHerbIds(SharedPreferences preferences, String keyHistory) {
        List<String> historyHerbIdsList = new ArrayList<>();

        String historyHerbsJson = preferences.getString(keyHistory, null);

        if (historyHerbsJson != null) {
            List<String> historyHerbIds = new Gson().fromJson(historyHerbsJson, new TypeToken<List<String>>() {}.getType());
            historyHerbIdsList.addAll(historyHerbIds);
        }

        historyHerbIdsLiveData.setValue(historyHerbIdsList);
    }

    public void fetchHistoryHerbs(List<String> historyHerbIds) {
        List<Herb> historyHerbList = new ArrayList<>();

        // Firebase reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("herbs");

        // Iterate through history herb IDs and fetch corresponding herbs from Firebase
        for (String herbId : historyHerbIds) {
            databaseReference.child(herbId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Herb herb = dataSnapshot.getValue(Herb.class);
                    if (herb != null) {
                        historyHerbList.add(herb);
                        historyHerbListLiveData.setValue(historyHerbList);
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
}
