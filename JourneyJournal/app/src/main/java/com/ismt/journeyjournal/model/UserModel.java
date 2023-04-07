package com.ismt.journeyjournal.model;

public class UserModel {
    String username, email, password, uid;

    public UserModel() {
    }

    public UserModel(String username, String email, String password, String uid) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
