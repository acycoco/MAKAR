package com.example.makar.data;

public class User {
    String userUId;
    Station homeStation;
    Station schoolStation;
    Station departureStation;
    Station destinationStation;

    public User() {
    }

    public String getUserUId() {
        return userUId;
    }

    public Station getHomeStation() {
        return homeStation;
    }

    public Station getSchoolStation() {
        return schoolStation;
    }

    public Station getDepartureStation() {
        return departureStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public User(String userUId, Station homeStation, Station schoolStation, Station departureStation, Station destinationStation) {
        this.userUId = userUId;
        this.homeStation = homeStation;
        this.schoolStation = schoolStation;
        this.departureStation = departureStation;
        this.destinationStation = destinationStation;
    }

    public void setHomeStation(Station homeStation) {
        this.homeStation = homeStation;
    }

    public void setSchoolStation(Station schoolStation) {
        this.schoolStation = schoolStation;
    }

    public void setDepartureStation(Station departureStation) {
        this.departureStation = departureStation;
    }

    public void setDestinationStation(Station destinationStation) {
        this.destinationStation = destinationStation;
    }

    public void setUserUId(String userUId) {
        this.userUId = userUId;
    }
}
