package com.pawsitive.pawsitiveproject;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Used by gson to parse the votes response. Implements Comparable to enable sorting by date
 */
public class Vote extends Object implements Comparable<Vote> {
    private String country_code;
    private String created_at;
    private int id;
    private String image_id;
    private String sub_id;
    private int value;
    private String image_url;

    public void Vote () {}

    public Vote(String country_code, String created_at, int id, String image_id, String sub_id, int value) {
        this.country_code = country_code;
        this.created_at = created_at;
        this.id = id;
        this.image_id = image_id;
        this.sub_id = sub_id;
        this.value = value;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public Date getCreated_at() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CANADA);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date_created = null;
        try {
            date_created = dateFormatter.parse(created_at);
        } catch (ParseException pe) {
            Log.d("bsr", "DATE PARSE ERROR: " + pe);
        }
        return date_created;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getSub_id() {
        return sub_id;
    }

    public void setSub_id(String sub_id) {
        this.sub_id = sub_id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "country_code='" + country_code + '\'' +
                ", created_at='" + created_at + '\'' +
                ", id=" + id +
                ", image_id='" + image_id + '\'' +
                ", sub_id='" + sub_id + '\'' +
                ", value=" + value +
                ", image_url='" + image_url + '\'' +
                '}';
    }

    @Override
    public int compareTo(Vote vote) {
        if (getCreated_at() == null || vote.getCreated_at() == null)
            return 0;
        return getCreated_at().compareTo(vote.getCreated_at());
    }
}
