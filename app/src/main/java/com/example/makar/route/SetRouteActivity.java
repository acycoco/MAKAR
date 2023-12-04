package com.example.makar.route;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.R;
import com.example.makar.TimeInfo;
import com.example.makar.data.ActivityUtil;
import com.example.makar.data.Adapter.RouteAdapter;
import com.example.makar.data.BriefStation;
import com.example.makar.data.DataConverter;
import com.example.makar.data.LineStationInfo;
import com.example.makar.data.Station;
import com.example.makar.BuildConfig;
import com.example.makar.data.Route;
import com.example.makar.data.SubRouteItem;
import com.example.makar.data.RouteSearchResponse;
import com.example.makar.data.SubRoute;
import com.example.makar.data.SubwayStation;
import com.example.makar.data.TransferInfo;

import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetRouteBinding;
import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.main.MainActivity;
import com.example.makar.onboarding.LoginActivity;
import com.example.makar.route.listener.OnBookmarkClickListener;
import com.example.makar.route.listener.OnRouteClickListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;


public class SetRouteActivity extends AppCompatActivity {

    ActivitySetRouteBinding binding;
    RouteRecyclerViewItemBinding recyclerViewItemBinding;
    public Button sourceBtn, destinationBtn;

    //임시 출발지, 목적지 변수
    public static Station sourceStation, destinationStation;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private User user = MainActivity.user;
    public static Route selectedRoute;
    public List<Route> resultList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    public static Station briefToSourceStation;
    public static Station briefToDestinationStation;

    private static final int UPTOWN = 1;  //상행
    private static final int DOWNTOWN = 2; //하행

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sourceBtn = binding.searchSourceButton;
        destinationBtn = binding.searchDestinationButton;

        //TODO 앱이 시작화면에 초기화하는 코드 -> 나중에 옮겨야됨 (확실히 필요한지는 모르겠음)
//        FirebaseApp.initializeApp(this);

        setActivityUtil();
        setButtonListener();
        setRecyclerView();

        // 출발역, 도착역 데이터가 있다면 받아오기
        sourceStation = user.getSourceStation();
        destinationStation = user.getDestinationStation();

