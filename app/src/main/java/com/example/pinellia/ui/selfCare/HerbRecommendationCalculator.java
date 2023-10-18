package com.example.pinellia.ui.selfCare;

import com.example.pinellia.model.SymptomScore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HerbRecommendationCalculator {
    private final DatabaseReference tfidfDatabaseReference;
    private final DatabaseReference herbNameDatabaseReference;
    private List<String> herbNameList;

    public HerbRecommendationCalculator() {
        // Initialize the Firebase Database reference for TF-IDF scores
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tfidfDatabaseReference = database.getReference("tfidf_data");
        herbNameDatabaseReference = database.getReference("herb_names");
    }

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

    public interface SymptomScoresCallback {
        void onSymptomScoresRetrieved(List<SymptomScore> symptomScores);

        void onSymptomScoresError(Exception e);
    }

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

    public interface HerbNamesCallback {
        void onHerbNamesRetrieved(List<String> herbNames);

        void onHerbNamesError(Exception e);
    }


}
