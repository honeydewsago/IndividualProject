package com.example.pinellia.viewmodel;

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

    // Load all herbs from Firebase database
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

    // Perform a search for herbs based on a query
     public void performSearch(String query) {

         // Check if there are herbs available in the LiveData
        if (herbListLiveData == null || herbListLiveData.getValue() == null) {
            return; // No herbs available
        }

         // Get the list of herbs from the LiveData
        List<Herb> herbList = herbListLiveData.getValue();

         // Create a list to store the search results
        List<Herb> searchResults = new ArrayList<>();

         for (Herb herb : herbList) {

             // Get the herb names and convert them to lowercase
             String name = herb.getName().toLowerCase();
             String nameCN = herb.getNameCN().toLowerCase();
             String namePinyin = herb.getNamePinyin().toLowerCase();

             // Check if the search query is contained in any of the names
             if (name.contains(query.toLowerCase()) ||
                     nameCN.contains(query.toLowerCase()) ||
                     namePinyin.contains(query.toLowerCase())) {
                 // Add the herb to the search results list if there is a match
                 searchResults.add(herb);
             }
         }

         // Update the LiveData with the list of search results
         searchResultsLiveData.setValue(searchResults);
    }

    public LiveData<List<Herb>> getSearchResultsLiveData() {
        return searchResultsLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
}
