package com.example.makar.data;

public class Station {

    private String cleanStationName;
    private String stationName;
    private String stationCode;
    private String lineNum;
    private String railOpr;
    private OdsayStation odsayStation;
    public Station() {
    }

    public Station(String stationName, String stationCode, String lineNum, String railOpr) {
        this.stationName = stationName;
        this.stationCode = stationCode;
        this.lineNum = lineNum;
        this.railOpr = railOpr;
    }

    public String getCleanStationName() {
        return cleanStationName;
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

    public OdsayStation getOdsayStation() {
        return odsayStation;
    }

    public void setOdsayStation(OdsayStation odsayStation) {
        this.odsayStation = odsayStation;
    }

    @Override
    public String toString() {
        return "Station{" +
                "cleanStationName='" + cleanStationName + '\'' +
                ", stationName='" + stationName + '\'' +
                ", stationCode='" + stationCode + '\'' +
                ", lineNum='" + lineNum + '\'' +
                ", railOpr='" + railOpr + '\'' +
                ", odsayStation=" + odsayStation +
                '}';
    }
}

