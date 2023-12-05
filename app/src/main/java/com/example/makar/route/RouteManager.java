package com.example.makar.route;

import android.util.Log;

import com.example.makar.BuildConfig;
import com.example.makar.data.BriefStation;
import com.example.makar.data.Route;
import com.example.makar.data.RouteSearchResponse;
import com.example.makar.data.Station;
import com.example.makar.data.SubRoute;
import com.example.makar.data.SubRouteItem;
import com.example.makar.data.SubwayStation;
import com.example.makar.data.TransferInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteManager {

    private static final int DEFAULT_TRANSFER_TIME = 4; //기본 환승 소요시간
    public static String searchRoute(double sourceX, double sourceY, double destinationX, double destinationY) throws IOException {
        String apiKey = BuildConfig.ODSAY_API_KEY;
        if (apiKey == null) {
            Log.e("MAKAR", "api key null");
        }

        //대중교통 길찾기 api
        StringBuilder urlBuilder = new StringBuilder("https://api.odsay.com/v1/api/searchPubTransPathT");
        urlBuilder.append("?SX=" + URLEncoder.encode(String.valueOf(sourceX), "UTF-8"));
        urlBuilder.append("&SY=" + URLEncoder.encode(String.valueOf(sourceY), "UTF-8"));
        urlBuilder.append("&EX=" + URLEncoder.encode(String.valueOf(destinationX), "UTF-8"));
        urlBuilder.append("&EY=" + URLEncoder.encode(String.valueOf(destinationY), "UTF-8"));

        urlBuilder.append("&SearchPathType=" + URLEncoder.encode("1", "UTF-8")); //1:지하철
        urlBuilder.append("&apiKey=" + URLEncoder.encode(apiKey, "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb);
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    public static List<Route> parseRouteResponse(String jsonResponse, Station sourceStation, Station destinationStation) throws IOException, ExecutionException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        RouteSearchResponse result = objectMapper.readValue(jsonResponse, RouteSearchResponse.class);

        List<Route> routes = new ArrayList<>();
        List<RouteSearchResponse.Path> paths = result.getResult().getPath();

        //검색된 여러 경로 탐색
        for (RouteSearchResponse.Path path : paths) {
            RouteSearchResponse.Info pathInfo = path.getInfo();

            //1~9호선이 아닌 경로가 포함되어있는 경우 경로에서 제외
            if (isNotSubwayLineOneToNine(pathInfo.getMabObj())) {
                continue;
            }

            List<SubRouteItem> subRouteItems = new ArrayList<>();
            List<RouteSearchResponse.SubPath> subPaths = path.getSubPath();
            List<BriefStation> briefRoute = new ArrayList<>();

            //경로의 서브 경로 탐색
            for (int i = 0; i < subPaths.size(); i++) {

                RouteSearchResponse.SubPath subPath = subPaths.get(i);
                //도보타입일 경우는 skip -> api결과에 환승 소요시간이 안나옴
                if (subPath.isWalkType()) {
                    continue;
                }

                RouteSearchResponse.Lane lane = subPath.getLane().get(0);
                int lineNum = lane.getLineNum();
                int sectionTime = subPath.getSectionTime();
                String startStationName = subPath.getStartStationName();
                String endStationName = subPath.getEndStationName();
                int startStationCode = subPath.getStartID();
                int endStationCode = subPath.getEndID();
                int wayCode = subPath.getWayCode();

                SubRoute subRoute = new SubRoute(startStationName, endStationName, startStationCode,
                        endStationCode, lineNum, wayCode, sectionTime);
                briefRoute.add(new BriefStation(startStationName, lineNum));

                //마지막 서브 경로인 경우 도착역 정보도 추가
                if (i == pathInfo.getSubwayTransitCount() - 1) {
                    briefRoute.add(new BriefStation(endStationName, lineNum));
                }

                subRouteItems.add(new SubRouteItem(subRoute));
            }

            Route route = new Route(pathInfo.getTotalTime(), pathInfo.getSubwayTransitCount(), subRouteItems, briefRoute, sourceStation, destinationStation);

            //환승소요시간을 포함해서 전체소요시간 구하기
            setTransferTimeInRoute(route);
            routes.add(route);
        }

        //막차시간 구하기
        setMakarTimeInRoutes(routes);
        return routes;
    }



    private static boolean isNotSubwayLineOneToNine(String input) {

        // 정규식을 사용하여 숫자 1~9인지 확인
        Pattern pattern = Pattern.compile("^[1-9]$");

        // @로 섹션 나누기
        StringTokenizer sectionsTokenizer = new StringTokenizer(input, "@");
        while (sectionsTokenizer.hasMoreTokens()) {
            String section = sectionsTokenizer.nextToken();

            // :로 값을 나누기
            StringTokenizer valuesTokenizer = new StringTokenizer(section, ":");
            if (valuesTokenizer.hasMoreTokens()) {
                String firstValue = valuesTokenizer.nextToken();

                // 정규식을 사용하여 숫자 1~9인지 확인
                Matcher matcher = pattern.matcher(firstValue);
                if (!matcher.matches()) {
                    return true;
                }
            }
        }
        return false;
    }


    //환승소요시간을 포함해서 전체소요시간 구하기
    private static void setTransferTimeInRoute(Route route) {

        int totalTime = 0;
        List<SubRouteItem> routeItems = route.getRouteItems();
        int transitCount = route.getTransitCount();

        for (int i = 0; i < transitCount; i++) {
            SubRouteItem subRouteItem = routeItems.get(i);
            SubRoute currentSubRoute = subRouteItem.getSubRoute();
            totalTime += currentSubRoute.getSectionTime();

            if (i + 1 < transitCount) {
                SubRoute nextSubRoute = routeItems.get(i + 1).getSubRoute();
                CompletableFuture<TransferInfo> transferInfoFuture = searchTransferInfoAsync(currentSubRoute, nextSubRoute);

                TransferInfo transferInfo;
                try {
                    transferInfo = transferInfoFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    // 에러 시 기본 환승 소요시간
                    transferInfo = new TransferInfo(currentSubRoute.getLineNum(), nextSubRoute.getLineNum(), currentSubRoute.getEndStationName(), DEFAULT_TRANSFER_TIME);
                }

                subRouteItem.setTransferInfo(transferInfo);
                totalTime += transferInfo.getTransferTime();
            }
        }
        route.setTotalTime(totalTime);
    }

    //막차시간 구하기
    private static void setMakarTimeInRoutes(List<Route> routes) throws IOException, ExecutionException, InterruptedException {
        for (Route route : routes) {

            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            List<SubRouteItem> routeItems = route.getRouteItems();
            Calendar takingTime = null;
            //경로를 거꾸로 순회하면서 막차시간 구하기
            for (int i = routeItems.size() - 1; i >= 0; i--) {

                int sectionTime = 0;

                SubRouteItem subRouteItem = routeItems.get(i);
                SubRoute lastSubPath = subRouteItem.getSubRoute();

                //startStation의 지하철 시간표 호출하기
                String responseSubwaySchedule = SubwaySchedulerManager.requestSubwaySchedule(lastSubPath.getStartStationCode(), lastSubPath.getWayCode());
                SubwayStation subwayStation = SubwaySchedulerManager.parseSubwayScheduleResponse(responseSubwaySchedule, lastSubPath.getStartStationCode(), lastSubPath.getWayCode());

                //마지막 서브 경로 or 단일 서브 경로인 경우
                if (i == routeItems.size() - 1) {
                    takingTime = MarkarManager.computeLastMakarTime(dayOfWeek, subwayStation, lastSubPath.getLineNum(), lastSubPath.getWayCode(), lastSubPath.getStartStationCode(), lastSubPath.getEndStationCode());
                    System.out.println("last makar" + takingTime.getTime());
                }
                else {
                    sectionTime += subRouteItem.getSubRoute().getSectionTime();
                    sectionTime += subRouteItem.getTransferInfo().getTransferTime();

                    //환승시간 포함해서 걸린시간을 이후 서브 경로의 막차시간에서 빼준다.
                    System.out.println("minus minute" + (-sectionTime));
                    takingTime.add(Calendar.MINUTE, -sectionTime);
                    System.out.println("뺀 결과 " + takingTime.getTime());
                    //해당시간 이전보다 빠른 막차시간을 구한다.
                    takingTime = MarkarManager.computeTransferMakarTime(takingTime, dayOfWeek, subwayStation, lastSubPath.getLineNum(), lastSubPath.getWayCode(), lastSubPath.getStartStationCode(), lastSubPath.getEndStationCode());
                    Log.d( "makar", "환승막차 " + takingTime.getTime());
                }

            }
            route.setMakarTime(takingTime.getTime());
        }
    }



    private static CompletableFuture<TransferInfo> searchTransferInfoAsync(SubRoute currentSubRoute, SubRoute nextSubRoute) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        int fromStationID = currentSubRoute.getEndStationCode();
        int toStationID = nextSubRoute.getStartStationCode();
        CompletableFuture<TransferInfo> future = new CompletableFuture<>();

        System.out.println("from " + fromStationID + " to " + toStationID);
        firebaseFirestore.collection("transfer")
                .whereEqualTo("fromStationID", fromStationID)
                .whereEqualTo("toStationID", toStationID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            DocumentSnapshot documentSnapshot = result.getDocuments().get(0);
                            int fromLine = ((Long) documentSnapshot.get("fromLine")).intValue();
                            int toLine = ((Long) documentSnapshot.get("toLine")).intValue();
                            String odsayStationName = (String) documentSnapshot.get("odsayStationName");
                            int time = ((Long) documentSnapshot.get("time")).intValue();

                            TransferInfo transferInfo = new TransferInfo(fromLine, toLine, odsayStationName, time);
                            future.complete(transferInfo);
                        } else {
                            // 조회결과가 없을 경우 기본 환승소요시간으로 생성
                            TransferInfo defaultTransferInfo = new TransferInfo(currentSubRoute.getLineNum(),
                                    nextSubRoute.getLineNum(), currentSubRoute.getEndStationName(), DEFAULT_TRANSFER_TIME);
                            future.complete(defaultTransferInfo);
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }


}
