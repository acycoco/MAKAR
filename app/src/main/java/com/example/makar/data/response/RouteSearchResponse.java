package com.example.makar.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteSearchResponse {
    //json응답 데이터를 파싱하기 위한 객체

    @JsonProperty("result")
    private Result result;

    public Result getResult() {
        return result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("subwayCount")
        private int totalRouteCount;

        @JsonProperty("path")
        private List<Path> path;

        public int getTotalRouteCount() {
            return totalRouteCount;
        }

        public List<Path> getPath() {
            return path;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Path {

        @JsonProperty("info")
        private Info info;

        @JsonProperty("subPath")
        private List<SubPath> subPath;

        public Info getInfo() {
            return info;
        }

        public List<SubPath> getSubPath() {
            return subPath;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Info {
        @JsonProperty("totalTime")
        private int totalTime;

        @JsonProperty("mapObj")
        private String mabObj;

        @JsonProperty("subwayTransitCount")
        private int subwayTransitCount;


        public int getTotalTime() {
            return totalTime;
        }

        public String getMabObj() {
            return mabObj;
        }

        public int getSubwayTransitCount() {
            return subwayTransitCount;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubPath {
        @JsonProperty("trafficType")
        private int trafficType;  //1:지하철 //2:버스

        @JsonProperty("sectionTime")
        private int sectionTime; //구간 소요시간

        @JsonProperty("lane")
        private List<Lane> lane;

        @JsonProperty("startName")
        private String startStationName;

        @JsonProperty("endName")
        private String endStationName;

        @JsonProperty("wayCode")
        private int wayCode; //1: 상행, 2: 하행

        @JsonProperty("startID")
        private int startID;

        @JsonProperty("endID")
        private int endID;

        public boolean isWalkType() {
            return trafficType == 3;
        }
        public int getTrafficType() {
            return trafficType;
        }

        public int getSectionTime() {
            return sectionTime;
        }

        public List<Lane> getLane() {
            return lane;
        }

        public String getStartStationName() {
            return startStationName;
        }

        public String getEndStationName() {
            return endStationName;
        }

        public int getWayCode() {
            return wayCode;
        }

        public int getStartID() {
            return startID;
        }

        public int getEndID() {
            return endID;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lane {
        @JsonProperty("name")
        private String name;

        @JsonProperty("subwayCode")
        private int lineNum;

        public String getName() {
            return name;
        }

        public int getLineNum() {
            return lineNum;
        }
    }

}
