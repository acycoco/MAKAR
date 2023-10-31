package com.example.makar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.makar.databinding.ActivityNonRouteMainBinding;

public class NonRouteMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNonRouteMainBinding binding = ActivityNonRouteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //경로 설정 버튼 클릭 리스너
        binding.setRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });
    }
}