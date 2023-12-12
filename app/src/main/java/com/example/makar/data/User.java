package com.example.makar.data;

import com.example.makar.data.route.Route;

public class User {
    String userUId;
    Station homeStation;
    Station schoolStation;
    Station sourceStation;
    Station destinationStation;
    int makarAlarmTime = 10;
    int getOffAlarmTime = 10;
    Route selectedRoute;
    Route favoriteRoute1;
    Route favoriteRoute2;
    Route favoriteRoute3;
    Route recentRoute1;
    Route recentRoute2;
    Route recentRoute3;

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

    public void setUserUId(String userUId) {
        this.userUId = userUId;
    }

    public void setMakarAlarmTime(int makarAlarmTime) {
        this.makarAlarmTime = makarAlarmTime;
    }

    public void setGetOffAlarmTime(int getOffAlarmTime) {
        this.getOffAlarmTime = getOffAlarmTime;
    }

    public void setFavoriteRoute1(Route favoriteRoute1) {
        this.favoriteRoute1 = favoriteRoute1;
    }

    public void setFavoriteRoute2(Route favoriteRoute2) {
        this.favoriteRoute2 = favoriteRoute2;
    }

    public void setFavoriteRoute3(Route favoriteRoute3) {
        this.favoriteRoute3 = favoriteRoute3;
    }

    public void setRecentRoute1(Route recentRoute1) {
        this.recentRoute1 = recentRoute1;
    }

    public void setRecentRoute2(Route recentRoute2) {
        this.recentRoute2 = recentRoute2;
    }

    public void setRecentRoute3(Route recentRoute3) {
        this.recentRoute3 = recentRoute3;
    }

    public Route getFavoriteRoute1() {
        return favoriteRoute1;
    }

    public Route getFavoriteRoute2() {
        return favoriteRoute2;
    }

    public Route getFavoriteRoute3() {
        return favoriteRoute3;
    }

    public Route getRecentRoute1() {
        return recentRoute1;
    }

    public Route getRecentRoute2() {
        return recentRoute2;
    }

    public Route getRecentRoute3() {
        return recentRoute3;
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