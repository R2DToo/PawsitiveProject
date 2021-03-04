package com.example.pawsitiveproject;

public class PictureItem extends Object{
    private String id;
    private String url;

    public PictureItem(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
