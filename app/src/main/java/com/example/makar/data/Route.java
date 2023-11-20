package com.example.makar.data;

import java.util.List;

public class Route {

    private final int totalTime;
    private final int transitCount;
    private final List<SubRouteItem> subRouteItems;

    public Route(int totalTime, int transitCount, List<SubRouteItem> subRouteItems) {
        this.totalTime = totalTime;
        this.transitCount = transitCount;
        this.subRouteItems = subRouteItems;
    }

    public List<SubRouteItem> getRouteItems() {
        return subRouteItems;
    }

    @Override
    public String toString() {
        return "Route{" +
                "totalTime=" + totalTime +
                ", transitCount=" + transitCount +
                ", subRouteItems=" + subRouteItems +
                '}';
    }
}

