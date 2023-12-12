package com.example.makar.mypage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.data.ActivityUtil;

import com.example.makar.data.Adapter.RouteListAdapter;
import com.example.makar.data.route.Route;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetFavoriteRouteBinding;
import com.example.makar.main.MainActivity;

import java.util.List;

public class SetFavoriteRouteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RouteListAdapter adapter;
    private User user = MainActivity.user;
    private List<Route> favoriteRoutes = MainActivity.favoriteRoutes;
    ActivitySetFavoriteRouteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetFavoriteRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Log.d("MAKAR_SET_FAVORITE_ROUTE", "favoriteRoute1" + user.getFavoriteRoute1().toString());
//        Log.d("MAKAR_SET_FAVORITE_ROUTE", "favoriteRoute2" + user.getFavoriteRoute2().toString());
//        Log.d("MAKAR_SET_FAVORITE_ROUTE", "favoriteRoute3" + user.getFavoriteRoute3().toString());
//
//        Log.d("MAKAR_SET_FAVORITE_ROUTE", "arr" + favoriteRoutes.toString());
        setActivityUtil();
        setRecyclerView();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarSetFavoriteRoute.getRoot());
        ActivityUtil.setToolbar(binding.toolbarSetFavoriteRoute, "즐겨찾는 경로 설정");
    }

    private void setRecyclerView() {
        recyclerView = binding.setFavoriteStationRecyclerView;
        if (favoriteRoutes != null) {
            adapter = new RouteListAdapter(this, favoriteRoutes);
            binding.emptyListText.setVisibility(View.GONE);
        } else {
            binding.emptyListText.setVisibility(View.VISIBLE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}