package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.example.makar.Dialog.SetAlarmDialog;
import com.example.makar.Dialog.SetFavoriteStationDialog;
import com.example.makar.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private Boolean isRouteSet = false;
    SetAlarmDialog setAlarmDialog;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = ActivityMainBinding.inflate(getLayoutInflater());
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
            //result 받아서 막차 시간 set

            //막차 set 유무 따라
            isRouteSet = true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isRouteSet){
            //route 설정된 메인화면
            binding.timetableBtn.setVisibility(View.VISIBLE);
            binding.mainDestinationText.setText("source  ->  destination"); //출발지, 도착지
            binding.mainTitleText.setText("막차까지 %d분 남았습니다"); //막차까지 남은 시간
            binding.mainDestinationText.setText("destination"); //도착지 이름
            binding.changeRouteBtn.setVisibility(View.VISIBLE);
            binding.setAlarmBtn.setVisibility(View.VISIBLE);
            binding.setRouteBtn.setVisibility(View.GONE);
            binding.favoriteRouteText.setVisibility(View.GONE);
            binding.recentRouteText.setVisibility(View.GONE);
        }
        else {
            //route 미설정 화면
            setFavoriteStation();

            binding.timetableBtn.setVisibility(View.GONE);
            binding.mainDestinationText.setText("출발역  ->  도착역");
            binding.mainTitleText.setText("경로를 설정해주세요");
            binding.mainDestinationText.setText("MAKAR");
            binding.changeRouteBtn.setVisibility(View.GONE);
            binding.setAlarmBtn.setVisibility(View.GONE);
            binding.setRouteBtn.setVisibility(View.VISIBLE);
            binding.favoriteRouteText.setVisibility(View.VISIBLE);
            binding.recentRouteText.setVisibility(View.VISIBLE);
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