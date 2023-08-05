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

    public LiveData<List<Herb>> getHerbData() {
        if (herbLiveData == null) {
            herbLiveData = new MutableLiveData<>();
            errorMessageLiveData = new MutableLiveData<>();
            loadHerbsFromFirebase();
        }
        else {
            reloadHerbsFromFirebase();
        }
        return herbLiveData;
    }

    private void loadHerbsFromFirebase() {
        // Fetch herbs from Firebase and update herbLiveData
        List<Herb> herbList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("herbs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                herbList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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

                herbLiveData.setValue(herbList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error and set the error message to the LiveData
                errorMessageLiveData.setValue(databaseError.getMessage());
            }
        });
    }

    private void reloadHerbsFromFirebase() {
        // Fetch herbs from Firebase and update herbLiveData
        // same implementation as loadHerbsFromFirebase() but without clearing the list to keep the existing data in the list and update it with new data
        FirebaseDatabase.getInstance().getReference("herbs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Herb> herbList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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

                herbLiveData.setValue(herbList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error and set the error message to the LiveData
                errorMessageLiveData.setValue(databaseError.getMessage());
            }
        });
    }

    public void refreshHerbData() {
        loadHerbsFromFirebase(); // This method can be used to reload the data
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
}