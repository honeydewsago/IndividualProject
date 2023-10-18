package com.example.pinellia.model;

import java.util.List;

public class HerbScore {
    private String herbName;
    private Double scores;
    private List<String> symptoms;

    public HerbScore() {
    }

    public HerbScore(String herbName, Double scores, List<String> symptoms) {
        this.herbName = herbName;
        this.scores = scores;
        this.symptoms = symptoms;
    }

    public String getHerbName() {
        return herbName;
    }

    public void setHerbName(String herbName) {
        this.herbName = herbName;
    }

    public Double getScores() {
        return scores;
    }

    public void setScores(Double scores) {
        this.scores = scores;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}
