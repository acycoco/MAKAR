package com.example.makar.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TimeTable {
    private Context context;

    public TimeTable(Context context) {
        this.context = context;
    }

    public void TimeTableFormatter() {
        try {
            InputStream inputStream = context.getAssets().open("makar_timetable.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String json = stringBuilder.toString();
            JSONObject jsonObject = new JSONObject(json);

            String name = jsonObject.getString("subwayename");
//            int age = jsonObject.getInt("나이");

            // 추출한 데이터 활용
            // 예를 들어, 텍스트뷰에 데이터를 설정할 수 있습니다.
            // textView.setText("이름: " + name + ", 나이: " + age);
            Log.d("ㅁㅁㅁㅁㅁㅁㅁㅁ", name);
            bufferedReader.close();
            inputStream.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
