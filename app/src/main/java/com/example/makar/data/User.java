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
    int makarAlarmTime = 10;
    int getOffAlarmTime = 10;
    Route selectedRoute;
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

    public Route getSelectedRoute() {
        return selectedRoute;
    }

    public int getMakarAlarmTime() {
        return makarAlarmTime;
    }

    public int getGetOffAlarmTime() {
        return getOffAlarmTime;
    }

    public User(String userUId) {
        this.userUId = userUId;
    }

    public void setFavoriteStation(Station homeStation, Station schoolStation) {
        this.homeStation = homeStation;
        this.schoolStation = schoolStation;
    }

    public void setRouteStation(Station sourceStation, Station destinationStation) {
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
    }

    public void setSelectedRoute(Route selectedRoute) {
        this.selectedRoute = selectedRoute;
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

    public void setMakarAlarmTime(int makarAlarmTime) {
        this.makarAlarmTime = makarAlarmTime;
    }

    public void setGetOffAlarmTime(int getOffAlarmTime) {
        this.getOffAlarmTime = getOffAlarmTime;
    }

    public void setFavoriteRouteArr(List<Route> favoriteRouteArr) {
        this.favoriteRouteArr = favoriteRouteArr;
    }

    public void setRecentRouteArr(List<Route> recentRouteArr) {
        this.recentRouteArr = recentRouteArr;
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