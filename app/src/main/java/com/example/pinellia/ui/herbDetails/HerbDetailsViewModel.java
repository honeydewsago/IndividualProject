package com.example.pinellia.ui.herbDetails;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinellia.adapter.MeridianTropismAdapter;
import com.example.pinellia.databinding.ActivityHerbDetailsBinding;
import com.example.pinellia.model.Herb;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
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

    public void setIsFavorite(boolean isFavorite) {
        isFavoriteLiveData.setValue(isFavorite);
    }
}