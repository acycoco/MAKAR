package com.example.makar.mypage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.makar.R;
import com.example.makar.data.ActivityUtil;
import com.example.makar.data.Station;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetFavoriteStationBinding;
import com.example.makar.main.MainActivity;
import com.example.makar.onboarding.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SetFavoriteStationActivity extends AppCompatActivity {

    //임시 즐겨찾는 역
    public static Station homeStation, schoolStation;
    private Boolean editMode = false;

    ActivitySetFavoriteStationBinding binding;
    private User user = MainActivity.user;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetFavoriteStationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActionBar(); //actionBar 변경
        ActivityUtil.setToolbar(binding.toolbarSetFavoriteStation, "자주 가는 역 설정");
        ActivityUtil.setHideKeyboard(binding.getRoot());

        homeStation = user.getHomeStation();
        Log.d("MAKARTEST", "setFavoriteStation : home : " + homeStation);
        schoolStation = user.getSchoolStation();
        Log.d("MAKARTEST", "setFavoriteStation : school : " + schoolStation);

        //즐겨찾는 역 유무에 따라 편집 모드 변경
        setEditMode();


        binding.homeSearchButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SearchHomeActivity.class));
        });

        binding.schoolSearchButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SearchSchoolActivity.class));
        });

        binding.textViewHome.setOnClickListener(view -> {
            if (editMode) {
                startActivity(new Intent(this, SearchHomeActivity.class));
            }
        });

        binding.textViewSchool.setOnClickListener(view -> {
            if (editMode) {
                startActivity(new Intent(this, SearchSchoolActivity.class));
            }
        });

        //자주 가는 역 등록하기 버튼 클릭 리스너
        binding.setFavoriteStationBtn.setOnClickListener(view -> {
            if (!editMode) {
                editMode = true;
                binding.setFavoriteStationBtn.setText("등록하기");
                binding.homeSearchButton.setVisibility(View.VISIBLE);
                binding.schoolSearchButton.setVisibility(View.VISIBLE);
            } else {
                if (homeStation != null && schoolStation != null) {
                    firebaseFirestore.collection("users")
                            .whereEqualTo("userUId", LoginActivity.userUId)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            // 값이 존재하는 경우, 해당 데이터를 수정
                                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);

                                            //homeStation 수정
                                            documentSnapshot.getReference().update("homeStation", homeStation).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentSnapshot.getId());
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("MAKAR", "Firestore에 사용자 데이터 수정 중 오류 발생: " + e.getMessage());
                                                }
                                            });
                                            //schoolStation 수정
                                            documentSnapshot.getReference().update("schoolStation", schoolStation).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentSnapshot.getId());
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("MAKAR", "Firestore에 사용자 데이터 수정 중 오류 발생: " + e.getMessage());
                                                }
                                            });
                                        } else {
                                            // 값이 존재하지 않는 경우, 새로운 사용자 데이터 생성
                                            firebaseFirestore.collection("users")
                                                    .add(user)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d("MAKAR", "새로운 사용자 데이터가 Firestore에 추가되었습니다. ID: " + documentReference.getId());

                                                            documentReference.update("homeStation", homeStation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentReference.getId());
                                                                }
                                                            });
                                                            documentReference.update("schoolStation", schoolStation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentReference.getId());
                                                                }
                                                            });

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("MAKAR", "Firestore에 사용자 데이터 추가 중 오류 발생: " + e.getMessage());
                                                        }
                                                    });
                                        }
                                    } else {
                                        Log.e("MAKAR", "Firestore에서 사용자 데이터 검색 중 오류 발생: " + task.getException().getMessage());
                                    }
                                }
                            });
                    Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_station_toast, Toast.LENGTH_SHORT).show();
                    finish();
                } else if (homeStation == null) {
                    Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_error_toast_1, Toast.LENGTH_SHORT).show();
                } else if (schoolStation == null) {
                    Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_error_toast_2, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_error_toast_3, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //즐겨찾는 역 텍스트 수정
        setFavoriteStationText();
    }

    @SuppressLint("ResourceAsColor")
    private void setFavoriteStationText() {
        TextView textViewHome = binding.textViewHome;
        TextView textViewSchool = binding.textViewSchool;

        textViewHome.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
        textViewSchool.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));

        if (homeStation != null) {
            textViewHome.setText(" " + homeStation.getFullName());
        } else {
            textViewHome.setText(R.string.home_station_hint);
        }

        if (schoolStation != null) {
            textViewSchool.setText(" " + schoolStation.getFullName());
        } else {
            textViewSchool.setText(R.string.school_station_hint);
        }
    }

    private void setEditMode() {
        if (homeStation == null && schoolStation == null) {
            editMode = true;
            binding.textViewHome.setText(R.string.home_station_hint);
            binding.textViewSchool.setText(R.string.school_station_hint);
            binding.homeSearchButton.setVisibility(View.VISIBLE);
            binding.schoolSearchButton.setVisibility(View.VISIBLE);
            binding.setFavoriteStationBtn.setText("등록하기");
        } else {
            editMode = false;
            binding.homeSearchButton.setVisibility(View.GONE);
            binding.schoolSearchButton.setVisibility(View.GONE);
            binding.setFavoriteStationBtn.setText("수정하기");
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

    private void setActionBar() {
        setSupportActionBar(binding.toolbarSetFavoriteStation.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

}