package com.example.makar.data;

public class BriefStation {
    private final String stationName;
    private final int lineNum;

    public BriefStation(String stationName, int lineNum) {
        this.stationName = stationName;
        this.lineNum = lineNum;
    }

    public String getStationName() {
        return stationName;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public String toString() {
        return "BriefStation{" +
                "stationName='" + stationName + '\'' +
                ", lineNum=" + lineNum +
                '}';
    }
}
