package com.example.makar.route;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.makar.data.ActivityUtil;
import com.example.makar.data.Adapter.SearchAdapter;
import com.example.makar.data.Station;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySearchSourceBinding;
import com.example.makar.main.MainActivity;
import com.example.makar.mypage.SetFavoriteStationActivity;
import com.example.makar.route.listener.OnItemClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchSourceActivity extends AppCompatActivity {
    ActivitySearchSourceBinding binding;
    //static Station sourceStation;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private List<Station> resultList = new ArrayList<>();
    private User user = MainActivity.user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchSourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActionBar();
        setToolBar();
        ActivityUtil.setHideKeyboard(binding.getRoot());
        setSearchView();
        setRecyclerView();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        binding.searchViewSource.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.isEmpty()) {
                    CollectionReference collectionRef = db.collection("stations");

                    //newText로 시작하는 모든 역 검색
                    Query query = collectionRef.orderBy("stationName")
                            .startAt(newText)
                            .endAt(newText + "\uf8ff");

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                resultList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    Station station = document.toObject(Station.class);
                                    resultList.add(station);
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d("MAKAR", "검색 중 오류 발생: ", task.getException());
                            }
                        }
                    });


                }
                return true;
            }
        });

        //recyclerView click listener
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Station station) {
                SetRouteActivity.sourceStation = station;
                Log.d("MAKARTEST", "SearchSource : Source = " + SetRouteActivity.sourceStation);
                finish();
            }
        });

        //즐겨찾는 역 출발지로 설정
        binding.homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getHomeStation() != null) {
                    SetRouteActivity.sourceStation = user.getHomeStation();
                    finish();
                } else {
                    startActivity(new Intent(SearchSourceActivity.this, SetFavoriteStationActivity.class));
                }
            }
        });

        binding.schoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getSchoolStation() != null) {
                    SetRouteActivity.sourceStation = user.getSchoolStation();
                    Log.d("MAKARTEST", "SearchSource : Source = " + SetRouteActivity.sourceStation);
                    finish();
                } else {
                    startActivity(new Intent(SearchSourceActivity.this, SetFavoriteStationActivity.class));
                }
            }
        });

        binding.detailBtn.setOnClickListener(view -> {
            startActivity(new Intent(SearchSourceActivity.this, SetFavoriteStationActivity.class));
        });
    }


    //searchView input 설정
    private void setSearchView() {
        SearchView searchView = binding.searchViewSource;
        searchView.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
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

    private void setToolBar() {
        binding.toolbarSearchSource.toolbarText.setText("출발역 입력");
        binding.toolbarSearchSource.toolbarImage.setVisibility(View.GONE);
        binding.toolbarSearchSource.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbarSearchSource.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setRecyclerView() {
        recyclerView = binding.searchSourceRecyclerView;
        adapter = new SearchAdapter(this, resultList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}