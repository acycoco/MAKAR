package com.example.makar.data;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.makar.BuildConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataConverter {
    private final Context context;
    private final FirebaseFirestore db;

    public DataConverter(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    //역정보 엑셀 파일 읽고 db에 저장하기
    public void readExcelFileAndSave() {
        try {
            InputStream inputStream = context.getAssets().open("stations_info.xlsx");
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);


            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {

                    String stationName = row.getCell(5).getStringCellValue();
                    String stationCode = row.getCell(4).getStringCellValue();
                    String lineNum = row.getCell(3).getStringCellValue();
                    String railOpr = row.getCell(0).getStringCellValue();

                    Station station = new Station(stationName, stationCode, lineNum, railOpr);

                    db.collection("stations")
                            .add(station)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("MAKAR", station.getStationName() + " " + station.getLineNum() + "가 Firestore에 추가되었습니다. ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("MAKAR", "Firestore에 station 데이터 추가 중 오류 발생: " + e.getMessage());
                                }
                            });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //**api호출 1000번 초과하면 안됨!! 주의 **
    //중복이 제거된 역이름을 읽어서 api의 역 정보 저장하기
    public void readUniqueStationNameAndSearchStation() {
        try {
            InputStream inputStream = context.getAssets().open("unique_clean_stations_Name.xlsx");
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i <= 50; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String stationName = row.getCell(0).getStringCellValue();
                    String result = searchStation(stationName);
                    List<OdsayStationData.Station> stations = parseStationDataResponse(result, stationName);

                    if (stations.isEmpty()) {
                        continue;
                    }

                    updateOdsayStationData(stations);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //역이름과 호선 틀린 데이터 찾기
    public void modifyOdsayStationData() {
        List<String> documentIds = new ArrayList<>();
        documentIds.add("PT91d4DtVGngUk5jIQSr");
        documentIds.add("Ktsfwna30xygmDhaqg8I");
        documentIds.add("WgXIeWhrdF3pSNgt7GKq");
        documentIds.add("jXj2CYy1grxwGjGaHGwS");

        for (String documentId : documentIds) {
            CollectionReference stationsRef = db.collection("stations");
            DocumentReference documentRef = stationsRef.document(documentId);

            documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            String stationName = document.getString("cleanStationName");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String result = searchStation(stationName);
                                        List<OdsayStationData.Station> stations = parseStationDataResponse(result, stationName);
                                        updateOdsayStationData(stations);

                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }).start();

                        } else {

                            Log.d("MAKAR", "Document does not exist: " + documentId);
                        }
                    } else {
                        Log.e("MAKAR", "Error getting document: " + documentId, task.getException());
                    }
                }
            });

        }
    }

    private void updateOdsayStationData(List<OdsayStationData.Station> stations) {

        for (OdsayStationData.Station odsayStation : stations) {
            String stationName = odsayStation.getStationName();

            if (!odsayStation.getStationName().equals(stationName)) {
                Log.d("makar", "주의: " + stationName + "이랑 " + odsayStation.getStationName() + "이 다릅니다.");
                continue;
            }

            int stationType = odsayStation.getType();
            String lineNum = mapOdsayStationTypeToLineNum(stationType);

            if (lineNum == null) {
                Log.e("makar", "변환 에러: " + stationName + " " + stationType + "호선");
                continue;
            }

            CollectionReference stationsRef = db.collection("stations");
            Query query = stationsRef.whereEqualTo("cleanStationName", stationName)
                    .whereEqualTo("lineNum", lineNum);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() != 1) {
                            Log.e("makar", "데이터개수 오류 " + stationName + " " + stationType + "호선");
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            updateOdsayStationDataInDocument(document, odsayStation);
                        }

                    } else {
                        Log.d("MAKAR", "검색 중 오류 발생: " + stationName + " ", task.getException());
                    }
                }
            });
        }
    }

    public void updateStationsCollection() {
        db.collection("stations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference docRef = db.collection("stations").document(document.getId());
                                docRef.collection("odsay_stations")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot subDocument : task.getResult()) {
                                                        OdsayStation odsayStation = subDocument.toObject(OdsayStation.class);

                                                        docRef.update("odsayStationID", odsayStation.getStationID());
                                                        docRef.update("odsayStationName", odsayStation.getStationName());
                                                        docRef.update("x", odsayStation.getX());
                                                        docRef.update("y", odsayStation.getY());
                                                        docRef.update("OdsayLaneType", odsayStation.getType());
                                                        Log.d("makar",document.getId() + "success");
                                                    }
                                                } else {
                                                    Log.d("makar", "Error getting sub-collection documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d("makar", "Error getting documents: ", task.getException());
                        }
                    }
                });

        db.collection("stations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            WriteBatch batch = db.batch();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference docRef = db.collection("stations").document(document.getId());
                                batch.update(docRef, "X", FieldValue.delete());
                                batch.update(docRef, "Y", FieldValue.delete());
                            }
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("makar", "Document successfully updated!");
                                    } else {
                                        Log.d("makar", "Error updating document", task.getException());
                                    }
                                }
                            });
                        } else {
                            Log.d("makar", "Error getting documents: ", task.getException());
                        }
                    }
                });


    }
    private void updateOdsayStationDataInDocument(QueryDocumentSnapshot document, OdsayStationData.Station odsayStation) {
        String stationName = odsayStation.getStationName();
        int stationType = odsayStation.getType();

        db.collection("stations")
                .document(document.getId())
                .collection("odsay_stations")
                .document(stationName + stationType)
                .set(odsayStation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("MAKAR", stationName + " " + stationType + "가 추가 또는 업데이트 " + document.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MAKAR", stationName + " " + stationType + "가 오류 발생: " + e.getMessage());
                    }
                });
    }


    public void validateOdsayStationsDataFromDB() {
        CollectionReference stationsRef = db.collection("stations");

        stationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        validateOdsayStationsCollection(document);
                    }
                } else {
                    Log.d("MAKAR", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void validateOdsayStationsCollection(QueryDocumentSnapshot document) {
        // 각 문서의 필드 가져오기
        String stationName = document.getString("cleanStationName");
        String lineNum = document.getString("lineNum");

        // 서브컬렉션(odsay_stations) 확인
        CollectionReference odsayStationsRef = document.getReference().collection("odsay_stations");
        odsayStationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> odsayTask) {
                if (odsayTask.isSuccessful()) {
                    //문서 개수 확인
                    if (odsayTask.getResult().size() == 1) {

                        List<DocumentSnapshot> documents = odsayTask.getResult().getDocuments();
                        DocumentSnapshot documentSnapshot = documents.get(0);

                        String odsayStationName = documentSnapshot.getString("stationName");
                        int odsayType = documentSnapshot.getLong("type").intValue();

                        // 필드 비교
                        if (!stationName.equals(odsayStationName) || !lineNum.equals(mapOdsayStationTypeToLineNum(odsayType))) {
                            // 오류 로그 출력
                            Log.e("MAKAR", "Data inconsistency error in document: " + document.getId());
                        }
                    }
                } else {
                    Log.d("MAKAR", "Error getting odsay_stations documents: ", odsayTask.getException());
                }
            }
        });
    }


    public void addCleanStationNameAtDB() {
        CollectionReference stationsRef = db.collection("stations");
        stationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // 기존 필드 값 가져오기
                        String stationName = document.getString("stationName");

                        //괄호제거
                        String cleanStationName = extractTextWithinParentheses(stationName);

                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("cleanStationName", cleanStationName);

                        // Firestore 문서 업데이트
                        document.getReference().update(updateData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("makar", "Document updated successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("makar", "Error updating document", e);
                                    }
                                });
                    }
                } else {
                    Log.d("makar", "Error getting documents: ", task.getException());
                }
            }
        });

    }


    //db에서 unique한 역이름 가져오기
    private Set<String> getUniqueStationNamesFromDB() {
        Set<String> uniqueStationNames = new HashSet<>();

        db.collection("stations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String stationName = document.getString("cleanStationName");
                            System.out.println(stationName);
                            uniqueStationNames.add(stationName);
                        }
                        writeUniqueStationNamesToExcel(uniqueStationNames);
                    } else {
                        Exception e = task.getException();
                        Log.e("makar", e.getMessage());
                    }
                });

        return uniqueStationNames;
    }

    private void writeUniqueStationNamesToExcel(Set<String> uniqueStationNames) {
        System.out.println(uniqueStationNames.size());
        File file = new File(context.getFilesDir(), "unique_clean_stations_Name.xlsx");
        Log.d("makar", String.valueOf(context.getFilesDir()));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            Workbook newWorkbook = new XSSFWorkbook();
            Sheet sheet = newWorkbook.createSheet("Unique Stations");

            int rowNumber = 0;
            for (String stationName : uniqueStationNames) {
                Row row = sheet.createRow(rowNumber++);
                Cell cell = row.createCell(0);
                cell.setCellValue(stationName);
            }

            newWorkbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String mapOdsayStationTypeToLineNum(int stationType) {
        if (stationType >= 1 && stationType <= 9) {
            return stationType + "호선";
        }
        if (stationType == 101) {
            return "공항철도";
        }
        if (stationType == 104) {
            return "경의중앙";
        }
        return null;
    }

    private String extractTextWithinParentheses(String input) {
        Pattern pattern = Pattern.compile("(.+?)\\(.+?\\)");
        Matcher matcher = pattern.matcher(input);

        // 매칭된 경우 괄호 앞 부분 반환, 매칭이 안 되면 전체 문자열 반환
        return matcher.find() ? matcher.group(1) : input;
    }


    //대중교통 정류장 찾기 api호출
    private String searchStation(String stationName) throws IOException {
        String apiKey = BuildConfig.ODSAY_API_KEY;
        if (apiKey == null) {
            Log.e("MAKAR", "api key null");
        }

        StringBuilder urlBuilder = new StringBuilder("https://api.odsay.com/v1/api/searchStation");

        urlBuilder.append("?lang=" + URLEncoder.encode("0", "UTF-8"));
        urlBuilder.append("&stationName=" + URLEncoder.encode(stationName, "UTF-8"));
        urlBuilder.append("&stationClass=" + URLEncoder.encode("2", "UTF-8")); //2:지하철
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
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private List<OdsayStationData.Station> parseStationDataResponse(String jsonResponse, String stationName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OdsayStationData result = objectMapper.readValue(jsonResponse, OdsayStationData.class);
            return result.getResult().getStation();
        } catch (JsonProcessingException e) {
            Log.e("makar", "json응답데이터 파싱 실패 " + stationName);
            return Collections.emptyList();
        }
    }

    private void writeApiResultToExcel(Sheet sheet, int rowNum, List<OdsayStationData.Station> stations) {
        int cellNum = 6; // cell 6부터 작성

        for (OdsayStationData.Station station : stations) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                row = sheet.createRow(rowNum);
            }

            row.createCell(cellNum++).setCellValue(station.getStationName());
            row.createCell(cellNum++).setCellValue(station.getStationID());
            row.createCell(cellNum++).setCellValue(station.getX());
            row.createCell(cellNum++).setCellValue(station.getY());
            row.createCell(cellNum++).setCellValue(station.getCID());
            row.createCell(cellNum++).setCellValue(station.getArsID());
            row.createCell(cellNum++).setCellValue(station.getDoValue());
            row.createCell(cellNum++).setCellValue(station.getGu());
            row.createCell(cellNum++).setCellValue(station.getDong());
            row.createCell(cellNum++).setCellValue(station.getType());
            row.createCell(cellNum++).setCellValue(station.getLaneName());
            row.createCell(cellNum++).setCellValue(station.getLaneCity());
            row.createCell(cellNum++).setCellValue(station.getEbid());
        }
    }
}
