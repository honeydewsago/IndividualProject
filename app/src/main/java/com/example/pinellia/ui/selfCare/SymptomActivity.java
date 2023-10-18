package com.example.pinellia.ui.selfCare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pinellia.adapter.SymptomAdapter;
import com.example.pinellia.databinding.ActivitySymptomBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.model.SymptomScores;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymptomActivity extends AppCompatActivity{

    private ActivitySymptomBinding binding;
    private SymptomAdapter symptomAdapter;
    private List<String> symptomsList;
    private List<String> herbNameList;
    private List<SymptomScores> symptomScoresList;
    private HerbRecommendationCalculator herbRecommendationCalculator;

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

        herbRecommendationCalculator = new HerbRecommendationCalculator();
        fetchHerbNameList();
        fetchSymptomScores();

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

        binding.textViewRecommendResults.setVisibility(View.INVISIBLE);
        binding.buttonSubmitSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.textViewRecommendResults.setVisibility(View.VISIBLE);

                // Retrieve the selected symptoms from the adapter
                List<String> selectedSymptoms = symptomAdapter.getSelectedItems();

                // Create a string to display the selected symptoms
                StringBuilder selectedSymptomsText = new StringBuilder("Selected Symptoms:\n");
                for (String symptom : selectedSymptoms) {
                    selectedSymptomsText.append(symptom).append("\n");
                }

                // Show a toast with the selected symptoms
                Toast.makeText(SymptomActivity.this, selectedSymptomsText.toString(), Toast.LENGTH_LONG).show();

                int scrollTo = binding.textViewRecommendResults.getTop();
                binding.scrollViewSymptomActivity.smoothScrollTo(0, scrollTo);

                calculateHerbRecommendation(selectedSymptoms);
            }
        });
    }

    private void calculateHerbRecommendation(List<String> selectedSymptoms) {
        List<String> actualSymptoms = getActualSymptoms(selectedSymptoms);

        if (symptomScoresList != null) {
            List<Double> summedScores = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                summedScores.add(0.0); // Initialize the sum for each herb to 0.0
            }

            for (String actualSymptom : actualSymptoms) {
                for (int i = 0; i < 15; i++) {
                    SymptomScores scores = symptomScoresList.get(i);
                    String symptomName = scores.getSymptomName();
                    List<Double> scoresList = scores.getScores();

                    if (actualSymptom.equals(symptomName)) {
                        // Sum the scores for the matching symptom
                        for (int j = 0; j < 15; j++) {
                            summedScores.set(j, summedScores.get(j) + scoresList.get(j));
                        }
                    }
                }
            }

            // Log the summed scores
            StringBuilder logText = new StringBuilder("Summed Scores:\n");
            for (int i = 0; i < 15; i++) {
                logText.append("Herb ").append(i).append(": ").append(summedScores.get(i)).append("\n");
            }
            Log.d("SymptomActivity", logText.toString());
        }
    }

    private void fetchSymptomScores() {
        herbRecommendationCalculator.retrieveSymptomScores(new HerbRecommendationCalculator.SymptomScoresCallback() {
            @Override
            public void onSymptomScoresRetrieved(List<SymptomScores> symptomScores) {
                // Assign the retrieved symptom scores
                symptomScoresList = symptomScores;
            }

            @Override
            public void onSymptomScoresError(Exception e) {
                // Handle the error
                Toast.makeText(SymptomActivity.this, "Error retrieving symptom scores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHerbNameList() {
        herbRecommendationCalculator.retrieveHerbNames(new HerbRecommendationCalculator.HerbNamesCallback() {
            @Override
            public void onHerbNamesRetrieved(List<String> herbNames) {
                // Assign the retrieved herb name list
                herbNameList = herbNames;
            }

            @Override
            public void onHerbNamesError(Exception e) {
                // Handle the error
                Toast.makeText(SymptomActivity.this, "Error retrieving herb names: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getActualSymptoms(List<String> selectedSymptoms) {
        List<String> actualSymptoms = new ArrayList<>();

        for (String symptom : selectedSymptoms) {
            String lowercaseSymptom;

            if (symptom.equals("High Blood Pressure")) {
                lowercaseSymptom = "blood pressure";
            } else if (symptom.equals("Menstruation Pain/Cramps")) {
                lowercaseSymptom = "menstruation";
            } else if (symptom.equals("Nose Bleed")) {
                lowercaseSymptom = "epistaxis";
            } else if (symptom.equals("Yin Deficiency")) {
                lowercaseSymptom = "yin";
            } else {
                lowercaseSymptom = symptom.toLowerCase();
            }

            actualSymptoms.add(lowercaseSymptom);
        }

        return actualSymptoms;
    }
    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}