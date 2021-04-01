package com.example.pawsitiveproject;

public class User {
    private String email;
    private String uid;
    private String status;

    public User () {}

    public User(String email) { this.email = email; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", uid='" + uid + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
