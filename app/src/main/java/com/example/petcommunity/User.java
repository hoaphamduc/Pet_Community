package com.example.petcommunity;

public class User {
    private String username;
    private String dateOfBirth;
    private String address;
    private String currentCity;
    private String workplace;
    private String gender;
    private String avatarUrl;
    private String uid;

    // Empty constructor required for Firebase
    public User() {
    }
    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }
    public User(String username, String dateOfBirth, String address, String currentCity, String workplace, String gender, String avatarUrl) {
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.currentCity = currentCity;
        this.workplace = workplace;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
    }

    // Getter and Setter methods

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
