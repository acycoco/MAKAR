package com.example.makar.data;


public class OdsayStation {
    private String stationName;

    private int stationID;

    private double x;

    private double y;

    private int cid;

    private String arsID;

    private String doValue;

    private String gu;

    private String dong;

    private int type;

    private String laneName;

    private String laneCity;

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

    public int getCid() {
        return cid;
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
                ", CID=" + cid +
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
