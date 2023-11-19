package com.example.makar.mypage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.makar.data.CustomAdapter;
import com.example.makar.data.Station;
import com.example.makar.databinding.ActivitySearchHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySearchHomeBinding searchHomeBinding = ActivitySearchHomeBinding.inflate(getLayoutInflater());
        setContentView(searchHomeBinding.getRoot());

        setSupportActionBar(searchHomeBinding.toolbarSearchHome.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        searchHomeBinding.toolbarSearchHome.toolbarText.setText("역 검색");
        searchHomeBinding.toolbarSearchHome.toolbarButton.setVisibility(View.GONE);
        searchHomeBinding.toolbarSearchHome.toolbarImage.setVisibility(View.GONE);

        SearchView searchView = searchHomeBinding.searchViewHome;
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

        ListView listView = searchHomeBinding.searchHomeListView;
        List<Station> resultList = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(this, resultList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                Intent intent = new Intent(SearchHomeActivity.this, SetFavoriteStationActivity.class);
                intent.putExtra("station_near_home", selectedItem);
                Log.d("MAKARS", selectedItem);
                startActivity(intent);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        searchHomeBinding.searchViewHome.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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