package com.example.pinellia.ui.selfCare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.adapter.SymptomAdapter;
import com.example.pinellia.databinding.ActivitySymptomBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.model.HerbScore;
import com.example.pinellia.model.SymptomScore;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SymptomActivity extends AppCompatActivity{

    private ActivitySymptomBinding binding;
    private SymptomAdapter symptomAdapter;
    private HerbAdapter herbAdapter;
    private List<String> symptomsList;
    private List<String> herbNameList;
    private List<HerbScore> herbScoreList;
    private List<Herb> herbRecommendationList;
    private List<SymptomScore> symptomScoreList;
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
        herbScoreList = new ArrayList<>();
        herbRecommendationList = new ArrayList<>();

        // Initialize RecyclerView with FlexboxLayoutManager to display meridian tropism
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP); // Enable item wrapping
        layoutManager.setJustifyContent(JustifyContent.FLEX_START); // Align items to the start of the container

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewSymptom.setLayoutManager(layoutManager);
        symptomAdapter = new SymptomAdapter(symptomsList);
        binding.recyclerViewSymptom.setAdapter(symptomAdapter);

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewSymRecommendation.setLayoutManager(new LinearLayoutManager(this));
        herbAdapter = new HerbAdapter(herbRecommendationList);
        binding.recyclerViewSymRecommendation.setAdapter(herbAdapter);

        binding.textViewRecommendResults.setVisibility(View.INVISIBLE);
        binding.buttonSubmitSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the selected symptoms from the adapter
                List<String> selectedSymptoms = symptomAdapter.getSelectedItems();

                if (!selectedSymptoms.isEmpty()) {
                    binding.textViewRecommendResults.setVisibility(View.VISIBLE);

                    int scrollTo = binding.textViewRecommendResults.getTop();
                    binding.scrollViewSymptomActivity.smoothScrollTo(0, scrollTo);

                    herbScoreList = calculateHerbRecommendation(selectedSymptoms);
                    getHerbList(herbScoreList);
                }
                else {
                    Toast.makeText(SymptomActivity.this, "No symptoms selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getHerbList(List<HerbScore> herbScoreList) {
        herbRecommendationList.clear();
        herbRecommendationCalculator.retrieveAllHerbs(new HerbRecommendationCalculator.AllHerbsCallback() {
            @Override
            public void onAllHerbsRetrieved(List<Herb> herbList) {
                for (HerbScore herbScore : herbScoreList) {
                    String herbName = herbScore.getHerbName();

                    // Search for the corresponding herb in herbList
                    for (Herb herb : herbList) {
                        if (herb.getName().equals(herbName)) {
                            herbRecommendationList.add(herb);
                            break; // Stop searching once found
                        }
                    }
                }
                herbAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAllHerbsError(Exception e) {
                // Handle the error
                Toast.makeText(SymptomActivity.this, "Error retrieving herbs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<HerbScore> calculateHerbRecommendation(List<String> selectedSymptoms) {
        List<String> actualSymptoms = getActualSymptoms(selectedSymptoms);
        List<HerbScore> recommendationList = new ArrayList<>();

        if (symptomScoreList != null && herbNameList != null) {
            List<Double> summedScores = new ArrayList<>();
            for (int i = 0; i < symptomScoreList.size(); i++) {
                summedScores.add(0.0); // Initialize the sum for each herb to 0.0
            }

            for (String actualSymptom : actualSymptoms) {

                for (int i = 0; i < symptomScoreList.size(); i++) {
                    SymptomScore scores = symptomScoreList.get(i);
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

            // Create and associate HerbScore with herb names
            for (int i = 0; i < 15; i++) {
                if (summedScores.get(i) != 0) {
                    HerbScore herbScore = new HerbScore(herbNameList.get(i), summedScores.get(i));
                    recommendationList.add(herbScore);
                }
            }

            // Sort mHerbScoreList in descending order based on scores
            Collections.sort(recommendationList, new Comparator<HerbScore>() {
                @Override
                public int compare(HerbScore herb1, HerbScore herb2) {
                    return Double.compare(herb2.getScores(), herb1.getScores());
                }
            });

            // Log the summed scores
            StringBuilder logText = new StringBuilder("Summed Scores:\n");
            for (HerbScore herbScore : recommendationList) {
                logText.append(herbScore.getHerbName()).append(": ").append(herbScore.getScores()).append("\n");
            }
            Log.d("SymptomActivity", logText.toString());
        }

        return recommendationList;
    }

    private void fetchSymptomScores() {
        herbRecommendationCalculator.retrieveSymptomScores(new HerbRecommendationCalculator.SymptomScoresCallback() {
            @Override
            public void onSymptomScoresRetrieved(List<SymptomScore> symptomScores) {
                // Assign the retrieved symptom scores
                symptomScoreList = symptomScores;
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