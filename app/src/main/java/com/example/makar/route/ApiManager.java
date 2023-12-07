package com.example.makar.route;

import android.util.Log;

import com.example.makar.BuildConfig;
import com.example.makar.data.RouteSearchResponse;
import com.example.makar.data.SubwaySchedule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ApiManager {
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public ApiManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.apiKey = BuildConfig.ODSAY_API_KEY;
        if (apiKey == null) {
            Log.e("MAKAR", "api key null");
        }
    }

    //대중교통 길찾기 api 호출
    public RouteSearchResponse requestRoute(double sourceX, double sourceY, double destinationX, double destinationY) throws IOException {
        String endpoint = "https://api.odsay.com/v1/api/searchPubTransPathT";
        Map<String, String> params = new HashMap<>();
        params.put("SX", String.valueOf(sourceX));
        params.put("SY", String.valueOf(sourceY));
        params.put("EX", String.valueOf(destinationX));
        params.put("EY", String.valueOf(destinationY));
        params.put("SearchPathType", "1");

        String routeSearchResponse = makeApiRequest(endpoint, params);
        Log.d("makar-api", "경로 조회 api결과 : " + routeSearchResponse);
        return parseRouteSearchResponse(routeSearchResponse);
    }

    //지하철 시간표 api호출
    public SubwaySchedule requestSubwaySchedule(int stationID, int wayCode) throws IOException {
        String endpoint = "https://api.odsay.com/v1/api/subwayTimeTable";
        Map<String, String> params = new HashMap<>();
        params.put("stationID", String.valueOf(stationID));
        params.put("wayCode", String.valueOf(wayCode));
        params.put("showExpressTime", "1");
        params.put("sepExpressTime", "1");

        String subwayScheduleResponse = makeApiRequest(endpoint, params);
        Log.d("makar-api", "시간표 api결과 : " + subwayScheduleResponse);
        return parseSubwayScheduleResponse(subwayScheduleResponse);
    }

    private String makeApiRequest(String endpoint, Map<String, String> params) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(endpoint);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                    .append("&");
        }
        urlBuilder.append("apiKey=").append(URLEncoder.encode(apiKey, "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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

    private RouteSearchResponse parseRouteSearchResponse(String jsonResponse) throws IOException {
        return objectMapper.readValue(jsonResponse, RouteSearchResponse.class);
    }

    private SubwaySchedule parseSubwayScheduleResponse(String jsonResponse) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode resultNode = rootNode.path("result");

        return objectMapper.treeToValue(resultNode, SubwaySchedule.class);
    }
}
