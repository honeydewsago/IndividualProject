package com.example.pinellia.ui;

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

public class SearchHerbViewModel extends ViewModel {

    private MutableLiveData<List<Herb>> herbListLiveData;
    private MutableLiveData<List<Herb>> searchResultsLiveData;
    private MutableLiveData<String> errorMessageLiveData;

    public SearchHerbViewModel() {
        // Load the herb list from Firebase when the ViewModel is created
        getHerbList();
    }

    public LiveData<List<Herb>> getHerbList() {
        if (herbListLiveData == null) {
            herbListLiveData = new MutableLiveData<>();
            searchResultsLiveData = new MutableLiveData<>();
            errorMessageLiveData = new MutableLiveData<>();
            loadHerbsFromFirebase();
        }

        return herbListLiveData;
    }

    private void loadHerbsFromFirebase() {
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

                herbListLiveData.setValue(herbList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorMessageLiveData.setValue(databaseError.getMessage());
            }
        });
    }

     public void performSearch(String query) {

        if (herbListLiveData == null || herbListLiveData.getValue() == null) {
            return; // No herbs available
        }

        List<Herb> herbList = herbListLiveData.getValue();
        List<Herb> searchResults = new ArrayList<>();

         for (Herb herb : herbList) {
             String name = herb.getName().toLowerCase();
             String nameCN = herb.getNameCN().toLowerCase();
             String namePinyin = herb.getNamePinyin().toLowerCase();

             if (name.contains(query.toLowerCase()) ||
                     nameCN.contains(query.toLowerCase()) ||
                     namePinyin.contains(query.toLowerCase())) {
                 searchResults.add(herb);
             }
         }

        searchResultsLiveData.setValue(searchResults);
    }

    public LiveData<List<Herb>> getSearchResultsLiveData() {
        return searchResultsLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
}
