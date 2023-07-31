package com.example.pinellia.model;

public class Herb {
    private String name;
    private String description;

    public Herb() {
        // Required empty constructor for Firebase
    }

    public Herb(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
