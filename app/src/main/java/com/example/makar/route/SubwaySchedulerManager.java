package com.example.makar.route;

import android.util.Log;

import com.example.makar.BuildConfig;
import com.example.makar.data.SubwayStation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SubwaySchedulerManager {

    private static final int UPTOWN = 1;  //상행
    private static final int DOWNTOWN = 2; //하행

    public static String requestSubwaySchedule(int stationID, int wayCode) throws
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

    public static SubwayStation parseSubwayScheduleResponse(String jsonResponse, int stationID, int wayCode) throws
            IOException, ExecutionException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        JsonNode resultNode = rootNode.path("result");

        return objectMapper.treeToValue(resultNode, SubwayStation.class);
    }


    private CompletableFuture<SubwayStation> getSubwayStationAsync(int stationID, int wayCode) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        String way = "";
        if (wayCode == UPTOWN) {
            way = "up";
        } else if (wayCode == DOWNTOWN) {
            way = "down";
        }
        CompletableFuture<SubwayStation> future = new CompletableFuture<>();

        DocumentReference docRef = firebaseFirestore.collection("timetable").document(stationID + way);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    SubwayStation subwayStation = document.toObject(SubwayStation.class);
                    future.complete(subwayStation);
                } else {
                    try {
                        String response = SubwaySchedulerManager.requestSubwaySchedule(stationID, wayCode);
                        SubwayStation subwayStation = SubwaySchedulerManager.parseSubwayScheduleResponse(response, stationID, wayCode);
                        saveTimeTable(subwayStation, stationID, wayCode);
                    } catch (IOException | ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    future.completeExceptionally(new IllegalStateException("Document does not exist."));
                }
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }

    private void saveTimeTable(SubwayStation subwayStation, int stationID, int wayCode) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        String way = "";
        if (wayCode == UPTOWN) {
            way = "up";
        } else if (wayCode == DOWNTOWN) {
            way = "down";
        }
        firebaseFirestore.collection("timetable")
                .document(stationID + way)
                .set(subwayStation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("makar", "timetable save success");
                    } else {
                        Log.e("makar", "timetable save fail");
                    }
                });
    }
}
