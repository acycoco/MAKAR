package com.example.makar.route;

import android.util.Log;

import com.example.makar.TimeInfo;
import com.example.makar.data.Route;
import com.example.makar.data.SubRoute;
import com.example.makar.data.SubRouteItem;
import com.example.makar.data.SubwaySchedule;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MakarManager {

    private static final int UPTOWN = 1;  //상행
    private static final int DOWNTOWN = 2; //하행

    private final ApiManager apiManager;

    public MakarManager(ApiManager apiManager) {
        this.apiManager = apiManager;
    }

    //경로 정보와 지하철 시간표를 활용하여 막차 시간을 계산한다.
    public Calendar computeMakarTime(Route route, int dayOfWeek) throws IOException, ExecutionException, InterruptedException {
        List<SubRouteItem> routeItems = route.getRouteItems();
        Calendar takingTime = null;

        List<Date> makarTimes = new ArrayList<>();
        // 경로를 거꾸로 순회하면서 막차 시간 구하기
        for (int i = routeItems.size() - 1; i >= 0; i--) {
            SubRouteItem subRouteItem = routeItems.get(i);
            SubRoute lastSubRoute = subRouteItem.getSubRoute();

            //startStation의 지하철 시간표 호출하기
            SubwaySchedule subwaySchedule = apiManager.requestSubwaySchedule(lastSubRoute.getStartStationCode(), lastSubRoute.getWayCode());

            //마지막 서브 경로 or 단일 서브 경로인 경우
            if (i == routeItems.size() - 1) {
                takingTime = computeLastMakarTime(dayOfWeek, subwaySchedule, lastSubRoute.getLineNum(), lastSubRoute.getWayCode(), lastSubRoute.getStartStationCode(), lastSubRoute.getEndStationCode());
            } else {
                int sectionTime = subRouteItem.getSubRoute().getSectionTime();
                sectionTime += subRouteItem.getTransferInfo().getTransferTime();
                takingTime.add(Calendar.MINUTE, -sectionTime);

                // 환승시간을 포함하여 이후 서브 경로의 막차 시간 구하기
                takingTime = computeTransferMakarTime(takingTime, dayOfWeek, subwaySchedule, lastSubRoute.getLineNum(), lastSubRoute.getWayCode(), lastSubRoute.getStartStationCode(), lastSubRoute.getEndStationCode());

            }
            Log.d("makar", "막차시간 계산 : " + lastSubRoute.getStartStationName() + "->" + lastSubRoute.getEndStationName()
                    + " " + lastSubRoute.getWayCode() + "방면 막차 " + takingTime.getTime());
            makarTimes.add(0, takingTime.getTime());
        }
        Log.d("makar", "막차시간 리스트 : " + makarTimes);

        return takingTime;
    }

    public Calendar computeLastMakarTime(int dayOfWeek, SubwaySchedule subwaySchedule, int odsayLaneType, int wayCode, int startStationID, int endStationID) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        SubwaySchedule.OrdList ordList = getOrdListByDayOfWeek(dayOfWeek, subwaySchedule);

        List<SubwaySchedule.OrdList.TimeDirection.TimeData> time = getTimeByWayCode(wayCode, ordList);

        //매시간마다
        for (int i = time.size() - 1; i >= 0; i--) {
            SubwaySchedule.OrdList.TimeDirection.TimeData timeData = time.get(i);
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

                                        Log.d("makar", timeInfo.getMinute() + "분에" + startIndex + "에서 시작해서 " + endIndex + "로끝나고 종착은 " + terminalIndex);
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
                    Calendar nowCalendar = Calendar.getInstance();
                    Calendar makarCalendar = Calendar.getInstance();

                    int makarHour = time.get(i).getIdx();
                    int makarMinute = result.get().getMinute();

                    int nowHour = nowCalendar.get(Calendar.HOUR_OF_DAY);
                    int nowMinute = nowCalendar.get(Calendar.MINUTE);

                    //막차시간이 시간표 상 24, 25인 경우
                    if (makarHour >= 24) {
                        makarHour -= 24;
                        //현재 시간이 오전 3시를 넘으면
                        if (nowHour >= 3) {
                            //하루를 더함
                            makarCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        //현재 시간이 오전 12시 ~ 오전 2시(새벽)면 하루를 안더함
                    }

                    makarCalendar.set(Calendar.HOUR_OF_DAY, makarHour);
                    makarCalendar.set(Calendar.MINUTE, makarMinute);
                    makarCalendar.set(Calendar.SECOND, 0);
                    makarCalendar.set(Calendar.MILLISECOND, 0);

                    //막차시간이 이미 지나간 경우 하루를 더함
                    if (nowCalendar.after(makarCalendar)) {
                        makarCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    return makarCalendar;
                }

            }


        }
        return null;
    }

    public Calendar computeTransferMakarTime(Calendar takingTime, int dayOfWeek, SubwaySchedule subwaySchedule, int odsayLaneType, int wayCode, int startStationID, int endStationID) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        SubwaySchedule.OrdList ordList = getOrdListByDayOfWeek(dayOfWeek, subwaySchedule);
        List<SubwaySchedule.OrdList.TimeDirection.TimeData> time = getTimeByWayCode(wayCode, ordList);

        //매시간마다
        for (int i = time.size() - 1; i >= 0; i--) {

            SubwaySchedule.OrdList.TimeDirection.TimeData timeData = time.get(i);

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
                    Calendar nowCalendar = Calendar.getInstance();
                    Calendar makarCalendar = Calendar.getInstance();

                    int makarHour = time.get(i).getIdx();
                    int makarMinute = result.get().getMinute();

                    int nowHour = nowCalendar.get(Calendar.HOUR_OF_DAY);
                    int nowMinute = nowCalendar.get(Calendar.MINUTE);

                    //막차시간이 시간표 상 24, 25인 경우
                    if (makarHour >= 24) {
                        makarHour -= 24;
                        //현재 시간이 오전 3시를 넘으면
                        if (nowHour >= 3) {
                            //하루를 더함
                            makarCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        //현재 시간이 오전 12시 ~ 오전 2시면 하루를 안더함
                    }

                    makarCalendar.set(Calendar.HOUR_OF_DAY, makarHour);
                    makarCalendar.set(Calendar.MINUTE, makarMinute);
                    makarCalendar.set(Calendar.SECOND, 0);
                    makarCalendar.set(Calendar.MILLISECOND, 0);

                    //막차시간이 이미 지나간 경우 하루를 더함
                    if (nowCalendar.after(makarCalendar)) {
                        makarCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    return makarCalendar;
                }

            }
        }
        return null;
    }


    //요일에 맞는 시간표 가져오기
    private SubwaySchedule.OrdList getOrdListByDayOfWeek(int dayOfWeek, SubwaySchedule subwaySchedule) {
        if (dayOfWeek == Calendar.SATURDAY) {
            return subwaySchedule.getSatList();
        } else if (dayOfWeek == Calendar.SUNDAY) {
            return subwaySchedule.getSunList();
        } else {
            return subwaySchedule.getOrdList();
        }
    }

    //지하철 방면에 맞는 시간표 가져오기
    private List<SubwaySchedule.OrdList.TimeDirection.TimeData> getTimeByWayCode(int wayCode, SubwaySchedule.OrdList ordList) {
        if (wayCode == UPTOWN) {
            return ordList.getUp().getTime();
        } else if (wayCode == DOWNTOWN) {
            return ordList.getDown().getTime();
        } else {
            throw new IllegalArgumentException("Invalid wayCode: " + wayCode);
        }
    }

    private int getTakingHour(Calendar takingTime) {
        int takingHour = takingTime.get(Calendar.HOUR_OF_DAY);
        if (takingTime.get(Calendar.HOUR_OF_DAY) == 0) {
            takingHour = 24;
        } else if (takingTime.get(Calendar.HOUR_OF_DAY) == 1) {
            takingHour = 25;
        }
        return takingHour;
    }

    private boolean isBeforeTakingHour(SubwaySchedule.OrdList.TimeDirection.TimeData timeData, Calendar takingTime) {
        return timeData.getIdx() > getTakingHour(takingTime);
    }

    private boolean isBeforeTakingTime(SubwaySchedule.OrdList.TimeDirection.TimeData timeData, TimeInfo timeInfo, Calendar takingTime) {
        return (timeData.getIdx() == getTakingHour(takingTime) && timeInfo.getMinute() > takingTime.get(Calendar.MINUTE));
    }


}
