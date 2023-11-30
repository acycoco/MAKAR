package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.data.Adapter.RouteAdapter;

import com.example.makar.data.Adapter.RouteListAdapter;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetFavoriteRouteBinding;
import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.main.MainActivity;

public class SetFavoriteRouteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RouteListAdapter adapter;
    private User user = MainActivity.user;
    ActivitySetFavoriteRouteBinding setFavoriteRouteBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFavoriteRouteBinding = ActivitySetFavoriteRouteBinding.inflate(getLayoutInflater());
        setContentView(setFavoriteRouteBinding.getRoot());
        Log.d("dkdkkdkd", user.getFavoriteRouteArr().toString());
        setActionBar();
        setToolBar();
        setRecyclerView();
    }
    private void setRecyclerView() {
        recyclerView = setFavoriteRouteBinding.setFavoriteStationRecyclerView;
        if (user.getFavoriteRouteArr() != null) {
            adapter = new RouteListAdapter(this, user.getFavoriteRouteArr());
            setFavoriteRouteBinding.emptyListText.setVisibility(View.GONE);
        } else {
            setFavoriteRouteBinding.emptyListText.setVisibility(View.VISIBLE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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