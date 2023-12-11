package com.example.makar.route;


import android.util.Log;

import com.example.makar.data.BriefStation;
import com.example.makar.data.Route;
import com.example.makar.data.RouteSearchResponse;
import com.example.makar.data.Station;
import com.example.makar.data.SubRoute;
import com.example.makar.data.SubRouteItem;
import com.example.makar.data.TransferInfo;

import java.io.IOException;
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

    private final ApiManager apiManager;
    private final MakarManager makarManager;
    private final TransferManager transferManager;


    public RouteManager(ApiManager apiManager, MakarManager makarManager, TransferManager transferManager) {
        this.apiManager = apiManager;
        this.makarManager = makarManager;
        this.transferManager = transferManager;
    }


    public List<Route> getRoutes(Station sourceStation, Station destinationStation) throws IOException, ExecutionException, InterruptedException {
        RouteSearchResponse routeSearchResponse = apiManager.requestRoute(sourceStation.getX(), sourceStation.getY(), destinationStation.getX(), destinationStation.getY());
        List<Route> routes = new ArrayList<>();
        List<RouteSearchResponse.Path> paths = routeSearchResponse.getResult().getPath();

        //검색된 여러 경로 탐색
        for (RouteSearchResponse.Path path : paths) {
            RouteSearchResponse.Info pathInfo = path.getInfo();

            //1~9호선이 아닌 경로가 포함되어있는 경우 경로에서 제외
            if (isNotSubwayLineOneToNine(pathInfo.getMabObj())) {
                continue;
            }

            List<SubRouteItem> subRouteItems = createSubRouteItems(path);
            List<BriefStation> briefRoute = createBriefRoute(path, subRouteItems);

            Route route = new Route(pathInfo.getTotalTime(), pathInfo.getSubwayTransitCount(), subRouteItems, briefRoute, sourceStation, destinationStation);

            //환승소요시간을 포함해서 전체소요시간 구하기
            setTransferTimeInRoute(route);
            routes.add(route);
        }

        //막차시간 구하기
        setMakarTimeInRoutes(routes);
        return routes;
    }

    private List<SubRouteItem> createSubRouteItems(RouteSearchResponse.Path path) {
        List<SubRouteItem> subRouteItems = new ArrayList<>();
        List<RouteSearchResponse.SubPath> subPaths = path.getSubPath();

        for (RouteSearchResponse.SubPath subPath : subPaths) {
            if (subPath.isWalkType()) {
                continue;
            }

            SubRoute subRoute = createSubRoute(subPath);
            subRouteItems.add(new SubRouteItem(subRoute));
        }

        return subRouteItems;
    }

    private SubRoute createSubRoute(RouteSearchResponse.SubPath subPath) {
        RouteSearchResponse.Lane lane = subPath.getLane().get(0);
        int lineNum = lane.getLineNum();
        int sectionTime = subPath.getSectionTime();
        String startStationName = subPath.getStartStationName();
        String endStationName = subPath.getEndStationName();
        int startStationCode = subPath.getStartID();
        int endStationCode = subPath.getEndID();
        int wayCode = subPath.getWayCode();

        return new SubRoute(
                startStationName, endStationName, startStationCode, endStationCode, lineNum, wayCode, sectionTime
        );
    }

    private List<BriefStation> createBriefRoute(RouteSearchResponse.Path path, List<SubRouteItem> subRouteItems) {
        List<BriefStation> briefRoute = new ArrayList<>();

        for (int i = 0; i < subRouteItems.size(); i++) {
            SubRoute subRoute = subRouteItems.get(i).getSubRoute();
            briefRoute.add(new BriefStation(subRoute.getStartStationName(),subRoute.getLineNum()));

            //마지막 서브 경로의 경우 도착역도 추가
            if (i == path.getInfo().getSubwayTransitCount() - 1) {
                briefRoute.add(new BriefStation(subRoute.getEndStationName(), subRoute.getLineNum()));
            }
        }

        return briefRoute;
    }

    private boolean isNotSubwayLineOneToNine(String input) {

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
    private void setTransferTimeInRoute(Route route) {

        int totalTime = 0;
        List<SubRouteItem> routeItems = route.getRouteItems();
        int transitCount = route.getTransitCount();

        for (int i = 0; i < transitCount; i++) {
            SubRouteItem subRouteItem = routeItems.get(i);
            SubRoute currentSubRoute = subRouteItem.getSubRoute();
            totalTime += currentSubRoute.getSectionTime();

            if (i + 1 < transitCount) {
                SubRoute nextSubRoute = routeItems.get(i + 1).getSubRoute();
                CompletableFuture<TransferInfo> transferInfoFuture = transferManager.searchTransferInfoAsync(currentSubRoute, nextSubRoute);

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
    private void setMakarTimeInRoutes(List<Route> routes) throws IOException, ExecutionException, InterruptedException {
        for (int i = 0; i < routes.size(); i++) {
            Log.d("MAKAR", i + 1 + "번째 경로 막차 계산");
            Route route = routes.get(i);
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            Calendar takingTime = makarManager.computeMakarTime(route, dayOfWeek);
            route.setMakarTime(takingTime.getTime());
        }
    }

    public void setMakarTimeInRoute(Route route) throws IOException, ExecutionException, InterruptedException {
        Log.d("MAKAR", "선택한 경로 막차 계산");
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        Calendar takingTime = makarManager.computeMakarTime(route, dayOfWeek);
        route.setMakarTime(takingTime.getTime());
    }

}
