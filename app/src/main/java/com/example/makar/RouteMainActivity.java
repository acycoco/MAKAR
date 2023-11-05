package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.view.Menu;

import com.example.makar.Dialog.SetAlarmDialog;
import com.example.makar.databinding.ActivityRouteMainBinding;

public class RouteMainActivity extends AppCompatActivity {
    SetAlarmDialog setAlarmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRouteMainBinding binding = ActivityRouteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar3);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        //시간표 버튼 클릭 리스너
        binding.timetableBtn.setOnClickListener(view ->{
        });


        //막차 알림 설정 버튼 클릭 리스너
        binding.setAlarmBtn.setOnClickListener(view ->{
            setAlarmDialog = new SetAlarmDialog(this);
            setAlarmDialog.show();
        });


        //경로 변경하기 버튼 클릭 리스너
        binding.changeRouteBtn.setOnClickListener(view -> {
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater ().inflate (R.menu.menu_toolbar, menu);

        return true;
    }
}