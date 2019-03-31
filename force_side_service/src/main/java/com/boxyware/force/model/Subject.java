package com.boxyware.force.model;

public class Subject {

    private final String name;
    private final Double midichlorian;
    private final String species;
    private final String gender;
    private final String homeworld;

    public Subject(String name, Double midichlorian,
            String species, String gender, String homeworld) {

        this.name = name;
        this.midichlorian = midichlorian;
        this.species = species;
        this.gender = gender;
        this.homeworld = homeworld;
    }

    public String getName() {
        return name;
    }

    public Double getMidichlorian() {
        return midichlorian;
    }

    public String getSpecies() {
        return species;
    }

    public String getGender() {
        return gender;
    }

    public String getHomeworld() {
        return homeworld;
    }
}