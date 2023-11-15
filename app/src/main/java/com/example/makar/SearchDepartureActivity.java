package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.widget.SearchView;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import com.example.makar.Data.Station;
import com.example.makar.databinding.ActivitySearchDepartureBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SearchDepartureActivity extends AppCompatActivity {
    ActivitySearchDepartureBinding setFavoriteRouteBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_departure);

        setFavoriteRouteBinding = ActivitySearchDepartureBinding.inflate(getLayoutInflater());
        setContentView(setFavoriteRouteBinding.getRoot());

        setSupportActionBar(setFavoriteRouteBinding.toolbarSearchDeparture.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setFavoriteRouteBinding.toolbarSearchDeparture.toolbarText.setText("출발역 입력");
        setFavoriteRouteBinding.toolbarSearchDeparture.toolbarImage.setVisibility(View.GONE);
        setFavoriteRouteBinding.toolbarSearchDeparture.toolbarButton.setVisibility(View.GONE);

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 터치 이벤트가 발생시 키보드를 숨기기
                hideKeyboard();
                return false;
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        setFavoriteRouteBinding.searchViewDeparture.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                                StringBuilder result = new StringBuilder();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Station station = document.toObject(Station.class);

                                    //TODO 받은 station(검색결과)을 화면에 어떻게 띄울지
                                    result.append("역 이름: ").append(station.getStationName()).append("\n");
                                    result.append("역 코드: ").append(station.getStationCode()).append("\n");
                                    result.append("노선 이름: ").append(station.getLineNum()).append("\n");
                                    result.append("철도 운영 기관 코드: ").append(station.getRailOpr()).append("\n\n");
                                }

                                Log.d("MAKAR", "검색 결과:\n" + result.toString());
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