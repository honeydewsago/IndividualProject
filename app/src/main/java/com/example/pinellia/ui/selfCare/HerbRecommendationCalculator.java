package com.example.pinellia.ui.selfCare;

import android.util.Log;

import com.example.pinellia.model.Herb;
import com.example.pinellia.model.HerbScore;
import com.example.pinellia.model.SymptomScore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HerbRecommendationCalculator {
    private final DatabaseReference tfidfDatabaseReference;
    private final DatabaseReference herbNameDatabaseReference;
    private final DatabaseReference herbDatabaseReference;

    public HerbRecommendationCalculator() {
        // Initialize the Firebase Database reference for TF-IDF scores
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tfidfDatabaseReference = database.getReference("tfidf_data");
        herbNameDatabaseReference = database.getReference("herb_names");
        herbDatabaseReference = database.getReference("herbs");
    }

    // Calculate herb recommendations based on selected symptoms
    public List<HerbScore> calculateHerbRecommendation(List<String> selectedSymptoms, List<SymptomScore> symptomScoreList, List<String> herbNameList) {
        List<String> actualSymptoms = getActualSymptoms(selectedSymptoms);
        List<HerbScore> recommendationList = new ArrayList<>();

        if (symptomScoreList != null && herbNameList != null) {
            List<Double> summedScores = new ArrayList<>();

            // Initialize the sum for each herb to 0.0
            for (int i = 0; i < symptomScoreList.size(); i++) {
                summedScores.add(0.0);
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
                    List<String> relevantSymptoms = findRelevantSymptoms(herbNameList.get(i), actualSymptoms, selectedSymptoms, symptomScoreList, herbNameList);
                    HerbScore herbScore = new HerbScore(herbNameList.get(i), summedScores.get(i), relevantSymptoms);
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
                logText.append(herbScore.getHerbName()).append(": ").append(herbScore.getScores()).append(" Relevant Symptoms: ");
                for (String symptom : herbScore.getSymptoms()) {
                    logText.append(symptom).append(", ");
                }
                logText.append("\n");
            }
            Log.d("SymptomActivity", logText.toString());
        }

        return recommendationList;
    }

    // Find relevant symptoms for a herb
    private List<String> findRelevantSymptoms(String herbName, List<String> actualSymptoms, List<String> userSymptoms, List<SymptomScore> symptomScoreList, List<String> herbNameList) {
        List<String> relevantSymptoms = new ArrayList<>();

        for (String symptom : actualSymptoms) {
            for (SymptomScore scores : symptomScoreList) {
                String symptomName = scores.getSymptomName();
                List<Double> scoresList = scores.getScores();

                if (symptom.equals(symptomName)) {
                    int herbIndex = herbNameList.indexOf(herbName);
                    int herbNameIndex = actualSymptoms.indexOf(symptom);
                    if (herbIndex >= 0 && scoresList.get(herbIndex) > 0) {
                        relevantSymptoms.add(userSymptoms.get(herbNameIndex));
                    }
                }
            }
        }

        return relevantSymptoms;
    }

    // Convert selected symptoms to their actual symptoms text
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
            } else if (symptom.equals("Nourish Blood")) {
                lowercaseSymptom = "blood";
            } else if (symptom.equals("Anti-Cancer")) {
                lowercaseSymptom = "cancer";
            } else if (symptom.equals("Clearing Heat")) {
                lowercaseSymptom = "heat";
            } else if (symptom.equals("Calming Mind")) {
                lowercaseSymptom = "mind";
            } else if (symptom.equals("Regulate Qi")) {
                lowercaseSymptom = "qi";
            } else {
                lowercaseSymptom = symptom.toLowerCase();
            }

            actualSymptoms.add(lowercaseSymptom);
        }

        return actualSymptoms;
    }

    // Retrieve symptom scores from Firebase Database
    public void retrieveSymptomScores(final SymptomScoresCallback callback) {
        tfidfDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<SymptomScore> symptomScoreList = new ArrayList<>();

                for (DataSnapshot symptomSnapshot : dataSnapshot.getChildren()) {
                    String symptomName = symptomSnapshot.getKey();
                    List<Double> scores = new ArrayList<>();

                    for (DataSnapshot scoreSnapshot : symptomSnapshot.getChildren()) {
                        double score = scoreSnapshot.getValue(Double.class);
                        scores.add(score);
                    }

                    SymptomScore symptomScore = new SymptomScore(symptomName, scores);
                    symptomScoreList.add(symptomScore);
                }

                callback.onSymptomScoresRetrieved(symptomScoreList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSymptomScoresError(databaseError.toException());
            }
        });
    }

    // Callback interface for retrieving symptom scores
    public interface SymptomScoresCallback {
        void onSymptomScoresRetrieved(List<SymptomScore> symptomScores);

        void onSymptomScoresError(Exception e);
    }

    // Retrieve herb names from Firebase Database
    public void retrieveHerbNames(final HerbNamesCallback callback) {
        herbNameDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> herbNames = new ArrayList<>();

                for (DataSnapshot herbSnapshot : dataSnapshot.getChildren()) {
                    String herbName = herbSnapshot.getValue(String.class);
                    herbNames.add(herbName);
                }

                callback.onHerbNamesRetrieved(herbNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onHerbNamesError(databaseError.toException());
            }
        });
    }

    // Callback interface for retrieving herb names
    public interface HerbNamesCallback {
        void onHerbNamesRetrieved(List<String> herbNames);

        void onHerbNamesError(Exception e);
    }

    // Retrieve all herb data from Firebase Database
    public void retrieveAllHerbs(final AllHerbsCallback callback) {
        herbDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Herb> herbList = new ArrayList<>();

                for (DataSnapshot herbSnapshot : dataSnapshot.getChildren()) {
                    Herb herb = herbSnapshot.getValue(Herb.class);
                    herbList.add(herb);
                }

                callback.onAllHerbsRetrieved(herbList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onAllHerbsError(databaseError.toException());
            }
        });
    }

    // Callback interface for retrieving all herb data
    public interface AllHerbsCallback {
        void onAllHerbsRetrieved(List<Herb> herbList);

        void onAllHerbsError(Exception e);
    }
}
