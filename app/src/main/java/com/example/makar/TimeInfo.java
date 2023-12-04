package com.example.makar;

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
            String terminalStation = parts[1].replace(")", "");
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
