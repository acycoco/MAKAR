package com.example.makar.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubwaySchedule {

    String stationName;
    int stationID;
    int type;
    String laneName;
    String laneCity;
    String upWay;
    String downWay;

    @JsonProperty("OrdList")
    OrdList OrdList;

    @JsonProperty("SatList")
    OrdList satList;

    @JsonProperty("SunList")
    OrdList sunList;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLaneName() {
        return laneName;
    }

    public void setLaneName(String laneName) {
        this.laneName = laneName;
    }

    public String getLaneCity() {
        return laneCity;
    }

    public void setLaneCity(String laneCity) {
        this.laneCity = laneCity;
    }

    public String getUpWay() {
        return upWay;
    }

    public void setUpWay(String upWay) {
        this.upWay = upWay;
    }

    public String getDownWay() {
        return downWay;
    }

    public void setDownWay(String downWay) {
        this.downWay = downWay;
    }

    public OrdList getOrdList() {
        return OrdList;
    }

    public void setOrdList(OrdList ordList) {
        this.OrdList = ordList;
    }

    public SubwaySchedule.OrdList getSatList() {
        return satList;
    }

    public void setSatList(SubwaySchedule.OrdList satList) {
        this.satList = satList;
    }

    public SubwaySchedule.OrdList getSunList() {
        return sunList;
    }

    public void setSunList(SubwaySchedule.OrdList sunList) {
        this.sunList = sunList;
    }

    @Override
    public String toString() {
        return "SubwayStation{" +
                "stationName='" + stationName + '\'' +
                ", stationID=" + stationID +
                ", type=" + type +
                ", laneName='" + laneName + '\'' +
                ", laneCity='" + laneCity + '\'' +
                ", upWay='" + upWay + '\'' +
                ", downWay='" + downWay + '\'' +
                ", OrdList=" + OrdList +
                ", satList=" + satList +
                ", sunList=" + sunList +
                '}';
    }

    public static class OrdList {

        TimeDirection up;
        TimeDirection down;

        public TimeDirection getUp() {
            return up;
        }

        public void setUp(TimeDirection up) {
            this.up = up;
        }

        public TimeDirection getDown() {
            return down;
        }

        public void setDown(TimeDirection down) {
            this.down = down;
        }

        @Override
        public String toString() {
            return "OrdList{" +
                    "up=" + up +
                    ", down=" + down +
                    '}';
        }

        public static class TimeDirection {
            List<TimeData> time;


            public int getSize() {
                return time.size();
            }
            public List<TimeData> getTime() {
                return time;
            }

            public void setTime(List<TimeData> time) {
                this.time = time;
            }

            @Override
            public String toString() {
                return "TimeDirection{" +
                        "time=" + time +
                        '}';
            }

            public static class TimeData {

                @JsonProperty("Idx")
                int idx;
                String list;
                String expList;
                String expSPList;

                public TimeData() {
                }

                public int getIdx() {
                    return idx;
                }

                public void setIdx(int idx) {
                    this.idx = idx;
                }

                public String getList() {
                    return list;
                }

                public void setList(String list) {
                    this.list = list;
                }

                public String getExpList() {
                    return expList;
                }

                public void setExpList(String expList) {
                    this.expList = expList;
                }

                public String getExpSPList() {
                    return expSPList;
                }

                public void setExpSPList(String expSPList) {
                    this.expSPList = expSPList;
                }

                @Override
                public String toString() {
                    return "TimeData{" +
                            "idx=" + idx +
                            ", list='" + list + '\'' +
                            ", expList='" + expList + '\'' +
                            ", expSPList='" + expSPList + '\'' +
                            '}';
                }
            }
        }
    }
}