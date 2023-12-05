package com.example.makar.route;

import android.util.Log;

import com.example.makar.TimeInfo;
import com.example.makar.data.SubwayStation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MarkarManager {
    private static final int UPTOWN = 1;  //상행
    private static final int DOWNTOWN = 2; //하행


    public static Calendar computeLastMakarTime(int dayOfWeek, SubwayStation subwayStation, int odsayLaneType, int wayCode, int startStationID, int endStationID) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        SubwayStation.OrdList ordList = getOrdListByDayOfWeek(dayOfWeek, subwayStation);

        List<SubwayStation.OrdList.TimeDirection.TimeData> time = getTimeByWayCode(wayCode, ordList);

        //매시간마다
        for (int i = time.size() - 1; i >= 0; i--) {
            SubwayStation.OrdList.TimeDirection.TimeData timeData = time.get(i);
            List<TimeInfo> timeInfos = TimeInfo.parseTimeString(timeData.getList());

            AtomicBoolean canGoInSubway = new AtomicBoolean(false);
            List<CompletableFuture<Void>> tasks = new ArrayList<>();

            AtomicReference<TimeInfo> result = new AtomicReference<>();
            //분마다
            for (int j = timeInfos.size() - 1; j >= 0; j--) {
                TimeInfo timeInfo = timeInfos.get(j);

                //해당 호선의 지하철 노선도 데이터 가져오기
                CompletableFuture<Void> task = new CompletableFuture<>();
                firebaseFirestore.collection("line_sequence")
                        .whereEqualTo("odsayLaneType", odsayLaneType)
                        .get()
                        .addOnCompleteListener(taskSnapshot -> {
                            if (taskSnapshot.isSuccessful()) {
                                for (QueryDocumentSnapshot document : taskSnapshot.getResult()) {

                                    int startIndex = -1;
                                    int endIndex = -1;
                                    int terminalIndex = -1;

                                    List<Map<String, Object>> stationList = new ArrayList<>();
                                    if (wayCode == UPTOWN) {
                                        stationList = (List<Map<String, Object>>) document.get(String.valueOf(UPTOWN));
                                    } else if (wayCode == DOWNTOWN) {
                                        stationList = (List<Map<String, Object>>) document.get(String.valueOf(DOWNTOWN));
                                    } else {
                                        Log.e("makar", "waycode invalid error");
                                    }

                                    //순회하면서 출발역, 도착역, 종착역의 index구하기
                                    for (int k = 0; k < stationList.size(); k++) {
                                        String stationName = (String) stationList.get(k).get("stationName");
                                        if (stationName.equals(timeInfo.getTerminalStation())) {
                                            terminalIndex = k;
                                        }

                                        int odsayStationID = ((Long) stationList.get(k).get("odsayLaneType")).intValue();
                                        if (odsayStationID == startStationID) {
                                            startIndex = k;
                                        }

                                        if (odsayStationID == endStationID) {
                                            endIndex = k;
                                        }
                                    }

                                    //index가 출발역, 도착역, 종착역순이면 해당 열차를 탈 수 있다.
                                    if (startIndex < endIndex && endIndex < terminalIndex) {

                                        System.out.println(timeInfo.getMinute() + "분에" + startIndex + "에서 시작해서 " + endIndex + "로끝나고 종착은 " + terminalIndex);
                                        canGoInSubway.set(true);
                                        result.set(timeInfo);
                                        task.complete(null);
                                        return;
                                    }
                                }
                            }
                            task.complete(null);
                        });
                tasks.add(task);
                CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
                try {
                    allOf.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (canGoInSubway.get()) {
                    //현재 날짜
                    Calendar calendar = Calendar.getInstance();

                    int hour = time.get(i).getIdx();
                    int minute = result.get().getMinute();
                    if (hour >= 24) { //지하철 시간표에서 시간이 24, 25인 경우
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        hour -= 24;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    return calendar;
                }

            }


        }
        return null;
    }

    public static Calendar computeTransferMakarTime(Calendar takingTime, int dayOfWeek, SubwayStation subwayStation, int odsayLaneType, int wayCode, int startStationID, int endStationID) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        System.out.println("takingTime" + takingTime.get(Calendar.HOUR_OF_DAY) + "시" +
                takingTime.get(Calendar.MINUTE) + "분 " +
                startStationID + " 에서 " + endStationID + "로 가는 막차시간 구하기");

        SubwayStation.OrdList ordList = getOrdListByDayOfWeek(dayOfWeek, subwayStation);
        List<SubwayStation.OrdList.TimeDirection.TimeData> time = getTimeByWayCode(wayCode, ordList);

        //매시간마다
        for (int i = time.size() - 1; i >= 0; i--) {

            SubwayStation.OrdList.TimeDirection.TimeData timeData = time.get(i);

            //타야하는 시간보다 hour이 크면 skip
            if (isBeforeTakingHour(timeData, takingTime)) {
                continue;
            }

            String list = timeData.getList();
            List<TimeInfo> timeInfos = TimeInfo.parseTimeString(list);

            AtomicBoolean canGoInSubway = new AtomicBoolean(false);
            List<CompletableFuture<Void>> tasks = new ArrayList<>();

            AtomicReference<TimeInfo> result = new AtomicReference<>();
            //분마다
            for (int j = timeInfos.size() - 1; j >= 0; j--) {
                TimeInfo timeInfo = timeInfos.get(j);

                if (isBeforeTakingTime(timeData, timeInfo, takingTime)) {
                    continue;
                }

                //해당 호선의 지하철 노선도 데이터 가져오기
                CompletableFuture<Void> task = new CompletableFuture<>();
                firebaseFirestore.collection("line_sequence")
                        .whereEqualTo("odsayLaneType", odsayLaneType)
                        .get()
                        .addOnCompleteListener(taskSnapshot -> {
                            if (taskSnapshot.isSuccessful()) {
                                for (QueryDocumentSnapshot document : taskSnapshot.getResult()) {

                                    System.out.println(timeInfo.getMinute());
                                    int startIndex = -1;
                                    int endIndex = -1;
                                    int terminalIndex = -1;

                                    List<Map<String, Object>> stationList = new ArrayList<>();
                                    if (wayCode == UPTOWN) {
                                        stationList = (List<Map<String, Object>>) document.get(String.valueOf(UPTOWN));
                                    } else if (wayCode == DOWNTOWN) {
                                        stationList = (List<Map<String, Object>>) document.get(String.valueOf(DOWNTOWN));
                                    } else {
                                        Log.e("makar", "waycode invalid error");
                                    }

                                    //순회하면서 출발역, 도착역, 종착역의 index구하기
                                    for (int k = 0; k < stationList.size(); k++) {
                                        String stationName = (String) stationList.get(k).get("stationName");
                                        if (stationName.equals(timeInfo.getTerminalStation())) {
                                            terminalIndex = k;
                                        }

                                        int odsayStationID = ((Long) stationList.get(k).get("odsayLaneType")).intValue();
                                        if (odsayStationID == startStationID) {
                                            startIndex = k;
                                        }

                                        if (odsayStationID == endStationID) {
                                            endIndex = k;
                                        }
                                    }

                                    //index가 출발역, 도착역, 종착역순이면 해당 열차를 탈 수 있다.
                                    if (startIndex < endIndex && endIndex < terminalIndex) {

                                        System.out.println(timeInfo.getMinute() + "분에" + startIndex + "에서 시작해서 " + endIndex + "로끝나고 종착은 " + terminalIndex);
                                        canGoInSubway.set(true);
                                        result.set(timeInfo);
                                        task.complete(null);
                                        return;
                                    }
                                }
                            }
                            task.complete(null);
                        });
                tasks.add(task);
                CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
                try {
                    allOf.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (canGoInSubway.get()) {
                    Calendar calendar = Calendar.getInstance();

                    int hour = time.get(i).getIdx();
                    int minute = result.get().getMinute();
                    if (hour >= 24) { //지하철 시간표에서 시간이 24, 25인 경우
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        hour -= 24;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    return calendar;

                }
            }
        }
        return null;
    }


    //요일에 맞는 시간표 가져오기
    private static SubwayStation.OrdList getOrdListByDayOfWeek(int dayOfWeek, SubwayStation subwayStation) {
        if (dayOfWeek == Calendar.SATURDAY) {
            return subwayStation.getSatList();
        } else if (dayOfWeek == Calendar.SUNDAY) {
            return subwayStation.getSunList();
        } else {
            return subwayStation.getOrdList();
        }
    }

    //지하철 방면에 맞는 시간표 가져오기
    private static List<SubwayStation.OrdList.TimeDirection.TimeData> getTimeByWayCode(int wayCode, SubwayStation.OrdList ordList) {
        if (wayCode == UPTOWN) {
            return ordList.getUp().getTime();
        } else if (wayCode == DOWNTOWN) {
            return ordList.getDown().getTime();
        } else {
            throw new IllegalArgumentException("Invalid wayCode: " + wayCode);
        }
    }

    private static int getTakingHour(Calendar takingTime) {
        int takingHour = takingTime.get(Calendar.HOUR_OF_DAY);
        if (takingTime.get(Calendar.HOUR_OF_DAY) == 0) {
            takingHour = 24;
        } else if (takingTime.get(Calendar.HOUR_OF_DAY) == 1) {
            takingHour = 25;
        }
        return takingHour;
    }

    private static boolean isBeforeTakingHour(SubwayStation.OrdList.TimeDirection.TimeData timeData, Calendar takingTime) {
        return timeData.getIdx() > getTakingHour(takingTime);
    }

    private static boolean isBeforeTakingTime(SubwayStation.OrdList.TimeDirection.TimeData timeData, TimeInfo timeInfo, Calendar takingTime) {
        return (timeData.getIdx() == getTakingHour(takingTime) && timeInfo.getMinute() > takingTime.get(Calendar.MINUTE));
    }


}
