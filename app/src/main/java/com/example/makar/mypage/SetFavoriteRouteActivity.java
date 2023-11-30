package com.example.makar.mypage;

import androidx.annotation.NonNull;
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
import com.example.makar.data.Route;
import com.example.makar.data.Station;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetFavoriteRouteBinding;
import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.main.MainActivity;
import com.example.makar.main.MainActivityChangeView;
import com.example.makar.onboarding.LoginActivity;
import com.example.makar.route.SetRouteActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SetFavoriteRouteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RouteListAdapter adapter;
    private User user = MainActivity.user;
    private List<Route> favoriteRouteArr;
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    ActivitySetFavoriteRouteBinding setFavoriteRouteBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFavoriteRouteBinding = ActivitySetFavoriteRouteBinding.inflate(getLayoutInflater());
        setContentView(setFavoriteRouteBinding.getRoot());

        getFavoriteRouteArr();
        Log.d("dkdkkdkd", user.getFavoriteRouteArr().toString());
        setActionBar();
        setToolBar();
        setRecyclerView();
    }

    private void getFavoriteRouteArr() {
        firebaseFirestore.collection("users")
                .whereEqualTo("userUId", LoginActivity.userUId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);

                                    favoriteRouteArr = documentSnapshot.get("favoriteRouteArr", List.class);

                                    Log.d("MAKARTEST", "MAIN: favoriteRouteArr : " + favoriteRouteArr);
                                    setFavoriteRouteBinding.emptyListText.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                Log.e("MAKAR", "getFavoriteRouteArr 오류: " + e.getMessage());
                                setFavoriteRouteBinding.emptyListText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.e("MAKAR", "Firestore에서 userData 검색 중 오류 발생: " + task.getException().getMessage());
                            setFavoriteRouteBinding.emptyListText.setVisibility(View.VISIBLE);
                        }
                    }
                });
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

    private void setToolBar() {
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarText.setText("즐겨찾는 경로 설정");
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarImage.setVisibility(View.GONE);
        setFavoriteRouteBinding.toolbarSetFavoriteRoute.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar() {
        setSupportActionBar(setFavoriteRouteBinding.toolbarSetFavoriteRoute.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}