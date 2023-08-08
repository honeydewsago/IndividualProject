package com.example.pinellia.model;

import java.io.Serializable;
import java.util.List;

public class Herb implements Serializable {

    private String id;
    private String name;
    private String nameScientific;
    private String nameCN;
    private String namePinyin;
    private String property;
    private List<String> meridianTropism;
    private List<String> flavour;
    private String toxicology;
    private String storage;
    private String characteristics;
    private String placeOfOrigin;
    private String medicinePart;
    private String method;
    private String effect;
    private String usage;
    private String dosage;
    private String prohibition;
    private String imageLink;

    public Herb() {
        // Required empty constructor
    }

    public Herb(String id, String name, String nameScientific, String nameCN, String namePinyin, String property, List<String> meridianTropism, List<String> flavour, String toxicology, String storage, String characteristics, String placeOfOrigin, String medicinePart, String method, String effect, String usage, String dosage, String prohibition, String imageLink) {
        this.id = id;
        this.name = name;
        this.nameScientific = nameScientific;
        this.nameCN = nameCN;
        this.namePinyin = namePinyin;
        this.property = property;
        this.meridianTropism = meridianTropism;
        this.flavour = flavour;
        this.toxicology = toxicology;
        this.storage = storage;
        this.characteristics = characteristics;
        this.placeOfOrigin = placeOfOrigin;
        this.medicinePart = medicinePart;
        this.method = method;
        this.effect = effect;
        this.usage = usage;
        this.dosage = dosage;
        this.prohibition = prohibition;
        this.imageLink = imageLink;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameScientific() {
        return nameScientific;
    }

    public String getNameCN() {
        return nameCN;
    }

    public String getNamePinyin() {
        return namePinyin;
    }

    public String getProperty() {
        return property;
    }

    public List<String> getMeridianTropism() {
        return meridianTropism;
    }

    public List<String> getFlavour() {
        return flavour;
    }

    public String getToxicology() {
        return toxicology;
    }

    public String getStorage() {
        return storage;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public String getPlaceOfOrigin() {
        return placeOfOrigin;
    }

    public String getMedicinePart() {
        return medicinePart;
    }

    public String getMethod() {
        return method;
    }

    public String getEffect() {
        return effect;
    }

    public String getUsage() {
        return usage;
    }

    public String getDosage() {
        return dosage;
    }

    public String getProhibition() {
        return prohibition;
    }

    public String getImageLink() {
        return imageLink;
    }
}
