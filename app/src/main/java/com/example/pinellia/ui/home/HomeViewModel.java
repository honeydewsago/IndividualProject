package com.example.pinellia.ui.home;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pinellia.model.Herb;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Herb>> herbLiveData;
    private MutableLiveData<String> errorMessageLiveData;

    // LiveData to provide the list of herbs
    public LiveData<List<Herb>> getHerbData() {
        if (herbLiveData == null) {
            herbLiveData = new MutableLiveData<>();
            errorMessageLiveData = new MutableLiveData<>();
        }

        // Load herbs from Firebase and update herbLiveData
        loadHerbsFromFirebase();

        return herbLiveData;
    }

    // Load herbs data from Firebase
    private void loadHerbsFromFirebase() {
        // Create a list to store herbs retrieved from Firebase
        List<Herb> herbList = new ArrayList<>();

        // Fetch herb data from Firebase database
        FirebaseDatabase.getInstance().getReference("herbs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                herbList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve herb object
                    Herb herb = snapshot.getValue(Herb.class);
                    herbList.add(herb);
                }

                // Sort the herbList by name in ascending order (A-Z)
                Collections.sort(herbList, new Comparator<Herb>() {
                    @Override
                    public int compare(Herb herb1, Herb herb2) {
                        return herb1.getName().compareToIgnoreCase(herb2.getName());
                    }
                });

                // Update the LiveData with the sorted herbList
                herbLiveData.setValue(herbList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error and set the error message to the LiveData
                errorMessageLiveData.setValue(databaseError.getMessage());
            }
        });
    }

    // LiveData to provide error messages
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
}