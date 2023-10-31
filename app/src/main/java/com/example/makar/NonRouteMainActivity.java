package com.example.makar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.makar.databinding.ActivityNonRouteMainBinding;
import com.example.makar.databinding.DialogSetFavoriteStationBinding;

public class NonRouteMainActivity extends AppCompatActivity {
    AlertDialog setFavoriteStationDialog;


    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(dialog == setFavoriteStationDialog){
                //자주 가는 역 설정 Activity start
                startActivity(new Intent(NonRouteMainActivity.this, SetFavoriteStationActivity.class));

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNonRouteMainBinding binding = ActivityNonRouteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //경로 설정 버튼 클릭 리스너
        binding.setRouteBtn.setOnClickListener(view -> {
            startActivity(new Intent(NonRouteMainActivity.this, SetRouteActivity.class));

        });

        setFavoriteStation();
    }


    //자주 가는 역 설정 다이얼로그
    //해당 함수 언제 실행할지 상의필요
    private void setFavoriteStation(){

        setFavoriteStationDialog = new AlertDialog.Builder(this)
                .setTitle("자주 가는 역을 등록하시겠어요?")
                .setPositiveButton("설정하기", dialogListener)
                .setNegativeButton("닫기", null)
                .create();

//        /**Custom Dialog**/
//        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.dialog_set_favorite_station, null);
//        setFavoriteStationDialog = new AlertDialog.Builder(this)
//                .setView(dialogView)
//                        .create();
//        //positiveBtn, negativeBtn Listener 다는 구조 상의 필요
//

        setFavoriteStationDialog.show();
    }
}