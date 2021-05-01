package com.pawsitive.pawsitiveproject;

/**
 * Used by gson to parse the response from uploading a picture
 */
public class UploadResponse extends Object {
    private String id;
    private String url;
    private String sub_id;
    private int width;
    private int height;
    private String original_filename;
    private int pending;
    private int approved;

    public UploadResponse() {}

    public UploadResponse(String id, String url, String sub_id, int width, int height, String original_filename, int pending, int approved) {
        this.id = id;
        this.url = url;
        this.sub_id = sub_id;
        this.width = width;
        this.height = height;
        this.original_filename = original_filename;
        this.pending = pending;
        this.approved = approved;
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

    public String getSub_id() {
        return sub_id;
    }

    public void setSub_id(String sub_id) {
        this.sub_id = sub_id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getOriginal_filename() {
        return original_filename;
    }

    public void setOriginal_filename(String original_filename) {
        this.original_filename = original_filename;
    }

    public int getPending() {
        return pending;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return "UploadResponse{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", sub_id='" + sub_id + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", original_filename='" + original_filename + '\'' +
                ", pending=" + pending +
                ", approved=" + approved +
                '}';
    }
}
