package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.makar.Data.Station;
import com.example.makar.databinding.ActivitySetRouteBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class SetRouteActivity extends AppCompatActivity {

    ActivitySetRouteBinding setRouteBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO 앱이 시작화면에 초기화하는 코드 -> 나중에 옮겨야됨 (확실히 필요한지는 모르겠음)
//        FirebaseApp.initializeApp(this);

        setRouteBinding = ActivitySetRouteBinding.inflate(getLayoutInflater());
        setContentView(setRouteBinding.getRoot());

        setSupportActionBar(setRouteBinding.toolbarSetRoute.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled (true);

        setRouteBinding.toolbarSetRoute.toolbarText.setText("경로 설정하기");
        setRouteBinding.toolbarSetRoute.toolbarImage.setVisibility(View.GONE);
        setRouteBinding.toolbarSetRoute.toolbarButton.setVisibility(View.GONE);

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 터치 이벤트가 발생시 키보드를 숨기기
                hideKeyboard();
                return false;
            }
        });
        
        //역 엑셀 파일을 db에 올리는 코드 (db초기화 시에만 씀)
//        DataConverter databaseConverter = new DataConverter(this);
//        databaseConverter.readExcelFileAndSave();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        setRouteBinding.searchDeparture.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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


        setRouteBinding.searchDestination.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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


        //경로 찾기 버튼 클릭 리스너
        setRouteBinding.searchRouteBtn.setOnClickListener(view -> {
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // toolbar
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