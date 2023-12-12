package com.example.makar.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsayStationData {

    @JsonProperty("result")
    private Result result;

    public Result getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "OdsayStationData{" +
                "result=" + result +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("station")
        private List<Station> station;

        public List<Station> getStation() {
            return station;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "station=" + station +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Station {

        @JsonProperty("stationName")
        private String stationName;

        @JsonProperty("stationID")
        private int stationID;

        @JsonProperty("x")
        private double x;

        @JsonProperty("y")
        private double y;

        @JsonProperty("CID")
        private int CID;

        @JsonProperty("arsID")
        private String arsID;

        @JsonProperty("do")
        private String doValue;

        @JsonProperty("gu")
        private String gu;

        @JsonProperty("dong")
        private String dong;

        @JsonProperty("type") //지하철 노선 번호
        private int type;

        @JsonProperty("laneName")
        private String laneName;

        @JsonProperty("laneCity")
        private String laneCity;

        @JsonProperty("ebid")
        private String ebid;

        public String getStationName() {
            return stationName;
        }

        public int getStationID() {
            return stationID;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public int getCID() {
            return CID;
        }

        public String getArsID() {
            return arsID;
        }

        public String getDoValue() {
            return doValue;
        }

        public String getGu() {
            return gu;
        }

        public String getDong() {
            return dong;
        }

        public int getType() {
            return type;
        }

        public String getLaneName() {
            return laneName;
        }

        public String getLaneCity() {
            return laneCity;
        }

        public String getEbid() {
            return ebid;
        }

        @Override
        public String toString() {
            return "Station{" +
                    "stationName='" + stationName + '\'' +
                    ", stationID=" + stationID +
                    ", x=" + x +
                    ", y=" + y +
                    ", CID=" + CID +
                    ", arsID='" + arsID + '\'' +
                    ", doValue='" + doValue + '\'' +
                    ", gu='" + gu + '\'' +
                    ", dong='" + dong + '\'' +
                    ", type=" + type +
                    ", laneName='" + laneName + '\'' +
                    ", laneCity='" + laneCity + '\'' +
                    ", ebid='" + ebid + '\'' +
                    '}';
        }
    }
}
