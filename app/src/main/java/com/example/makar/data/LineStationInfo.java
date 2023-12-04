package com.example.makar.data;

public class  LineStationInfo {

    private String stationName;
    private int odsayStationID ;

    public LineStationInfo(String stationName, int odsayStationID) {
        this.stationName = stationName;
        this.odsayStationID = odsayStationID;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getOdsayStationID() {
        return odsayStationID;
    }

    public void setOdsayStationID(int odsayStationID) {
        this.odsayStationID = odsayStationID;
    }

    @Override
    public String toString() {
        return "LineStationInfo{" +
                "stationName='" + stationName + '\'' +
                ", odsayStationID=" + odsayStationID +
                '}';
    }
}
