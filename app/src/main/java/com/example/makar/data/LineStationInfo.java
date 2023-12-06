package com.example.makar.data;

public class  LineStationInfo {

    private int odsayLaneType;
    private String stationName;

    public LineStationInfo(int odsayLaneType, String stationName) {
        this.odsayLaneType = odsayLaneType;
        this.stationName = stationName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getOdsayLaneType() {
        return odsayLaneType;
    }

    public void setOdsayLaneType(int odsayLaneType) {
        this.odsayLaneType = odsayLaneType;
    }

    @Override
    public String toString() {
        return "LineStationInfo{" +
                "odsayLaneType=" + odsayLaneType +
                ", stationName='" + stationName + '\'' +
                '}';
    }
}
