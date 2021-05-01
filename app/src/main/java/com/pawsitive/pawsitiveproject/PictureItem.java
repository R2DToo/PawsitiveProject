package com.example.pawsitiveproject;

/**
 * The final, flattened structure for a picture and all its information
 */
public class PictureItem extends Object{
    private String id;
    private String url;
    private String job;
    private String job_category;
    private String lifespan;
    private String name;
    private String temperament;

    public PictureItem(String id, String url, String job, String job_category, String lifespan, String name, String temperament) {
        this.id = id;
        this.url = url;
        this.job = job;
        this.job_category = job_category;
        this.lifespan = lifespan;
        this.name = name;
        this.temperament = temperament;
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

    public String getJob() {
        return this.job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getJobCategory() {
        return this.job_category;
    }

    public void setJobCategory(String job_category) {
        this.job_category = job_category;
    }

    public String getLifespan() {
        return this.lifespan;
    }

    public void setLifespan(String lifespan) {
        this.lifespan = lifespan;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemperament() {
        return this.temperament;
    }

    public void setTemperament(String temperament) {
        this.temperament = temperament;
    }

    @Override
    public String toString() {
        return "PictureItem{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", job='" + job + '\'' +
                ", job_category='" + job_category + '\'' +
                ", lifespan='" + lifespan + '\'' +
                ", name='" + name + '\'' +
                ", temperament='" + temperament + '\'' +
                '}';
    }
}
