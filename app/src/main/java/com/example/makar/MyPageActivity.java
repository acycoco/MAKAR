package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.databinding.ActivityMyPageBinding;

public class MyPageActivity extends AppCompatActivity {

    ActivityMyPageBinding myPageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPageBinding = ActivityMyPageBinding.inflate(getLayoutInflater());
        setContentView(myPageBinding.getRoot());

        setSupportActionBar(myPageBinding.toolbarMyPage.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        myPageBinding.toolbarMyPage.toolbarText.setText("마이페이지");
        myPageBinding.toolbarMyPage.toolbarImage.setVisibility(View.GONE);
        myPageBinding.toolbarMyPage.toolbarButton.setVisibility(View.GONE);
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