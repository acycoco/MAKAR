package com.example.makar.data;

public class Station {
    private String stationName;
    private String stationCode;
    private String lineNum;
    private String railOpr;

    public Station() {
    }

    public Station(String stationName, String stationCode, String lineNum, String railOpr) {
        this.stationName = stationName;
        this.stationCode = stationCode;
        this.lineNum = lineNum;
        this.railOpr = railOpr;
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
}

