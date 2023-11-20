package com.example.makar.route;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.example.makar.data.SearchAdapter;
import com.example.makar.data.Station;
import com.example.makar.databinding.ActivitySearchDestinationBinding;
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
    static Station destinationStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchDestinationBinding = ActivitySearchDestinationBinding.inflate(getLayoutInflater());
        setContentView(searchDestinationBinding.getRoot());

        setSupportActionBar(searchDestinationBinding.toolbarSearchDestination.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        searchDestinationBinding.toolbarSearchDestination.toolbarText.setText("도착역 입력");
        searchDestinationBinding.toolbarSearchDestination.toolbarImage.setVisibility(View.GONE);
        searchDestinationBinding.toolbarSearchDestination.toolbarButton.setVisibility(View.GONE);

        SearchView searchView = searchDestinationBinding.searchViewDestination;
        searchView.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 터치 이벤트가 발생시 키보드를 숨기기
                hideKeyboard();
                return false;
            }
        });

//        DataConverter databaseConverter = new DataConverter(this);
//        databaseConverter.readExcelFileAndSave();

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
                    CollectionReference collectionRef = db.collection("stations"); // 컬렉션 이름에 맞게 변경하세요.

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
                destinationStation = station;
                finish();
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