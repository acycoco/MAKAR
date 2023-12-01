package com.example.makar.mypage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.example.makar.data.ActivityUtil;
import com.example.makar.data.Adapter.SearchAdapter;
import com.example.makar.data.Station;
import com.example.makar.databinding.ActivitySearchSchoolBinding;
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

public class SearchSchoolActivity extends AppCompatActivity {
    private ActivitySearchSchoolBinding binding;
    private SearchAdapter adapter;
    private final List<Station> resultList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchSchoolBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActivityUtil();
        setListener();
        setRecyclerView();
        setSearchView();
        setListener();

        db = FirebaseFirestore.getInstance();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarSearchSchool.getRoot());
        ActivityUtil.setToolbar(binding.toolbarSearchSchool, "역 검색");
        ActivityUtil.setHideKeyboard(binding.getRoot());
    }

    // MARK: setListener()
    private void setListener() {
        binding.searchViewSchool.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.isEmpty()) {
                    CollectionReference collectionRef = db.collection("stations");
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
                                Log.d("MAKAR_SEARCH_SCHOOL", "검색 중 오류 발생: ", task.getException());
                            }
                        }
                    });
                }
                return true;
            }
        });
    }

    // MARK: setRecyclerView()
    private void setRecyclerView() {
        RecyclerView recyclerView = binding.searchSchoolRecyclerView;
        adapter = new SearchAdapter(this, resultList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Station station) {
                SetFavoriteStationActivity.schoolStation = station;
                finish();
            }
        });
    }

    // MARK: setSearchView()
    private void setSearchView() {
        SearchView searchView = binding.searchViewSchool;
        searchView.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
    }

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}