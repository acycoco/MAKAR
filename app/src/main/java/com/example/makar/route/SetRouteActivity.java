package com.example.makar.route;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.R;
import com.example.makar.data.Adapter.RouteAdapter;
import com.example.makar.data.BriefStation;
import com.example.makar.data.Station;
import com.example.makar.BuildConfig;
import com.example.makar.data.Route;
import com.example.makar.data.SubRouteItem;
import com.example.makar.data.RouteSearchResponse;
import com.example.makar.data.SubRoute;
import com.example.makar.data.TransferInfo;

import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetRouteBinding;
import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.main.MainActivity;
import com.example.makar.mypage.SetFavoriteStationActivity;
import com.example.makar.onboarding.LoginActivity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SetRouteActivity extends AppCompatActivity {

    ActivitySetRouteBinding setRouteBinding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO 앱이 시작화면에 초기화하는 코드 -> 나중에 옮겨야됨 (확실히 필요한지는 모르겠음)
//        FirebaseApp.initializeApp(this);

        setRouteBinding = ActivitySetRouteBinding.inflate(getLayoutInflater());
        setContentView(setRouteBinding.getRoot());

        setActionBar();
        setToolBar();
        setHideKeyBoard();
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
//                databaseConverter.updateStationsCollection();
//            }
//        }).start();

        sourceBtn = setRouteBinding.searchSourceButton;
        destinationBtn = setRouteBinding.searchDestinationButton;

        sourceBtn.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchSourceActivity.class));
        });

        destinationBtn.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchDestinationActivity.class));
        });

        //경로 찾기 버튼 클릭 리스너
        setRouteBinding.searchRouteBtn.setOnClickListener(view -> {
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
//        테스트 값 "x": 126.953991,
//                "y": 37.495861,
//                "x": 127.024521,
//                "y": 37.504464,
//        urlBuilder.append("?SX=" + URLEncoder.encode("126.9027279", "UTF-8"));
//        urlBuilder.append("&SY=" + URLEncoder.encode("37.5349277", "UTF-8"));
//        urlBuilder.append("&EX=" + URLEncoder.encode("126.9145430", "UTF-8"));
//        urlBuilder.append("&EY=" + URLEncoder.encode("37.5499421", "UTF-8"));
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
            for (RouteSearchResponse.SubPath subPath : subPaths) {
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
                TransferInfo transferInfo = new TransferInfo(); //TODO 환승정보 만들어야됨 마지막인 경우는 null로 생성
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
            route.setMakarTime("2023-11-25 14:36:30"); //TODO 막차시간 구하기 (막차시간 임시로 설정)
            routes.add(route);
        }
        return routes;
    }


    //터치 시 키보드 내리기
    private void setHideKeyBoard() {
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setToolBar() {
        setRouteBinding.toolbarSetRoute.toolbarText.setText("경로 설정하기");
        setRouteBinding.toolbarSetRoute.toolbarImage.setVisibility(View.GONE);
        setRouteBinding.toolbarSetRoute.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar() {
        setSupportActionBar(setRouteBinding.toolbarSetRoute.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

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
        recyclerView = setRouteBinding.routeRecyclerView;
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
}