package com.example.makar.data.route;


public class SubRoute {

    private String startStationName;
    private String endStationName;
    private int startStationCode;
    private int endStationCode;
    private int lineNum;
    private int wayCode;
    private int sectionTime;

    public SubRoute(String startStationName, String endStationName, int startStationCode, int endStationCode, int lineNum, int wayCode, int sectionTime) {
        this.startStationName = startStationName;
        this.endStationName = endStationName;
        this.startStationCode = startStationCode;
        this.endStationCode = endStationCode;
        this.lineNum = lineNum;
        this.wayCode = wayCode;
        this.sectionTime = sectionTime;
    }

    public SubRoute(){

    }

    public String getStartStationName() {
        return startStationName;
    }

    public String getEndStationName() {
        return endStationName;
    }

    public int getStartStationCode() {
        return startStationCode;
    }

    public int getEndStationCode() {
        return endStationCode;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getWayCode() {
        return wayCode;
    }

    public int getSectionTime() {
        return sectionTime;
    }

    @Override
    public String toString() {
        return "SubRoute{" +
                "startStationName='" + startStationName + '\'' +
                ", endStationName='" + endStationName + '\'' +
                ", startStationCode=" + startStationCode +
                ", endStationCode=" + endStationCode +
                ", lineNum=" + lineNum +
                ", wayCode=" + wayCode +
                ", sectionTime=" + sectionTime +
                '}';
    }

    public String lineNumToString() {
        return lineNum + "호선";
    }
}