package com.example.pinellia.ui.selfCare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.example.pinellia.R;
import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.adapter.SymptomAdapter;
import com.example.pinellia.databinding.ActivityRecognitionResultsBinding;
import com.example.pinellia.databinding.ActivitySymptomBinding;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SymptomActivity extends AppCompatActivity {

    private ActivitySymptomBinding binding;
    private SymptomAdapter symptomAdapter;
    private List<String> symptomsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySymptomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Symptom");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String[] symptomsArray = {
                "Abdominal Pain", "Bleeding", "High Blood Pressure", "Cold", "Cough", "Constipation", "Cramps",
                "Dampness", "Diarrhea", "Menstruation Pain/Cramps", "Nose Bleed", "Fatigue", "Fever", "Gastric",
                "Insomnia", "Itching", "Phlegm", "Swelling", "Yin Deficiency"
        };

        symptomsList = new ArrayList<>(Arrays.asList(symptomsArray));

        // Initialize RecyclerView with FlexboxLayoutManager to display meridian tropism
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP); // Enable item wrapping
        layoutManager.setJustifyContent(JustifyContent.FLEX_START); // Align items to the start of the container

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewSymptom.setLayoutManager(layoutManager);
        symptomAdapter = new SymptomAdapter(symptomsList);
        binding.recyclerViewSymptom.setAdapter(symptomAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}