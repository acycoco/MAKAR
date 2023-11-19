package com.example.makar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route {

    private final int totalTravelTime;
    private List<RouteItem> routeItems;
    private List<Integer> exWalkTimes;

    public Route(int totalTravelTime) {
        this.totalTravelTime = totalTravelTime;
        this.routeItems = new ArrayList<>();
        this.exWalkTimes = new ArrayList<>();
    }

    public Route(int totalTravelTime, List<RouteItem> routeItems, List<Integer> exWalkTimes) {
        this.totalTravelTime = totalTravelTime;
        this.routeItems = routeItems;
        this.exWalkTimes = exWalkTimes;
    }

    public void addRouteItem(RouteItem routeItem) {
        routeItems.add(routeItem);
    }

    public void addExWalkTime(int exWalkTime) {
        exWalkTimes.add(exWalkTime);
    }

    public List<RouteItem> getRouteItems() {
        return Collections.unmodifiableList(routeItems);
    }

    @Override
    public String toString() {
        return "Route{" +
                "totalTravelTime=" + totalTravelTime +
                ", routeItems=" + routeItems +
                ", exWalkTimes=" + exWalkTimes +
                '}';
    }
}
