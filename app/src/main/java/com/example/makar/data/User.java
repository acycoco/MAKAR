package com.example.makar.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class User {
    String userUId;
    Station homeStation;
    Station schoolStation;
    Station sourceStation;
    Station destinationStation;

    List<Route> favoriteRouteArr = new ArrayList<>(); //즐겨찾는 경로
    List<Route> recentRouteArr = new ArrayList<>(3); //최근경로



    //TODO 하차 알림 시간, 막차 일림 시간 정보도 같이 저장

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

    public Station getSourceStation() {
        return sourceStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public User(String userUId, Station homeStation, Station schoolStation, Station sourceStation, Station destinationStation) {
        this.userUId = userUId;
        this.homeStation = homeStation;
        this.schoolStation = schoolStation;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
    }

    public void setHomeStation(Station homeStation) {
        this.homeStation = homeStation;
    }

    public void setSchoolStation(Station schoolStation) {
        this.schoolStation = schoolStation;
    }

    public void setSourceStation(Station sourceStation) {
        this.sourceStation = sourceStation;
    }

    public void setDestinationStation(Station destinationStation) {
        this.destinationStation = destinationStation;
    }

    public List<Route> getRecentRouteArr() {
        return recentRouteArr;
    }

    public List<Route> getFavoriteRouteArr() {
        return favoriteRouteArr;
    }

    public void setUserUId(String userUId) {
        this.userUId = userUId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userUId='" + userUId + '\'' +
                ", homeStation=" + homeStation +
                ", schoolStation=" + schoolStation +
                ", sourceStation=" + sourceStation +
                ", destinationStation=" + destinationStation +
                '}';
    }
}
