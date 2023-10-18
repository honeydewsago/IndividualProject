package com.example.pinellia.model;

import java.util.List;

public class SymptomScore {
    private String symptomName;
    private List<Double> scores;

    public SymptomScore() {
    }

    public SymptomScore(String symptomName, List<Double> scores) {
        this.symptomName = symptomName;
        this.scores = scores;
    }

    public String getSymptomName() {
        return symptomName;
    }

    public List<Double> getScores() {
        return scores;
    }
}
