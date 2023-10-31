package com.example.makar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.makar.databinding.ActivitySetRouteBinding;

public class SetRouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetRouteBinding binding = ActivitySetRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //경로 찾기 버튼 클릭 리스너
        binding.searchRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}