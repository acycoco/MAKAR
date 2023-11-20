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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.makar.BuildConfig;
import com.example.makar.data.Route;
import com.example.makar.data.RouteItem;
import com.example.makar.data.Station;
import com.example.makar.databinding.ActivitySetRouteBinding;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class SetRouteActivity extends AppCompatActivity {

    ActivitySetRouteBinding setRouteBinding;
    public Button sourceBtn, destinationBtn;

    //임시 출발지, 목적지 변수
    public static Station sourceStation, destinationStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO 앱이 시작화면에 초기화하는 코드 -> 나중에 옮겨야됨 (확실히 필요한지는 모르겠음)
//        FirebaseApp.initializeApp(this);

        setRouteBinding = ActivitySetRouteBinding.inflate(getLayoutInflater());
        setContentView(setRouteBinding.getRoot());

        setSupportActionBar(setRouteBinding.toolbarSetRoute.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setRouteBinding.toolbarSetRoute.toolbarText.setText("경로 설정하기");
        setRouteBinding.toolbarSetRoute.toolbarImage.setVisibility(View.GONE);
        setRouteBinding.toolbarSetRoute.toolbarButton.setVisibility(View.GONE);

        sourceBtn = setRouteBinding.searchDepartureButton;
        destinationBtn = setRouteBinding.searchDestinationButton;

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 터치 이벤트가 발생시 키보드를 숨기기
                hideKeyboard();
                return false;
            }
        });

        //역 엑셀 파일을 db에 올리는 코드 (db초기화 시에만 씀)
//        DataConverter databaseConverter = new DataConverter(this);
//        databaseConverter.readExcelFileAndSave();

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

            new Thread(() -> {
                try {
                    String result = searchRoute();
                    Route route = parseRouteResponse(result);
                    // 결과를 사용하여 UI 업데이트 등의 작업을 하려면 Handler를 사용
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Log.d("MAKAR", route.toString());
                        // 결과를 사용하여 UI 업데이트 등의 작업 수행
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //sourceBtn, destinationBtn text 변경
        setSerchBarText();
    }

    private String searchRoute() throws IOException {
        String apiKey = BuildConfig.ODSAY_API_KEY;
        if (apiKey == null) {
            Log.e("makar", "api key null");
        }

        StringBuilder urlBuilder = new StringBuilder("https://api.odsay.com/v1/api/subwayPath");

        urlBuilder.append("?CID=" + URLEncoder.encode("1000", "UTF-8"));
        urlBuilder.append("&SID=" + URLEncoder.encode("201", "UTF-8"));
        urlBuilder.append("&EID=" + URLEncoder.encode("222", "UTF-8"));
        urlBuilder.append("&Sopt=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&apiKey=" + URLEncoder.encode(apiKey, "UTF-8"));
//        String urlInfo = "https://api.odsay.com/v1/api/searchPubTransPathT?SX=126.9027279&SY=37.5349277&EX=126.9145430&EY=37.5499421&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");

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
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }


    private Route parseRouteResponse(String jsonResponse) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            int totalTravelTime = jsonNode.path("result").path("globalTravelTime").asInt();

            Route route = new Route(totalTravelTime);

            //경로 받아오기
            JsonNode driveInfoSetNode = jsonNode.path("result").path("driveInfoSet");
            if (!driveInfoSetNode.isMissingNode()) {
                JsonNode driveInfoArray = driveInfoSetNode.path("driveInfo");

                for (JsonNode driveInfo : driveInfoArray) {
                    int lineId = driveInfo.path("laneID").asInt(); //호선 ID
                    String laneName = driveInfo.path("laneName").asText(); //호선명
                    String startName = driveInfo.path("startName").asText(); //시작역
                    int wayCode = driveInfo.path("wayCode").asInt(); //방면 1:상행 2:하행

                    route.addRouteItem(new RouteItem(lineId, startName, wayCode));
                }

                //여기에 도착역을 추가해주는 게 좋은지?
            }

            //환승정보 받아오기
            JsonNode exChangeInfoSetNode = jsonNode.path("result").path("exChangeInfoSet");
            if (!exChangeInfoSetNode.isMissingNode()) {
                JsonNode exChangeInfoArray = exChangeInfoSetNode.path("exChangeInfo");

                for (JsonNode exChangeInfo : exChangeInfoArray) {
                    int exWalkTime = exChangeInfo.path("exWalkTime").asInt(); //환승 소요시간 (초)

                    route.addExWalkTime(exWalkTime);
                }
            }
            System.out.println(route);
            return route;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

    private void setSerchBarText() {
        if (sourceStation == SearchDepartureActivity.sourceStation && sourceStation!=null) {
            sourceBtn.setText(sourceStation.getStationName());
        }else if(sourceStation != SearchDepartureActivity.sourceStation){
            sourceBtn.setText(SearchDepartureActivity.sourceStation.getStationName());
        }else { sourceBtn.setText(""); }

        if (destinationStation == SearchDestinationActivity.destinationStation && destinationStation!=null) {
            destinationBtn.setText(destinationStation.getStationName());
        } else if(destinationStation != SearchDestinationActivity.destinationStation){
            destinationBtn.setText(SearchDestinationActivity.destinationStation.getStationName());
        }else { destinationBtn.setText(""); }
    }
}