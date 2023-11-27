package com.example.makar.data;

import java.util.List;

public class Route {
    private final int totalTime;
    private final int transitCount;
    private final List<SubRouteItem> subRouteItems;
    private final List<BriefStation> briefRoute;
    private final Station sourceStation;
    private final Station destinationStation;

    public Route(int totalTime, int transitCount, List<SubRouteItem> subRouteItems, List<BriefStation> briefRoute, Station sourceStation, Station destinationStation) {
        this.totalTime = totalTime;
        this.transitCount = transitCount;
        this.subRouteItems = subRouteItems;
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
        return subRouteItems;
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

    @Override
    public String toString() {
        return "Route{" +
                "totalTime=" + totalTime +
                ", transitCount=" + transitCount +
                ", subRouteItems=" + subRouteItems +
                ", briefRoute=" + briefRoute +
                '}';
    }
}

