package com.example.makar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.makar.databinding.ActivityRouteMainBinding;

public class RouteMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRouteMainBinding binding = ActivityRouteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //시간표 버튼 클릭 리스너
        binding.timetableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //막차 알림 설정 버튼 클릭 리스너
        binding.setAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //경로 변경하기 버튼 클릭 리스너
        binding.changeRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}