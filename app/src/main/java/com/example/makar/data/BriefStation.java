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

    public String getLineNumToString() {
        if (lineNum == 101) {
            return "공항철도";
        } else if (lineNum == 104) {
            return "경의중앙";
        } else {
            return lineNum + "호선";
        }
    }

    public String briefToStationName() {
        if (stationName.equals("서울역")) {
            return stationName;
        } else {
            return stationName + "역";
        }
    }
}
