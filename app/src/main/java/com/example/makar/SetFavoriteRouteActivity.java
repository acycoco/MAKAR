package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.databinding.ActivitySetFavoriteRouteBinding;

public class SetFavoriteRouteActivity extends AppCompatActivity {

    ActivitySetFavoriteRouteBinding setFavoriteRouteBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_favorite_route);

        setFavoriteRouteBinding = ActivitySetFavoriteRouteBinding.inflate(getLayoutInflater());
        setContentView(setFavoriteRouteBinding.getRoot());

        setSupportActionBar(setFavoriteRouteBinding.toolbarSetFavoriteRoute.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarText.setText("즐겨찾는 경로 설정");
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarImage.setVisibility(View.GONE);
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarButton.setVisibility(View.GONE);
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