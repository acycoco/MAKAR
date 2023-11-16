package com.example.makar.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

public class DataConverter {
    private final Context context;

    public DataConverter(Context context) {
        this.context = context;
    }

    public void readExcelFileAndSave() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

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
}
