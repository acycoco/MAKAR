package com.example.makar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.makar.databinding.ActivitySetFavoriteStationBinding;

public class SetFavoriteStationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetFavoriteStationBinding binding = ActivitySetFavoriteStationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //자주 가는 역 등록하기 버튼 클릭 리스너
        binding.setFavoriteStationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}