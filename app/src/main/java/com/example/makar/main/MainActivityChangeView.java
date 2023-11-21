package com.example.makar.main;

import android.view.View;

import com.example.makar.databinding.ActivityMainBinding;

public class MainActivityChangeView {
    static boolean changeView(ActivityMainBinding mainBinding, boolean isRouteSet, long leftTime, String source, String destination) {
        if (isRouteSet) {
            //route 설정된 메인화면
            mainBinding.timetableBtn.setVisibility(View.VISIBLE);
            mainBinding.mainRouteView.setText(source + "  ->  " + destination); //출발지, 도착지
            mainBinding.mainTitleText.setText("막차까지 " + leftTime + "분 남았습니다"); //막차까지 남은 시간
            mainBinding.mainDestinationText.setText(source); //도착지 이름
            mainBinding.changeRouteBtn.setVisibility(View.VISIBLE);
            mainBinding.setAlarmBtn.setVisibility(View.VISIBLE);
            mainBinding.setRouteBtn.setVisibility(View.GONE);
            mainBinding.favoriteRouteText.setVisibility(View.GONE);
            mainBinding.recentRouteText.setVisibility(View.GONE);
            return true;
        } else {
            //route 미설정 화면
            mainBinding.timetableBtn.setVisibility(View.GONE);
            mainBinding.mainRouteView.setText("출발역  ->  도착역");
            mainBinding.mainTitleText.setText("경로를 설정해주세요");
            mainBinding.mainDestinationText.setText("MAKAR");
            mainBinding.changeRouteBtn.setVisibility(View.GONE);
            mainBinding.setAlarmBtn.setVisibility(View.GONE);
            mainBinding.setRouteBtn.setVisibility(View.VISIBLE);
            mainBinding.favoriteRouteText.setVisibility(View.VISIBLE);
            mainBinding.recentRouteText.setVisibility(View.VISIBLE);
            return false;
            //false를 리턴해 MainActivity에서 setFavoriteStation() 실행
        }
    }
}
