package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.makar.R;
import com.example.makar.data.Station;
import com.example.makar.databinding.ActivitySetFavoriteStationBinding;

public class SetFavoriteStationActivity extends AppCompatActivity {

    //임시 즐겨찾는 역
    public static Station homeStation, schoolStation;

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

        setFavoriteStationBinding.homeSearchButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SearchHomeActivity.class));
        });

        setFavoriteStationBinding.schoolSearchButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SearchSchoolActivity.class));
        });

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 터치 이벤트가 발생시 키보드를 숨기기
                hideKeyboard();
                return false;
            }
        });

        //자주 가는 역 등록하기 버튼 클릭 리스너
        setFavoriteStationBinding.setFavoriteStationBtn.setOnClickListener(view -> {
            homeStation = SearchHomeActivity.homeStation;
            schoolStation = SearchSchoolActivity.schoolStation;
            Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_station_toast, Toast.LENGTH_SHORT).show();
            finish();
                //MainActivity로 돌아감
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //즐겨찾는 역 텍스트 수정
        setFavoriteStationText();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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

    private void setFavoriteStationText(){
        EditText editTextHome = setFavoriteStationBinding.editTextHome;
        EditText editTextSchool = setFavoriteStationBinding.editTextSchool;

        if(homeStation != SearchHomeActivity.homeStation && SearchHomeActivity.homeStation != null){
            editTextHome.setText(SearchHomeActivity.homeStation.getStationName());
        }else if(homeStation != null && homeStation == SearchHomeActivity.homeStation){
            editTextHome.setText(homeStation.getStationName());
        }
        else{ editTextHome.setText(""); }

        if(schoolStation != SearchSchoolActivity.schoolStation && SearchSchoolActivity.schoolStation != null){
            editTextSchool.setText(SearchSchoolActivity.schoolStation.getStationName());
        }else if(schoolStation != null && schoolStation == SearchSchoolActivity.schoolStation){
            editTextSchool.setText(schoolStation.getStationName());
        }else{ editTextSchool.setText(""); }
    }
}