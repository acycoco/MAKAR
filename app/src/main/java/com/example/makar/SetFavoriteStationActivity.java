package com.example.makar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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


                Toast.makeText(SetFavoriteStationActivity.this, "자주 가는 역이 등록되었습니다", Toast.LENGTH_SHORT).show();
                finish();
                //NonRouteMainActivity로 돌아감
            }
        });

    }
}