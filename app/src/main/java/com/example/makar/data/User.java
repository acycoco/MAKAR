package com.example.makar.data;

public class User {
    Station homeStation;
    Station schoolStation;
    Station depatureStation;
    Station destinationStation;

    public User() {
    }

    public Station getHomeStation() {
        return homeStation;
    }

    public Station getSchoolStation() {
        return schoolStation;
    }

    public Station getDepatureStation() {
        return depatureStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public User(Station homeStation, Station schoolStation, Station depatureStation, Station destinationStation) {
        this.homeStation = homeStation;
        this.schoolStation = schoolStation;
        this.depatureStation = depatureStation;
        this.destinationStation = destinationStation;
    }
}
