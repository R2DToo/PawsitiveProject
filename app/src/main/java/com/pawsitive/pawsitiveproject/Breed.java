package com.pawsitive.pawsitiveproject;

/**
 * This class holds information on the breed from an api requested picture.
 */
public class Breed extends Object {
    private String bred_for;
    private String breed_group;
    private String life_span;
    private String name;
    private String temperament;

    public void Breed() {}

    public Breed(String bred_for, String breed_group, String life_span, String name, String temperament) {
        this.bred_for = bred_for;
        this.breed_group = breed_group;
        this.life_span = life_span;
        this.name = name;
        this.temperament = temperament;
    }

    public String getBred_for() {
        return bred_for;
    }

    public String getBreed_group() {
        return breed_group;
    }

    public String getLifespan() {
        return life_span;
    }

    public String getName() {
        return name;
    }

    public String getTemperament() {
        return temperament;
    }

    public void setBred_for(String bred_for) {
        this.bred_for = bred_for;
    }

    public void setBreed_group(String breed_group) {
        this.breed_group = breed_group;
    }

    public void setLifespan(String life_span) {
        this.life_span = life_span;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemperament(String temperament) {
        this.temperament = temperament;
    }

    @Override
    public String toString() {
        return "Breed{" +
                "bred_for='" + bred_for + '\'' +
                ", breed_group='" + breed_group + '\'' +
                ", life_span='" + life_span + '\'' +
                ", name='" + name + '\'' +
                ", temperament='" + temperament + '\'' +
                '}';
    }
}
