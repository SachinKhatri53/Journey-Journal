package com.ismt.journeyjournal.model;

public class JourneyModel {
    String title, description, image, date, location, findLatitude, findLongitude;

    public JourneyModel() {
    }

    public JourneyModel(String title, String description, String image, String date, String location, String findLatitude, String findLongitude) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.date = date;
        this.location = location;
        this.findLatitude = findLatitude;
        this.findLongitude = findLongitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFindLatitude() {
        return findLatitude;
    }

    public void setFindLatitude(String findLatitude) {
        this.findLatitude = findLatitude;
    }

    public String getFindLongitude() {
        return findLongitude;
    }

    public void setFindLongitude(String findLongitude) {
        this.findLongitude = findLongitude;
    }
}
