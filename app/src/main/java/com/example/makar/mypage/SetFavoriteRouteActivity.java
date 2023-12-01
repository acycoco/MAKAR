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

import com.example.makar.data.ActivityUtil;

import com.example.makar.data.Adapter.RouteListAdapter;
import com.example.makar.data.Route;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetFavoriteRouteBinding;
import com.example.makar.main.MainActivity;
import com.example.makar.onboarding.LoginActivity;
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
    ActivitySetFavoriteRouteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetFavoriteRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getFavoriteRouteArr();
        Log.d("dkdkkdkd", user.getFavoriteRouteArr().toString());

        setActivityUtil();
        setRecyclerView();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarSetFavoriteRoute.getRoot());
        ActivityUtil.setToolbar(binding.toolbarSetFavoriteRoute, "즐겨찾는 경로 설정");
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
                                    binding.emptyListText.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                Log.e("MAKAR", "getFavoriteRouteArr 오류: " + e.getMessage());
                                binding.emptyListText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.e("MAKAR", "Firestore에서 userData 검색 중 오류 발생: " + task.getException().getMessage());
                            binding.emptyListText.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void setRecyclerView() {
        recyclerView = binding.setFavoriteStationRecyclerView;
        if (user.getFavoriteRouteArr() != null) {
            adapter = new RouteListAdapter(this, user.getFavoriteRouteArr());
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