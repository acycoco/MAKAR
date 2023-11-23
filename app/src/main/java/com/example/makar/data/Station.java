package com.example.makar.data;

public class Station {

    private String stationName;
    private String stationCode;
    private String lineNum;
    private String railOpr;
    private String odsayStationName;
    private int odsayStationID;
    private double x;
    private double y;
    private int odsayLaneType;

    public Station() {
    }

    public Station(String stationName, String stationCode, String lineNum, String railOpr) {
        this.stationName = stationName;
        this.stationCode = stationCode;
        this.lineNum = lineNum;
        this.railOpr = railOpr;
    }

    public Station(String stationName, String stationCode, String lineNum, String railOpr, String odsayStationName, int odsayStationID, double x, double y, int odsayLaneType) {
        this.stationName = stationName;
        this.stationCode = stationCode;
        this.lineNum = lineNum;
        this.railOpr = railOpr;
        this.odsayStationName = odsayStationName;
        this.odsayStationID = odsayStationID;
        this.x = x;
        this.y = y;
        this.odsayLaneType = odsayLaneType;
    }

    public String getStationName() {
        return stationName;
    }

    public String getStationCode() {
        return stationCode;
    }

    public String getLineNum() {
        return lineNum;
    }

    public String getRailOpr() {
        return railOpr;
    }

    public String getOdsayStationName() {
        return odsayStationName;
    }

    public int getOdsayStationID() {
        return odsayStationID;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getOdsayLaneType() {
        return odsayLaneType;
    }

    @Override
    public String toString() {
        return "Station{" +
                "stationName='" + stationName + '\'' +
                ", stationCode='" + stationCode + '\'' +
                ", lineNum='" + lineNum + '\'' +
                ", railOpr='" + railOpr + '\'' +
                ", odsayStationName='" + odsayStationName + '\'' +
                ", odsayStationID=" + odsayStationID +
                ", x=" + x +
                ", y=" + y +
                ", odsayLaneType=" + odsayLaneType +
                '}';
    }
}

