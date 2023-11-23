package com.example.makar.data;

public class BriefStatoion {
    private final String stationName;
    private final int lineNum;

    public BriefStatoion(String stationName, int lineNum) {
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
        return "BriefStatoion{" +
                "stationName='" + stationName + '\'' +
                ", lineNum=" + lineNum +
                '}';
    }
}
