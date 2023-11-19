package com.example.makar.data;

public class RouteItem {

    private final int lineId; //TODO 나중에 Station형으로 바꿔야됨
    private final String stationName; //TODO stationLines랑 합칠 예정
    private final int wayCode; //1: 상행  2: 하행


    public RouteItem(int lineId, String stationName, int wayCode) {
        this.lineId = lineId;
        this.stationName = stationName;
        this.wayCode = wayCode;
    }

    public int getLineId() {
        return lineId;
    }

    public String getStationName() {
        return stationName;
    }

    public int getWayCode() {
        return wayCode;
    }



    @Override
    public String toString() {
        return "RouteItem{" +
                "lineId=" + lineId +
                ", stationName='" + stationName + '\'' +
                ", wayCode=" + wayCode +
                '}';
    }
}
