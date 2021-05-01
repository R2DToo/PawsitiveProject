package com.example.pawsitiveproject;

import android.util.Log;

import java.util.List;

/**
 * Used by gson to parse the response of a random picture request
 */
public class RandomImageResponse extends Object {
    private String id;
    private String url;

    private List<Breed> breeds;

    public void RandomImageResponse() {}

    public void RandomImageResponse(String id, String url, List<Breed> breeds) {
        this.id = id;
        this.url = url;
        this.breeds = breeds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Breed> getBreeds() {
        return breeds;
    }

    public void setBreeds(List<Breed> breeds) {
        this.breeds = breeds;
    }

    @Override
    public String toString() {
        return "RandomImageResponse{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", breeds=" + breeds +
                '}';
    }
}
