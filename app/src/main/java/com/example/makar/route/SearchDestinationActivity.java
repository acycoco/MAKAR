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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.makar.data.ActivityUtil;
import com.example.makar.data.Adapter.SearchAdapter;
import com.example.makar.data.Station;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySearchDestinationBinding;
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

public class SearchDestinationActivity extends AppCompatActivity {
    ActivitySearchDestinationBinding binding;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private List<Station> resultList = new ArrayList<>();
    private FirebaseFirestore db;
    private User user = MainActivity.user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActivityUtil();
        setListener();
        setSearchView();
        setRecyclerView();

        db = FirebaseFirestore.getInstance();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarSearchDestination.getRoot());
        ActivityUtil.setToolbar(binding.toolbarSearchDestination, "도착역 입력");
        ActivityUtil.setHideKeyboard(binding.getRoot());
    }

    // MARK: setListener()
    private void setListener() {
        binding.searchViewDestination.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.isEmpty()) {
                    CollectionReference collectionRef = db.collection("stations");

                    //newText로 시작하는 모든 역 검색
                    Query query = collectionRef.orderBy("stationName").startAt(newText).endAt(newText + "\uf8ff");

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

        //즐겨찾는 역 도착지로 설정
        binding.homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getHomeStation() != null) {
                    SetRouteActivity.destinationStation = user.getHomeStation();
                    finish();
                } else {
                    startActivity(new Intent(SearchDestinationActivity.this, SetFavoriteStationActivity.class));
                }
            }
        });

        binding.schoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getSchoolStation() != null) {
                    SetRouteActivity.destinationStation = user.getSchoolStation();
                    finish();
                } else {
                    startActivity(new Intent(SearchDestinationActivity.this, SetFavoriteStationActivity.class));
                }
            }
        });

        binding.detailBtn.setOnClickListener(view -> {
            startActivity(new Intent(SearchDestinationActivity.this, SetFavoriteStationActivity.class));
        });
    }

    // MARK: setSearchView()
    private void setSearchView() {
        SearchView searchView = binding.searchViewDestination;
        searchView.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
    }

    // MARK: setRecyclerView()
    private void setRecyclerView() {
        recyclerView = binding.searchDestinationRecyclerView;
        adapter = new SearchAdapter(this, resultList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Station station) {
                SetRouteActivity.destinationStation = station;
                Log.d("MAKARTEST", "SearchDestination : Destination = " + SetRouteActivity.destinationStation);
                finish();
            }
        });
    }

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}