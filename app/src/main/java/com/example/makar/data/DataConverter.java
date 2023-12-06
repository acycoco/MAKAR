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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.apache.poi.ss.format.CellFormatType;
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
    //db에 데이터를 저장하고 검증하는 클래스
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


    public void saveReverseTransferInfo() {
        db.collection("transfer")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    // 각 문서의 fromLine, toLine 값을 가져오기
                                    int fromLine = ((Long) document.get("fromLine")).intValue();
                                    int toLine = ((Long) document.get("toLine")).intValue();
                                    String stationName = (String) document.get("odsayStationName");
//                                    System.out.println("확인" + stationName + fromLine + " " + toLine);
                                    // 서로 반대인 경우가 존재하는지 확인
                                    if (!hasOppositeDocument(querySnapshot, fromLine, toLine, stationName)) {
                                        // 반대인 경우, 새로운 문서 추가
                                        Map<String, Object> newData = new HashMap<>();
                                        newData.put("fromLine", toLine);
                                        newData.put("toLine", fromLine);
                                        newData.put("fromStationID", document.get("toStationID"));
                                        newData.put("toStationID", document.get("fromStationID"));
                                        newData.put("odsayStationName", document.get("odsayStationName"));
                                        newData.put("time", document.get("time"));

                                        System.out.println(stationName + fromLine + toLine);
                                        // 새로운 문서 추가
                                        db.collection("transfer").add(newData)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d("MAKAR", "새로운 문서 추가 완료. ID: " + documentReference.getId());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("MAKAR", "새로운 문서 추가 중 오류 발생: " + e.getMessage());
                                                    }
                                                });
                                    }
                                }
                            } else {
                                Log.d("MAKAR", "데이터가 존재하지 않습니다.");
                            }
                        } else {
                            // 쿼리 실패
                            Log.e("MAKAR", "쿼리 실패: " + task.getException());
                        }
                    }
                });


    }

    private boolean hasOppositeDocument(QuerySnapshot querySnapshot, int fromLine, int toLine, String stationName) {
        for (QueryDocumentSnapshot document : querySnapshot) {
            int currentFromLine = ((Long) document.get("fromLine")).intValue();
            int currentToLine = ((Long) document.get("toLine")).intValue();
            String currentStationName = (String) document.get("odsayStationName");
//            System.out.println("반대가 있나? " + "확인" + stationName + fromLine + " " + toLine);
            if (currentFromLine == toLine && currentToLine == fromLine && stationName.equals(currentStationName)) {
                // 서로 반대인 경우가 이미 존재함
                return true;
            }
        }
        // 서로 반대인 경우가 존재하지 않음
        return false;
    }


    public void validateLineSequences2() {
        db.collection("line_sequence")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Object sourceValue = document.get("1");

                                if (sourceValue instanceof List) {
                                    List<?> sourceList = (List<?>) sourceValue;

                                    for (Object obj : sourceList) {
                                        if (obj instanceof LineStationInfo) {
                                            LineStationInfo lineStationInfo = (LineStationInfo) obj;
                                            if (containsWhitespaceOrParentheses(lineStationInfo.getStationName())) {
                                                Log.d("data", lineStationInfo.getStationName() + " contains whitespace or parentheses");
                                            }
                                        } else {
                                            HashMap<?, ?> obj1 = (HashMap<?, ?>) obj;
                                            if (containsWhitespaceOrParentheses((String) obj1.get("stationName"))) {
                                                Log.e("data", (String) obj1.get("stationName")+ " contains whitespace or parentheses");
                                            }
                                            Log.d("data", (String) obj1.get("stationName")+ " 확인완료");
                                        }
                                    }
                                } else {
                                    Log.d("MAKAR", "No such document");
                                }

                                sourceValue = document.get("2");
                                if (sourceValue instanceof List) {
                                    List<?> sourceList = (List<?>) sourceValue;

                                    for (Object obj : sourceList) {
                                        if (obj instanceof LineStationInfo) {
                                            LineStationInfo lineStationInfo = (LineStationInfo) obj;
                                            if (containsWhitespaceOrParentheses(lineStationInfo.getStationName())) {
                                                Log.d("data", lineStationInfo.getStationName() + " contains whitespace or parentheses");
                                            }
                                        } else {
                                            HashMap<?, ?> obj1 = (HashMap<?, ?>) obj;
                                            if (containsWhitespaceOrParentheses((String) obj1.get("stationName"))) {
                                                Log.e("data", (String) obj1.get("stationName")+ " contains whitespace or parentheses");
                                            }
                                            Log.d("data", (String) obj1.get("stationName")+ " 확인완료");
                                        }
                                    }
                                } else {
                                    Log.d("MAKAR", "No such document");
                                }
                            }
                        }
                    }
                });
    }

    public void validateLineSequences33(String documentName) {
        DocumentReference docRef = db.collection("line_sequence").document(documentName);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 원본 필드의 값을 가져와서
                        Object sourceValue = document.get("1");

                        if (sourceValue instanceof List) {
                            List<?> sourceList = (List<?>) sourceValue;

                            for (Object obj : sourceList) {
                                if (obj instanceof LineStationInfo) {
                                    LineStationInfo lineStationInfo = (LineStationInfo) obj;
                                    if (containsWhitespaceOrParentheses(lineStationInfo.getStationName())) {
                                        Log.d("data", lineStationInfo.getStationName() + " contains whitespace or parentheses");
                                    }
                                } else {
                                    HashMap<?, ?> obj1 = (HashMap<?, ?>) obj;
                                    if (containsWhitespaceOrParentheses((String) obj1.get("stationName"))) {
                                        Log.e("data", (String) obj1.get("stationName")+ " contains whitespace or parentheses");
                                    }
                                    Log.d("data", (String) obj1.get("stationName")+ " 확인완료");
                                }
                            }
                        } else {
                            Log.d("MAKAR", "No such document");
                        }
                    } else {
                        Log.d("MAKAR", "get failed with ", task.getException());
                    }
                }
            }
        });
    }



    public void validateTransferInfo() {
        db.collection("transfer")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                Map<String, Integer> countMap = new HashMap<>();

                                // 데이터 개수 카운팅
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    Object fromLine = document.get("fromLine");
                                    Object toLine = document.get("toLine");
                                    Object odsayStationName = document.get("odsayStationName");

                                    // 각 필드의 값이 null이 아니면서 같은 값을 갖는 경우 count 증가
                                    if (fromLine != null && toLine != null && odsayStationName != null) {
                                        if ((fromLine instanceof Long || fromLine instanceof Integer) &&
                                                (toLine instanceof Long || toLine instanceof Integer) &&
                                                odsayStationName instanceof String) {

                                            String key = fromLine + "-" + toLine + "-" + odsayStationName;
                                            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                                        }
                                    }
                                }

                                // 결과 출력
                                for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                                    if (entry.getValue() == 1) {
                                        Log.d("MAKAR", "동일한 필드값을 가진 데이터가 1개 존재: " + entry.getKey());
                                    } else {
                                        Log.e("MAKAR", "동일한 필드값을 가진 데이터가 1개가 아님: " + entry.getKey());
                                    }
                                }
                            } else {
                                Log.d("MAKAR", "데이터가 존재하지 않습니다.");
                            }
                        } else {
                            // 쿼리 실패
                            Log.e("MAKAR", "쿼리 실패: " + task.getException());
                        }
                    }
                });


    }

    public void readAndSaveTransferInfo() {
        try {
            InputStream inputStream = context.getAssets().open("transfer_info.xlsx");
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Set<String> addedStations = new HashSet<>();
//            sheet.getLastRowNum()
            for (int i = 109; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {

                    String stationName = row.getCell(2).getStringCellValue();
                    int from = (int) row.getCell(1).getNumericCellValue();
                    int to = (int) row.getCell(3).getNumericCellValue();
                    String time = row.getCell(5).getStringCellValue();

                    if (stationName.equals("서울")) {
                        stationName = "서울역";
                    }


                    if (stationName.equals("이수")) {
                        stationName = "총신대입구(이수)";
                    }
//
                    final String finalStationName = stationName;

                    Map<String, Object> newData = new HashMap<>();
//

                    String key = finalStationName + from + to + time;
                    if (addedStations.contains(key)) {
                        // 이미 추가된 경우, 다음 루프로 이동
                        continue;
                    }

                    addedStations.add(key);

                    String[] parts = time.split("분");
                    int minute = Integer.parseInt(parts[0]);

                    newData.put("fromLine", from);
                    newData.put("toLine", to);
                    newData.put("time", minute + 1);
                    db.collection("stations")
                            .whereEqualTo("odsayLaneType", from)
                            .whereEqualTo("odsayStationName", stationName).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                                    if (documents.size() != 1) {
                                        Log.e("makar", finalStationName + from + "없음 에러");
                                    }
                                    Station fromStation = documents.get(0).toObject(Station.class);
                                    db.collection("stations")
                                            .whereEqualTo("odsayLaneType", to)
                                            .whereEqualTo("odsayStationName", finalStationName).get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                                                    if (documents.size() != 1) {
                                                        Log.e("makar", finalStationName + to + "없음 에러");
                                                    }
                                                    Station toStation = documents.get(0).toObject(Station.class);

                                                    newData.put("fromStationID", fromStation.getOdsayStationID());
                                                    newData.put("toStationID", toStation.getOdsayStationID());
                                                    newData.put("odsayStationName", fromStation.getOdsayStationName());
                                                    db.collection("transfer")
                                                            .add(newData)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Log.d("MAKAR", fromStation.getOdsayStationName() + "가 Firestore에 추가되었습니다. ID: " + documentReference.getId());
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("MAKAR", "Firestore에 station 데이터 추가 중 오류 발생: " + e.getMessage());
                                                                }
                                                            });
                                                }
                                            });
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


    public void readExcelFileAndSaveLineMap(int code) {
        try {
            InputStream inputStream = context.getAssets().open("linemap.xlsx");
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);


            Row row = sheet.getRow(12); //7호선
            if (row != null) {
                String lineOrder = row.getCell(4).getStringCellValue();
                System.out.println(lineOrder);
                List<String> list = extractStationNamesNot(lineOrder);

                System.out.println(list);
                Map<Integer, String> stationsMap = new HashMap<>();
                for (int i = 0; i < list.size(); i++) {
                    String stationName = list.get(i);
                    if (stationName.equals("올림픽공원")) {
                        stationName = "올림픽공원(한국체대)";
                    }
                    System.out.println(stationName);

                    final int finalIndex = i;
                    db.collection("stations")
                            .whereEqualTo("stationName", stationName)
                            .whereEqualTo("odsayLaneType", code)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<Station> station = task.getResult().toObjects(Station.class);
                                    if (station.size() != 1) {
                                        Log.d("makar", "에러");
                                    }

                                    stationsMap.put(finalIndex, station.get(0).getOdsayStationName());

                                    // 모든 역에 대한 데이터를 가져온 후에 search 메소드 호출
                                    if (stationsMap.size() == list.size()) {
                                        List<String> orderedStations = new ArrayList<>();
                                        for (int j = 0; j < list.size(); j++) {
                                            orderedStations.add(stationsMap.get(j));
                                        }
//                                        Collections.reverse(orderedStations);
                                        search(orderedStations, code);
                                    }
                                }
                            });

                }


            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveNewLine(int code, String sourceField, String destinationField, String newDocumentName) {

        db.collection("line_sequence").document(String.valueOf(code))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // "sourceField"의 값을 가져와서 "destinationField"에 설정
                                Object sourceValue = document.get(sourceField);
                                if (sourceValue != null) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put(destinationField, sourceValue);
                                    data.put("odsayLaneType", code);

                                    // 새로운 도큐먼트에 저장
                                    db.collection("line_sequence").document(newDocumentName)
                                            .set(data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("MAKAR", "값 복사 성공");
                                                        searchOdsayCode(newDocumentName, destinationField, code);
                                                    } else {
                                                        Log.w("MAKAR", "값 복사 실패", task.getException());
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.d("MAKAR", "문서가 존재하지 않습니다.");
                            }
                        } else {
                            Log.w("MAKAR", "문서 가져오기 실패", task.getException());
                        }
                    }
                });

    }

    public boolean containsWhitespaceOrParentheses(String input) {
        // 공백이나 괄호를 포함하는지 여부를 확인
        return input != null && input.contains(" ") || input.contains("(") || input.contains(")");
    }

    private void searchOdsayCode(String newDocumentName, String destinationField, int code) {
        db.collection("line_sequence").document(newDocumentName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // "destinationField"의 값을 가져와서 List<String>로 형변환
                                Object destinationValueObject = document.get(destinationField);
                                if (destinationValueObject instanceof List) {
                                    List<String> destinationValues = (List<String>) destinationValueObject;
                                    Collections.reverse(destinationValues);

                                    Map<Integer, LineStationInfo> briefStations = new HashMap<>();
                                    // BriefStation 객체를 저장할 리스트

                                    // "stations" 컬렉션에서 필터를 적용하여 데이터 가져오기
                                    for (int i = 0; i < destinationValues.size(); i++) {

                                        final int idx = i;
                                        String stationName = destinationValues.get(i);

                                        db.collection("stations")
                                                .whereEqualTo("odsayStationName", stationName)
                                                .whereEqualTo("odsayLaneType", code)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            // "odsayStationName"과 일치하는 문서를 찾았을 때
                                                            if (task.getResult().size() != 1) {
                                                                Log.e("makar", "size에러 " + stationName);

                                                            }

                                                            DocumentSnapshot stationDocument = task.getResult().getDocuments().get(0);
                                                            String stationName = stationDocument.getString("odsayStationName");
                                                            int lineNum = stationDocument.getLong("odsayStationID").intValue();
                                                            LineStationInfo lineStationInfo = new LineStationInfo(lineNum, stationName);
                                                            // briefStations 리스트에 추가
                                                            briefStations.put(idx, lineStationInfo);

                                                            // 모든 검색이 완료되면 briefStations를 사용하여 새로운 도큐먼트에 저장

                                                            if (briefStations.size() == destinationValues.size()) {
                                                                List<LineStationInfo> orderedStations = new ArrayList<>();
                                                                for (int j = 0; j < destinationValues.size(); j++) {
                                                                    orderedStations.add(briefStations.get(j));
                                                                }
//                                        Collections.reverse(orderedStations);
                                                                saveBriefStationsToNewDocument(orderedStations, newDocumentName);
                                                            }
                                                        } else {
                                                            Log.w("MAKAR", "stations 컬렉션 조회 실패", task.getException());
                                                        }
                                                    }
                                                });
                                    }
                                }
                            } else {
                                Log.d("MAKAR", "문서가 존재하지 않습니다.");
                            }
                        } else {
                            Log.w("MAKAR", "문서 가져오기 실패", task.getException());
                        }
                    }
                });
    }

    private void saveBriefStationsToNewDocument(List<LineStationInfo> briefStations, String newDocumentName) {
        // 새로운 도큐먼트에 briefStations를 저장하는 코드 작성
        // 예시로 "new_collection" 컬렉션의 "new_document" 문서에 저장하는 코드
        Map<String, Object> data = new HashMap<>();
        data.put("1", briefStations);

        db.collection("line_sequence").document(newDocumentName)
                .set(data, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("MAKAR", "BriefStation 리스트를 새로운 도큐먼트에 저장 성공");
                        } else {
                            Log.w("MAKAR", "BriefStation 리스트 저장 실패", task.getException());
                        }
                    }
                });
    }

    private void search(List<String> stations, int code) {

        Map<String, Object> data = new HashMap<>();
        data.put("2", stations);

//        data.put("1", FieldValue.arrayUnion(stations.toArray()));
        db.collection("line_sequence").document(String.valueOf(code))
                .set(data, SetOptions.merge())
//                .update(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("MAKAR", code + "호선 db에 추가");
                    }
                });

    }


    public void copyField(String sourceField, String destinationField, String documentName) {
        DocumentReference docRef = db.collection("line_sequence").document(documentName);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 원본 필드의 값을 가져와서
                        Object sourceValue = document.get(sourceField);

                        if (sourceValue instanceof List) {
                            List<LineStationInfo> sourceList = (List<LineStationInfo>) sourceValue;
                            Collections.reverse(sourceList);
                            // 대상 필드에 복사
                            Map<String, Object> data = new HashMap<>();
                            data.put(destinationField, sourceList);

                            docRef.update(data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("MAKAR", sourceField + " 필드를 " + destinationField + "로 복사 완료");
                                        }
                                    });
                        } else {
                            Log.d("MAKAR", "No such document");
                        }
                    } else {
                        Log.d("MAKAR", "get failed with ", task.getException());
                    }
                }
            }
        });
    }


    private List<String> extractStationNames(String stationsString) {
        List<String> stationCodes = new ArrayList<>();

        String[] stations = stationsString.split(",");
        for (String station : stations) {
            String[] parts = station.split("-");
            if (parts.length > 1) {
                stationCodes.add(parts[1]);
            }
        }

        return stationCodes;
    }

    private List<String> extractStationNamesNot(String input) {
        List<String> stationNames = new ArrayList<>();

        // 쉼표(,)를 기준으로 문자열을 나눔
        String[] stationsArray = input.split(",");

        for (String station : stationsArray) {
            // "-역"을 제거하여 역이름만 추출
            String stationName = station.split("-")[1].replace("역", "");
            stationNames.add(stationName);
        }

        return stationNames;
    }

    //역이름과 호선 틀린 데이터 찾기
    public void modifyOdsayStationData() {
        List<String> documentIds = new ArrayList<>();
//        documentIds.add("PT91d4DtVGngUk5jIQSr");
//        documentIds.add("Ktsfwna30xygmDhaqg8I");
//        documentIds.add("WgXIeWhrdF3pSNgt7GKq");
//        documentIds.add("jXj2CYy1grxwGjGaHGwS");
//        documentIds.add("y72GToFI6qs2qppcOhSk");
        documentIds.add("6YBTuwAjzPPJt3JSHNZp");

        for (String documentId : documentIds) {
            CollectionReference stationsRef = db.collection("stations");
            DocumentReference documentRef = stationsRef.document(documentId);


            documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            String stationName = document.getString("stationName");
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

//            if (!odsayStation.getStationName().equals(stationName)) {
//                Log.d("makar", "주의: " + stationName + "이랑 " + odsayStation.getStationName() + "이 다릅니다.");
//                continue;
//            }

            int stationType = odsayStation.getType();
            String lineNum = mapOdsayStationTypeToLineNum(stationType);

            if (lineNum == null) {
                Log.e("makar", "변환 에러: " + stationName + " " + stationType + "호선");
                continue;
            }

            CollectionReference stationsRef = db.collection("stations");
            Query query = stationsRef.whereEqualTo("stationName", stationName)
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
                                                        docRef.update("odsayLaneType", odsayStation.getType());
                                                        Log.d("makar", document.getId() + "success");
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
                                batch.update(docRef, "OdsayLaneType", FieldValue.delete());
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
                .document(extractTextWithinParentheses(stationName) + stationType)
                .set(odsayStation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println(stationName + stationType);
                        Log.d("MAKAR", stationName + " " + stationType + "가 추가 또는 업데이트 " + document.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MAKAR", stationName + " " + stationType + "가 오류 발생: " + e.getMessage());
                    }
                });

//        DocumentReference docRef = db.collection("stations").document(document.getId());
//        docRef.collection("odsay_stations")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot subDocument : task.getResult()) {
//                                OdsayStation odsayStation = subDocument.toObject(OdsayStation.class);
//
//                                docRef.update("odsayStationID", odsayStation.getStationID());
//                                docRef.update("odsayStationName", odsayStation.getStationName());
//                                docRef.update("x", odsayStation.getX());
//                                docRef.update("y", odsayStation.getY());
//                                docRef.update("odsayLaneType", odsayStation.getType());
//                                Log.d("makar", document.getId() + "success");
//                            }
//                        } else {
//                            Log.d("makar", "Error getting sub-collection documents: ", task.getException());
//                        }
//                    }
//                });
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
