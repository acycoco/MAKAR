package com.example.makar.route;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.makar.data.DataConverter;
import com.example.makar.data.OdsayStation;
import com.example.makar.data.Station;
import com.example.makar.BuildConfig;
import com.example.makar.data.Route;
import com.example.makar.data.SubRouteItem;
import com.example.makar.data.RouteSearchResponse;
import com.example.makar.data.SubRoute;
import com.example.makar.data.TransferInfo;

import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetRouteBinding;
import com.example.makar.mypage.MyPageActivity;
import com.example.makar.mypage.SearchHomeActivity;
import com.example.makar.mypage.SetFavoriteStationActivity;
import com.example.makar.onboarding.LoginActivity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class SetRouteActivity extends AppCompatActivity {

    ActivitySetRouteBinding setRouteBinding;
    public Button sourceBtn, destinationBtn;

    //임시 출발지, 목적지 변수
    public static Station sourceStation, destinationStation;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference usersRef = database.getReference("users");
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

        sourceBtn = setRouteBinding.searchDepartureButton;
        destinationBtn = setRouteBinding.searchDestinationButton;


//        //역 엑셀 파일을 db에 올리는 코드 (db초기화 시에만 씀)
//        DataConverter databaseConverter = new DataConverter(this);
//        databaseConverter.readExcelFileAndSave();
//        databaseConverter.createUniqueStationExcelFile();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                databaseConverter.readUniqueStationNameAndSearchStation();
//                databaseConverter.addCleanStationNameAtDB();
//                databaseConverter.createUniqueStationExcelFile();
//                databaseConverter.validateOdsayStationsDataFromDB();
//                databaseConverter.modifyOdsayStationData();
//            }
//        }).start();


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        sourceBtn.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchDepartureActivity.class));
        });

        destinationBtn.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchDestinationActivity.class));
        });

        //경로 찾기 버튼 클릭 리스너
        setRouteBinding.searchRouteBtn.setOnClickListener(view -> {
            // 클릭 이벤트 발생 시 새로운 스레드에서 searchRoute 메서드를 실행
            sourceStation = SearchDepartureActivity.sourceStation;
            destinationStation = SearchDestinationActivity.destinationStation;

            User user = new User(LoginActivity.userUId, SetFavoriteStationActivity.homeStation, SetFavoriteStationActivity.schoolStation, sourceStation, destinationStation);

            firebaseFirestore.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("MAKAR", "사용자 데이터가 Firestore에 추가되었습니다. ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("MAKAR", "Firestore에 사용자 데이터 추가 중 오류 발생: " + e.getMessage());
                        }
                    });
          
            double sourceX = getX(sourceStation);
            double sourceY = getY(sourceStation);
            double destinationX = getX(destinationStation);
            double destinationY = getY(destinationStation);

            new Thread(() -> {
                try {
                    String routeJson = searchRoute(sourceX, sourceY, destinationX, destinationY);
                    System.out.println(routeJson);
                    List<Route> routes = parseRouteResponse(routeJson);
                    // 결과를 사용하여 UI 업데이트 등의 작업을 하려면 Handler를 사용
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Log.d("MAKAR", routes.toString());
                        // 결과를 사용하여 UI 업데이트 등의 작업 수행
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });

    }

    private double getX(Station station) {
        OdsayStation odsayStation = station.getOdsayStation();
        return odsayStation.getX();
    }

    private double getY(Station station) {
        OdsayStation odsayStation = station.getOdsayStation();
        return odsayStation.getY();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //sourceBtn, destinationBtn text 변경
        setSearchBarText();
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

                //서브 경로 리스트에 추가
                subRouteItems.add(new SubRouteItem(subRoute, transferInfo));
            }
            //경로 리스트에 추가
            Route route = new Route(pathInfo.getTotalTime(), pathInfo.getSubwayTransitCount(), subRouteItems);
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

    private void setActionBar(){
        setSupportActionBar(setRouteBinding.toolbarSetRoute.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setSearchBarText() {
        if (sourceStation == SearchDepartureActivity.sourceStation && sourceStation != null) {
            sourceBtn.setText("  " + sourceStation.getStationName() + "역 " + sourceStation.getLineNum());
        } else if (sourceStation != SearchDepartureActivity.sourceStation) {
            sourceBtn.setText("  " + SearchDepartureActivity.sourceStation.getStationName() + "역 " + SearchDepartureActivity.sourceStation.getLineNum());
        } else {
            sourceBtn.setText("");
        }

        if (destinationStation == SearchDestinationActivity.destinationStation && destinationStation != null) {
            destinationBtn.setText("  " + destinationStation.getStationName() + "역 " + destinationStation.getLineNum());
        } else if (destinationStation != SearchDestinationActivity.destinationStation) {
            destinationBtn.setText("  " + SearchDestinationActivity.destinationStation.getStationName() + "역 " + SearchDestinationActivity.destinationStation.getLineNum());
        } else {
            destinationBtn.setText("");
        }
    }
}