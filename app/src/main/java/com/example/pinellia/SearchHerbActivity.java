package com.example.pinellia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.pinellia.databinding.ActivitySearchHerbBinding;
import com.example.pinellia.databinding.FragmentFavouritesBinding;

public class SearchHerbActivity extends AppCompatActivity {

    private ActivitySearchHerbBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchHerbBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);
    }
}