        //역 엑셀 파일을 db에 올리는 코드 (db초기화 시에만 씀)
//        DataConverter databaseConverter = new DataConverter(this);
////        databaseConverter.readExcelFileAndSave();
////        databaseConverter.createUniqueStationExcelFile();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                databaseConverter.readUniqueStationNameAndSearchStation();
////                databaseConverter.addCleanStationNameAtDB();
////                databaseConverter.createUniqueStationExcelFile();
////                databaseConverter.validateOdsayStationsDataFromDB();
////                databaseConverter.modifyOdsayStationData();
////                databaseConverter.updateStationsCollection();
////                databaseConverter.readExcelFileAndSaveLineMap(2);
////                databaseConverter.copyField("1", "2", "1신창");
////                databaseConverter.copyFieldToAnotherDocument("1", "1", 5, "5하남검단산");
////                databaseConverter.saveNewLine(1, "1", "2", "1신창");
////                databaseConverter.saveReverseTransferInfo();
//                databaseConverter.validateTransferInfo();
//            }
//        }).start();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarSetRoute.getRoot());
        ActivityUtil.setToolbar(binding.toolbarSetRoute, "경로 설정하기");
        ActivityUtil.setHideKeyboard(binding.getRoot());
    }

    // MARK: setButtonListener()
    private void setButtonListener() {
        sourceBtn.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchSourceActivity.class));
        });

        destinationBtn.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchDestinationActivity.class));
        });

        //경로 찾기 버튼 클릭 리스너
        binding.searchRouteBtn.setOnClickListener(view -> {
            // 클릭 이벤트 발생 시 새로운 스레드에서 searchRoute 메서드를 실행
            if (sourceStation != null && destinationStation != null && !Objects.equals(sourceStation.getStationName(), destinationStation.getOdsayStationName())) {
                resultList.clear();
                executeSearchRoute();
            } else if (sourceStation == null) {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_1, Toast.LENGTH_SHORT).show();
            } else if (destinationStation == null) {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_2, Toast.LENGTH_SHORT).show();
            } else if (Objects.equals(sourceStation.getStationName(), destinationStation.getStationName())) {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_3, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_4, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //sourceBtn, destinationBtn text 변경
        setSearchViewText();
    }

    private void executeSearchRoute() {
        Log.d("dhdh", sourceStation.getStationName());
        Log.d("dhdh", destinationStation.getStationName());
        new Thread(() -> {
            try {
                String routeJson = searchRoute(sourceStation.getX(), sourceStation.getY(), destinationStation.getX(), destinationStation.getY());
                System.out.println(routeJson);
                resultList = parseRouteResponse(routeJson);
                Log.d("dhdhdh", resultList.toString());

                new Handler(Looper.getMainLooper()).post(() -> {
                    setRecyclerView();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private String searchRoute(double sourceX, double sourceY, double destinationX, double destinationY) throws IOException {
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

    private List<Route> parseRouteResponse(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RouteSearchResponse result = objectMapper.readValue(jsonResponse, RouteSearchResponse.class);

        List<Route> routes = new ArrayList<>();
        List<RouteSearchResponse.Path> paths = result.getResult().getPath();

        //검색된 여러 경로 탐색
        for (RouteSearchResponse.Path path : paths) {
            RouteSearchResponse.Info pathInfo = path.getInfo();
            List<SubRouteItem> subRouteItems = new ArrayList<>();
            List<RouteSearchResponse.SubPath> subPaths = path.getSubPath();
            List<BriefStation> briefRoute = new ArrayList<>();

            int count = 1;
            //경로의 서브 경로 탐색
            for (int i = 0; i < subPaths.size(); i++) {

                RouteSearchResponse.SubPath subPath = subPaths.get(i);
//            for (RouteSearchResponse.SubPath subPath : subPaths) {
                //도보타입일 경우는 skip
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
                SubRoute subRoute = new SubRoute(startStationName, endStationName, startStationCode, endStationCode, lineNum, wayCode, sectionTime);
                TransferInfo transferInfo = null; //TODO 환승정보 만들어야됨 마지막인 경우는 null로 생성
                if (i + 1 < pathInfo.getSubwayTransitCount()) {
                    subPaths.get(i).getEndID();
                    subPaths.get(i + 1).getStartID();
                    transferInfo = new TransferInfo();
                }

                briefRoute.add(new BriefStation(startStationName, lineNum));
                if (count == pathInfo.getSubwayTransitCount()) {
                    briefRoute.add(new BriefStation(endStationName, lineNum));
                }
                //서브 경로 리스트에 추가
                subRouteItems.add(new SubRouteItem(subRoute, transferInfo));
                count++;
            }

            //경로 리스트에 추가
            Route route = new Route(pathInfo.getTotalTime(), pathInfo.getSubwayTransitCount(), subRouteItems, briefRoute, sourceStation, destinationStation);

            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            List<SubRouteItem> routeItems = route.getRouteItems();

            SubRouteItem subRouteItem = routeItems.get(routeItems.size() - 1);
            SubRoute lastSubPath = subRouteItem.getSubPath();
            String resultSubwaySchedule = requestSubwaySchedule(lastSubPath.getStartStationCode(), lastSubPath.getWayCode());
            SubwayStation subwayStation = parseSubwayScheduleResponse(resultSubwaySchedule);
            Date lastMakarTime = computeMakarTime(dayOfWeek, subwayStation, lastSubPath.getLineNum(), lastSubPath.getWayCode(), lastSubPath.getStartStationCode(), lastSubPath.getEndStationCode());
            System.out.println("makar" + lastMakarTime);
            route.setMakarTime(lastMakarTime);
            for (int i = routeItems.size() - 2; i >= 0; i--) {
                int hours = lastMakarTime.getHours();
                long time = lastMakarTime.getTime();


            }

            routes.add(route);
        }

        return routes;
    }

    private Date computeMakarTime(int dayOfWeek, SubwayStation subwayStation, int odsayLaneType, int wayCode, int startStationID, int endStationID) {

        SubwayStation.OrdList ordList = null;

        //요일에 맞는 시간표 가져오기
        if (dayOfWeek == Calendar.SATURDAY) {
            ordList = subwayStation.getSatList();
        } else if (dayOfWeek == Calendar.SUNDAY) {
            ordList = subwayStation.getSunList();
        } else {
            ordList = subwayStation.getOrdList();
        }

        List<SubwayStation.OrdList.TimeDirection.TimeData> time = null;

        //지하철 방면에 맞는 시간표 가져오기
        if (wayCode == UPTOWN) {
            time = ordList.getUp().getTime();
        } else if (wayCode == DOWNTOWN) {
            time = ordList.getDown().getTime();
        } else {
            Log.e("makar" , "waycode invalid error");
        }

        //매시간마다
        for (int i = time.size() - 1; i >= 0; i--) {
            SubwayStation.OrdList.TimeDirection.TimeData timeData = time.get(i);
            String list = timeData.getList();
            List<TimeInfo> timeInfos = TimeInfo.parseTimeString(list);
            System.out.println(timeInfos);
            System.out.println(timeInfos.size());

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
                                        Log.e("makar" , "waycode invalid error");
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

                    calendar.set(Calendar.HOUR_OF_DAY, time.get(i).getIdx());
                    calendar.set(Calendar.MINUTE, result.get().getMinute());
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    // Date 객체 생성
                    return calendar.getTime();
                }

            }


        }
        return null;
    }


    private String requestSubwaySchedule(int stationID, int wayCode) throws
            IOException {
        String apiKey = BuildConfig.ODSAY_API_KEY;
        if (apiKey == null) {
            Log.e("MAKAR", "api key null");
        }

        //지하철역 전체 시간표 조회 api
        StringBuilder urlBuilder = new StringBuilder("https://api.odsay.com/v1/api/subwayTimeTable");
        urlBuilder.append("?stationID=" + URLEncoder.encode(String.valueOf(stationID), "UTF-8"));
        urlBuilder.append("&wayCode=" + URLEncoder.encode(String.valueOf(wayCode), "UTF-8"));
        urlBuilder.append("&showExpressTime=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&sepExpressTime=" + URLEncoder.encode("1", "UTF-8"));
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

    private SubwayStation parseSubwayScheduleResponse(String jsonResponse) throws
            IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

// jsonResponse 로그 출력
        System.out.println("jsonResponse: " + jsonResponse);

        JsonNode resultNode = rootNode.path("result");
//// resultNode 로그 출력
//        System.out.println("resultNode: " + resultNode);

        // resultNode를 SubwayStation 객체로 변환
//            SubwayStation subwayStation = objectMapper.treeToValue(rootNode, SubwayStation.class);
        SubwayStation subwayStation = objectMapper.treeToValue(resultNode, SubwayStation.class);

        // subwayStation 로그 출력
        System.out.println("subwayStation: " + subwayStation);
//            System.out.println(subwayStation);
//            firebaseFirestore.collection("timetable")
//                    .document("example")
//                    .set(subwayStation)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            // 저장 성공 시 처리
//                            Log.d("makar", "save success");
//                        } else {
//                            // 저장 실패 시 처리
//                        }
//                    });
//
        return subwayStation;
//
//        DocumentReference docRef = firebaseFirestore.collection("timetable").document("example");
//        docRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document.exists()) {
//                    SubwayStation subwayStation = document.toObject(SubwayStation.class);
//                    System.out.println(subwayStation);
//
//
//                    List<SubwayStation.OrdList.TimeDirection.TimeData> time = subwayStation.getOrdList().getUp().getTime();
//                    for (int i = time.size() - 1; i >= 0; i--) {
//                        SubwayStation.OrdList.TimeDirection.TimeData timeData = time.get(i);
//                        String list = timeData.getList();
//                        List<TimeInfo> timeInfos = TimeInfo.parseTimeString(list);
//                        System.out.println(timeInfos);
//                        System.out.println(timeInfos.size());
//                        for (int j = timeInfos.size() - 1; j >= 0; j--) {
//                            TimeInfo timeInfo = timeInfos.get(j);
//                            System.out.println(timeInfo.getTerminalStation());
//
////                            if (timeInfo.getTerminalStation() )
//                        }
//                    }
//                    // 가져온 데이터 활용
////                    String stationName = subwayStation.getStationName();
////                    int stationID = subwayStation.getStationID();
////                    TimeList weekdayTime = subwayStation.getWeekdayTime();
//                    // ...
//                } else {
//                    // 문서가 존재하지 않을 때 처리
//                }
//            } else {
//                // 작업 실패 시 처리
//            }
//        });
// resultNode를 SubwayStation 객체로 변환

//        subwaySchedule.setOrdList(parseSchedule(resultNode.path("OrdList")));
//        subwaySchedule.setSatList(parseSchedule(resultNode.path("SatList")));
//        subwaySchedule.setSunList(parseSchedule(resultNode.path("SunList")));


//        firebaseFirestore.collection('timetable')
//                .add()
    }


//    private S

    private void setSearchViewText() {
        // 서버에 출발역 저장했을 때
        if (sourceStation != null) {
            sourceBtn.setText("  " + sourceStation.getFullName());
        } else {
            sourceBtn.setText("");
        }

        // 서버에 도착역 저장했을 때
        if (destinationStation != null) {
            destinationBtn.setText("  " + destinationStation.getFullName());
        } else {
            destinationBtn.setText("");
        }
    }

    private void setRecyclerView() {
        recyclerView = binding.routeRecyclerView;
        adapter = new RouteAdapter(this, resultList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerViewItemBinding = RouteRecyclerViewItemBinding.inflate(getLayoutInflater());
        adapter.setOnRouteClickListener(new OnRouteClickListener() {
            @Override
            public void onRouteClick(Route route) {
                selectedRoute = route;

                // briefStation 객체 -> Station 객체
                int briefRouteSize = route.getBriefRoute().size();

                String targetSourceStationName = route.getBriefRoute().get(0).getStationName();
                String targetSourceLineNum = route.getBriefRoute().get(0).getLineNumToString();
                Log.d("zz: B SourceStationName", targetSourceStationName);
                Log.d("zz: B SourceLineNum", targetSourceLineNum);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("stations")
                        .whereEqualTo("odsayStationName", targetSourceStationName)
                        .whereEqualTo("lineNum", targetSourceLineNum)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                    Station station = documentSnapshot.toObject(Station.class);
                                    Log.d("zz: BTS", station.toString());
                                    briefToSourceStation = station;
                                    Log.d("zz: BTS", String.valueOf(briefToSourceStation));
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                Log.d("zz: briefToSourceStation", String.valueOf(briefToSourceStation));

                String targetDestinationStationName = route.getBriefRoute().get(briefRouteSize - 1).getStationName();
                String targetDestinationLineNum = route.getBriefRoute().get(briefRouteSize - 1).getLineNumToString();
                Log.d("zz: B DestinationStationName", targetDestinationStationName);
                Log.d("zz: B DestinationLineNum", targetDestinationLineNum);

                db.collection("stations")
                        .whereEqualTo("odsayStationName", targetDestinationStationName)
                        .whereEqualTo("lineNum", targetDestinationLineNum)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                    Station station = documentSnapshot.toObject(Station.class);
                                    Log.d("RouteClick: BTS", station.toString());
                                    briefToDestinationStation = station;
                                    Log.d("zz: BTS", String.valueOf(briefToDestinationStation));
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 검색 실패 시 처리
                                Log.d("MAKARTEST", "find Destination fail");
                            }
                        });

                user.getRecentRouteArr().add(resultList.get(0));
                user.setSelectedRoute(selectedRoute);
                Log.d("MAKAR_SET_ROUTE", selectedRoute.toString());

                // 사용자를 식별해 데이터 저장
                firebaseFirestore.collection("users")
                        .whereEqualTo("userUId", LoginActivity.userUId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        // 값이 존재하는 경우, 해당 데이터를 수정
                                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                        //Station 수정
                                        documentSnapshot.getReference().update(
                                                "sourceStation", briefToSourceStation,
                                                "destinationStation", briefToDestinationStation,
                                                "selectedRoute", selectedRoute
                                        ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentSnapshot.getId());
                                                    Log.d("MAKAR", "MAIN: 사용자 selectedRoute : " + user.getSelectedRoute());
                                                } else {
                                                    Log.d("MAKAR", "사용자 데이터 수정 실패: ", task.getException());
                                                }
                                            }
                                        });
                                    } else {
                                        // 값이 존재하지 않는 경우, 새로운 사용자 데이터 생성
                                        firebaseFirestore.collection("users")
                                                .add(user)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d("MAKAR", "새로운 사용자 데이터가 Firestore에 추가되었습니다. ID: " + documentReference.getId());

                                                        documentReference.update(
                                                                "sourceStation", briefToSourceStation,
                                                                "destinationStation", briefToDestinationStation,
                                                                "selectedRoute", selectedRoute
                                                        ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentReference.getId());
                                                                    Log.d("MAKAR", "MAIN: 사용자 selectedRoute : " + user.getSelectedRoute());
                                                                } else {
                                                                    Log.d("MAKAR", "사용자 데이터 수정 실패: ", task.getException());
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                    MainActivity.isRouteSet = true;
                                    finish();
                                } else {
                                    Toast.makeText(SetRouteActivity.this, R.string.set_favorite_error_toast_3, Toast.LENGTH_SHORT).show();
                                    Log.e("MAKAR", "Firestore에서 사용자 데이터 검색 중 오류 발생: " + task.getException().getMessage());
                                }
                            }
                        });
            }
        });
        adapter.setOnBookmarkClickListener(new OnBookmarkClickListener() {
            @Override
            public void onBookmarkClick(Route route) {
                List<Route> favoriteRouteArr = user.getFavoriteRouteArr();

                firebaseFirestore.collection("users")
                        .whereEqualTo("userUId", LoginActivity.userUId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        // 값이 존재하는 경우, 해당 데이터를 수정
                                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                        if (favoriteRouteArr != null && favoriteRouteArr.size() >= 3) {
                                            Toast.makeText(SetRouteActivity.this, "최대 즐겨찾기 수를 초과하였습니다.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // favoriteRouteArr 저장
                                            documentSnapshot.getReference().update("favoriteRouteArr", FieldValue.arrayUnion(route))
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
//                                                                user.addFavoriteRoute(route);
                                                                // TODO: 이미지 안 바뀜
                                                                recyclerViewItemBinding.favoriteRouteImageView.setImageResource(R.drawable.ic_star_line_filled);
                                                                adapter.notifyDataSetChanged();
                                                                Toast.makeText(SetRouteActivity.this, "즐겨찾는 경로에 추가되었습니다", Toast.LENGTH_SHORT).show();
                                                                Log.d("MAKAR", "사용자 데이터가 Firestore에 추가되었습니다. ID: " + documentSnapshot.getId());
                                                                Log.d("MAKAR", "MAIN: 사용자 selectedRoute : " + user.getFavoriteRouteArr());
                                                            } else {
                                                                Log.d("MAKAR", "사용자 데이터 수정 실패: ", task.getException());
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        // 값이 존재하지 않는 경우, 새로운 사용자 데이터 생성
                                        firebaseFirestore.collection("users")
                                                .add(user)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d("MAKAR", "새로운 사용자 데이터가 Firestore에 추가되었습니다. ID: " + documentReference.getId());
                                                        if (favoriteRouteArr != null && favoriteRouteArr.size() >= 3) {
                                                            Toast.makeText(SetRouteActivity.this, "최대 즐겨찾기 수를 초과하였습니다.", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            documentReference.update("favoriteRouteArr", route)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
//                                                                                user.addFavoriteRoute(route);
                                                                                // TODO: 이미지 안 바뀜
                                                                                recyclerViewItemBinding.favoriteRouteImageView.setImageResource(R.drawable.ic_star_line_filled);
                                                                                adapter.notifyDataSetChanged();
                                                                                Toast.makeText(SetRouteActivity.this, "즐겨찾는 경로에 추가되었습니다", Toast.LENGTH_SHORT).show();
                                                                                Log.d("MAKAR", "사용자 데이터가 Firestore에 추가되었습니다. ID: " + documentReference.getId());
                                                                                Log.d("MAKAR", "MAIN: 사용자 selectedRoute : " + user.getFavoriteRouteArr());
                                                                            } else {
                                                                                Log.d("MAKAR", "사용자 데이터 수정 실패: ", task.getException());
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(SetRouteActivity.this, R.string.set_favorite_error_toast_3, Toast.LENGTH_SHORT).show();
                                    Log.e("MAKAR", "Firestore에서 즐겨찾기 설정 중 오류 발생: " + task.getException().getMessage());
                                }
                            }
                        });
            }
        });
    }

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}