package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.databinding.ActivityMyPageBinding;
import com.example.makar.databinding.ActivitySetGetOffBinding;

public class SetGetOffActivity extends AppCompatActivity {

    ActivitySetGetOffBinding setGetOffBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_get_off);

        setGetOffBinding = ActivitySetGetOffBinding.inflate(getLayoutInflater());
        setContentView(setGetOffBinding.getRoot());

        setSupportActionBar(setGetOffBinding.toolbarSetGetOff.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setGetOffBinding.toolbarSetGetOff.toolbarText.setText("하차 알림 설정");
        setGetOffBinding.toolbarSetGetOff.toolbarImage.setVisibility(View.GONE);
        setGetOffBinding.toolbarSetGetOff.toolbarButton.setVisibility(View.GONE);
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