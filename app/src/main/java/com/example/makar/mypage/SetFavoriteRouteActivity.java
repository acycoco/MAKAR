package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.R;
import com.example.makar.databinding.ActivitySetFavoriteRouteBinding;

public class SetFavoriteRouteActivity extends AppCompatActivity {

    ActivitySetFavoriteRouteBinding setFavoriteRouteBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFavoriteRouteBinding = ActivitySetFavoriteRouteBinding.inflate(getLayoutInflater());
        setContentView(setFavoriteRouteBinding.getRoot());

        setActionBar();
        setToolBar();

    }

    //toolbar
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

    private void setToolBar(){
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarText.setText("즐겨찾는 경로 설정");
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarImage.setVisibility(View.GONE);
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar(){
        setSupportActionBar(setFavoriteRouteBinding.toolbarSetFavoriteRoute.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}