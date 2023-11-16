package com.example.makar.main;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.databinding.ActivitySignupBinding;
import com.example.makar.databinding.ActivityTimeTableBinding;

public class TimeTableActivity extends AppCompatActivity {
    ActivityTimeTableBinding timeTableBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timeTableBinding = ActivityTimeTableBinding.inflate(getLayoutInflater());
        setContentView(timeTableBinding.getRoot());

        setSupportActionBar(timeTableBinding.toolbarTimeTable.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled (true);

        timeTableBinding.toolbarTimeTable.toolbarText.setText("시간표");
        timeTableBinding.toolbarTimeTable.toolbarImage.setVisibility(View.GONE);
        timeTableBinding.toolbarTimeTable.toolbarButton.setVisibility(View.GONE);
    }

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