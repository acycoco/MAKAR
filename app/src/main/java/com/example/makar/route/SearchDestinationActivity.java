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

import com.example.makar.data.Adapter.SearchAdapter;
import com.example.makar.data.Station;
import com.example.makar.databinding.ActivitySearchDestinationBinding;
import com.example.makar.mypage.SetFavoriteStationActivity;
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
    ActivitySearchDestinationBinding searchDestinationBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchDestinationBinding = ActivitySearchDestinationBinding.inflate(getLayoutInflater());
        setContentView(searchDestinationBinding.getRoot());

        setActionBar();
        setToolBar();
        setHideKeyBoard();
        setSearchView(); //searchView request focus



        RecyclerView recyclerView = searchDestinationBinding.searchDestinationRecyclerView;
        List<Station> resultList = new ArrayList<>();
        SearchAdapter adapter = new SearchAdapter(this, resultList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        searchDestinationBinding.searchViewDestination.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                SetRouteActivity.destinationStation = station;
                Log.d("MAKARTEST", "SearchDestination : Destination = "+SetRouteActivity.destinationStation);
                finish();
            }
        });

        //즐겨찾는 역 도착지로 설정
        searchDestinationBinding.homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SetFavoriteStationActivity.homeStation != null) {
                    SetRouteActivity.destinationStation = SetFavoriteStationActivity.homeStation;
                    finish();
                } else {
                    startActivity(new Intent(SearchDestinationActivity.this, SetFavoriteStationActivity.class));
                }
            }
        });

        searchDestinationBinding.schoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SetFavoriteStationActivity.schoolStation != null) {
                    SetRouteActivity.destinationStation = SetFavoriteStationActivity.schoolStation;
                    finish();
                } else {
                    startActivity(new Intent(SearchDestinationActivity.this, SetFavoriteStationActivity.class));
                }
            }
        });

        searchDestinationBinding.detailBtn.setOnClickListener(view -> {
            startActivity(new Intent(SearchDestinationActivity.this, SetFavoriteStationActivity.class));
        });
    }


    //searchView input 설정
    private void setSearchView() {
        SearchView searchView = searchDestinationBinding.searchViewDestination;
        searchView.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
    }

    //키보드 내리기
    private void setHideKeyBoard() {
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 터치 이벤트가 발생시 키보드를 숨기기
                hideKeyboard();
                return false;
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        searchDestinationBinding.toolbarSearchDestination.toolbarText.setText("도착역 입력");
        searchDestinationBinding.toolbarSearchDestination.toolbarImage.setVisibility(View.GONE);
        searchDestinationBinding.toolbarSearchDestination.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar() {
        setSupportActionBar(searchDestinationBinding.toolbarSearchDestination.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}