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
        return herbLiveData;
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
                herbLiveData.setValue(herbList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error and set the error message to the LiveData
                errorMessageLiveData.setValue(databaseError.getMessage());
            }
        });
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
}