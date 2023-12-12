package com.example.makar.data.route;

import com.example.makar.data.Station;

import java.util.Date;
import java.util.List;

public class Route {

    private Date makarTime; //막차 시간
    private int totalTime;
    private int transitCount;
    private List<SubRouteItem> routeItems;
    private List<BriefStation> briefRoute;
    private Station sourceStation;
    private Station destinationStation;

    public Route() {
    }
    public Route(int totalTime, int transitCount, List<SubRouteItem> routeItems, List<BriefStation> briefRoute, Station sourceStation, Station destinationStation) {
        this.totalTime = totalTime;
        this.transitCount = transitCount;
        this.routeItems = routeItems;
        this.briefRoute = briefRoute;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getTransitCount() {
        return transitCount;
    }

    public List<SubRouteItem> getRouteItems() {
        return routeItems;
    }

    public List<BriefStation> getBriefRoute() {
        return briefRoute;
    }

    public Station getSourceStation() {
        return sourceStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }
    public Date getMakarTime() {
        return makarTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public void setMakarTime(Date makarTime) {
        this.makarTime = makarTime;
    }

    @Override
    public String toString() {
        return "Route{" +
                "makarTime=" + makarTime +
                ", totalTime=" + totalTime +
                ", transitCount=" + transitCount +
                ", routeItems=" + routeItems +
                ", briefRoute=" + briefRoute +
                ", sourceStation=" + sourceStation +
                ", destinationStation=" + destinationStation +
                '}';
    }
}

