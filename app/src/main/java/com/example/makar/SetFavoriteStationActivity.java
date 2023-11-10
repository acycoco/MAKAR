package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.makar.databinding.ActivitySetFavoriteStationBinding;

public class SetFavoriteStationActivity extends AppCompatActivity {

    ActivitySetFavoriteStationBinding setFavoriteStationBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFavoriteStationBinding = ActivitySetFavoriteStationBinding.inflate(getLayoutInflater());
        setContentView(setFavoriteStationBinding.getRoot());

        setSupportActionBar(setFavoriteStationBinding.toolbarSetFavoriteStation.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled (true);

        setFavoriteStationBinding.toolbarSetFavoriteStation.toolbarText.setText("자주 가는 역 등록");
        setFavoriteStationBinding.toolbarSetFavoriteStation.toolbarImage.setVisibility(View.GONE);
        setFavoriteStationBinding.toolbarSetFavoriteStation.toolbarButton.setVisibility(View.GONE);


        //자주 가는 역 등록하기 버튼 클릭 리스너
        setFavoriteStationBinding.setFavoriteStationBtn.setOnClickListener(view -> {
                Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_station_toast, Toast.LENGTH_SHORT).show();
                finish();
                //MainActivity로 돌아감
        });
    }

    // toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}