package com.example.makar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.makar.databinding.ActivityRouteMainBinding;

public class RouteMainActivity extends AppCompatActivity {
    AlertDialog setalarmDialog;

    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(dialog == setalarmDialog){
                //알림 설정

            }
        }
    };

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
        binding.setAlarmBtn.setOnClickListener(view ->{
                setalarmDialog = new AlertDialog.Builder(this)
                    .setTitle("막차 알림 설정하기")
                    .setPositiveButton("설정하기", dialogListener)
                    .setNegativeButton("닫기", null)
                    .create();

//        /**Custom Dialog**/
//        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.dialog_set_alarm, null);
//        setalarmDialog = new AlertDialog.Builder(this)
//                .setView(dialogView)
//                        .create();
//        //positiveBtn, negativeBtn Listener 다는 구조 상의 필요
//
                setalarmDialog.show();

        });


        //경로 변경하기 버튼 클릭 리스너
        binding.changeRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}