package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.makar.databinding.ActivitySetFavoriteStationBinding;

public class SetFavoriteStationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetFavoriteStationBinding binding = ActivitySetFavoriteStationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarSetFavoriteStation);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //자주 가는 역 등록하기 버튼 클릭 리스너
        binding.setFavoriteStationBtn.setOnClickListener(view -> {
                Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_station_toast, Toast.LENGTH_SHORT).show();
                finish();
                //NonRouteMainActivity로 돌아감
        });

    }
}