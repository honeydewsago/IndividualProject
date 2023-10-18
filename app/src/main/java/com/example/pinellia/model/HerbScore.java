package com.example.pinellia.model;

public class HerbScore {
    private String herbName;
    private Double scores;

    public HerbScore() {
    }

    public HerbScore(String herbName, Double scores) {
        this.herbName = herbName;
        this.scores = scores;
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
}
