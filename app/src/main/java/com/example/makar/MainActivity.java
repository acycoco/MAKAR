package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.example.makar.Dialog.SetAlarmDialog;
import com.example.makar.Dialog.SetFavoriteStationDialog;
import com.example.makar.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private Boolean isRouteSet = false;
    SetAlarmDialog setAlarmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarRouteMain);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        /**==경로 설정 Main==**/

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


        /**==경로 미설정 Main==**/

        //경로 설정 버튼 클릭 리스너
        binding.setRouteBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SetRouteActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isRouteSet){
            //route 설정된 메인화면
            setFavoriteStation();
        }
        else {
            //route 미설정 화면
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater ().inflate (R.menu.menu_toolbar, menu);

        return true;
    }

    //자주 가는 역 설정 다이얼로그
    private void setFavoriteStation(){
        SetFavoriteStationDialog setFavoriteStationDialog = new SetFavoriteStationDialog(this);
        setFavoriteStationDialog.show();
    }
}