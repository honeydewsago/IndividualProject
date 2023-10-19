package com.example.pinellia.ui.selfCare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.pinellia.adapter.HerbAdapter;
import com.example.pinellia.adapter.SymptomAdapter;
import com.example.pinellia.databinding.ActivityUsageBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.model.HerbScore;
import com.example.pinellia.model.SymptomScore;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsageActivity extends AppCompatActivity {

    private ActivityUsageBinding binding;
    private SymptomAdapter symptomAdapter;
    private HerbAdapter herbAdapter;
    private List<String> usageList;
    private List<String> herbNameList;
    private List<HerbScore> herbScoreList;
    private List<Herb> herbRecommendationList;
    private List<SymptomScore> symptomScoreList;
    private HerbRecommendationCalculator herbRecommendationCalculator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUsageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Usage");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        herbRecommendationCalculator = new HerbRecommendationCalculator();
        fetchHerbNameList();
        fetchSymptomScores();

        String[] usageArray = {
                "Beauty", "Nourish Blood", "Anti-Cancer", "Clearing Heat", "Calming Mind", "Regulate Qi"
        };

        usageList = new ArrayList<>(Arrays.asList(usageArray));
        herbScoreList = new ArrayList<>();
        herbRecommendationList = new ArrayList<>();

        // Initialize RecyclerView with FlexboxLayoutManager to display meridian tropism
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP); // Enable item wrapping
        layoutManager.setJustifyContent(JustifyContent.FLEX_START); // Align items to the start of the container

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewUsage.setLayoutManager(layoutManager);
        symptomAdapter = new SymptomAdapter(usageList);
        binding.recyclerViewUsage.setAdapter(symptomAdapter);

        // Set up RecyclerView to display favorite herbs
        binding.recyclerViewUsgRecommendation.setLayoutManager(new LinearLayoutManager(this));
        herbAdapter = new HerbAdapter(herbRecommendationList);
        binding.recyclerViewUsgRecommendation.setAdapter(herbAdapter);

        binding.buttonSubmitUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the selected symptoms from the adapter
                List<String> selectedSymptoms = symptomAdapter.getSelectedItems();

                if (!selectedSymptoms.isEmpty()) {

                    int scrollTo = binding.textViewUsageRecommendResults.getTop();
                    binding.scrollViewUsageActivity.smoothScrollTo(0, scrollTo);

                    herbScoreList = herbRecommendationCalculator.calculateHerbRecommendation(selectedSymptoms, symptomScoreList, herbNameList);
                    getHerbList(herbScoreList);
                }
                else {
                    Toast.makeText(UsageActivity.this, "No symptoms selected", Toast.LENGTH_SHORT).show();
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
                            herb.setSymptomsList(herbScore.getSymptoms());
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
                Toast.makeText(UsageActivity.this, "Error retrieving herbs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(UsageActivity.this, "Error retrieving symptom scores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UsageActivity.this, "Error retrieving herb names: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}