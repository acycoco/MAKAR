package com.example.makar.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TimeInfo {
    private String terminalStation;

    private int minute;

    public TimeInfo(String terminalStation, int minute) {
        this.terminalStation = terminalStation;
        this.minute = minute;
    }

    public static List<TimeInfo> parseTimeString(String timeString) {
        List<TimeInfo> timeInfoList = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(timeString, " ");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String[] parts = token.split("\\(");
            String stationInfo = parts[1].replace(")", "");

            //보통 시간(종착역)으로 종착역을 표시함
            //6호선의 경우 시간(종착역-다음 정차역)으로 종착역을 표시하는 경우를 위한 처리
            String[] stationParts = stationInfo.split("-");
            String terminalStation;
            if (stationParts.length > 1) {
                terminalStation = stationParts[0];
            } else {
                terminalStation = stationInfo;
            }

            int minute = Integer.parseInt(parts[0]);

            TimeInfo timeInfo = new TimeInfo(terminalStation, minute);
            timeInfoList.add(timeInfo);
        }

        return timeInfoList;
    }


    public String getTerminalStation() {
        return terminalStation;
    }


    public int getMinute() {
        return minute;
    }

    @Override
    public String toString() {
        return "TimeInfo{" +
                "terminalStation='" + terminalStation + '\'' +
                ", minute=" + minute +
                '}';
    }
}
