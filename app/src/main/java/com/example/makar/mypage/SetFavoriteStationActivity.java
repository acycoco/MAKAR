package com.example.makar.mypage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.makar.R;
import com.example.makar.data.Station;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetFavoriteStationBinding;
import com.example.makar.onboarding.LoginActivity;
import com.example.makar.route.SetRouteActivity;
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

    ActivitySetFavoriteStationBinding setFavoriteStationBinding;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFavoriteStationBinding = ActivitySetFavoriteStationBinding.inflate(getLayoutInflater());
        setContentView(setFavoriteStationBinding.getRoot());

        setActionBar(); //actionBar 변경
        setToolBar(); //toolBar 변경
        setHideKeyBoard();
        getUserData();

        //즐겨찾는 역 유무에 따라 편집 모드 변경
        setEditMode();

        setFavoriteStationBinding.homeSearchButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SearchHomeActivity.class));
        });

        setFavoriteStationBinding.schoolSearchButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SearchSchoolActivity.class));
        });

        setFavoriteStationBinding.textViewHome.setOnClickListener(view -> {
            if (editMode) {
                startActivity(new Intent(this, SearchHomeActivity.class));
            }
        });

        setFavoriteStationBinding.textViewSchool.setOnClickListener(view -> {
            if (editMode) {
                startActivity(new Intent(this, SearchSchoolActivity.class));
            }
        });

        //자주 가는 역 등록하기 버튼 클릭 리스너
        setFavoriteStationBinding.setFavoriteStationBtn.setOnClickListener(view -> {
            if (!editMode) {
                editMode = true;
                setFavoriteStationBinding.setFavoriteStationBtn.setText("등록하기");
                setFavoriteStationBinding.homeSearchButton.setVisibility(View.VISIBLE);
                setFavoriteStationBinding.schoolSearchButton.setVisibility(View.VISIBLE);
            } else {
                if (SearchHomeActivity.homeStation != null && SearchSchoolActivity.schoolStation != null) {
                    User user = new User(LoginActivity.userUId, SearchHomeActivity.homeStation, SearchSchoolActivity.schoolStation, SetRouteActivity.sourceStation, SetRouteActivity.destinationStation);

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
                                            String documentId = documentSnapshot.getId();
                                            firebaseFirestore.collection("users")
                                                    .document(documentId)
                                                    .set(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentId);
                                                            homeStation = SearchHomeActivity.homeStation;
                                                            schoolStation = SearchSchoolActivity.schoolStation;
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
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
                } else if (SearchHomeActivity.homeStation == null) {
                    Toast.makeText(SetFavoriteStationActivity.this, R.string.set_favorite_error_toast_1, Toast.LENGTH_SHORT).show();
                } else if (SearchSchoolActivity.schoolStation == null) {
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

    private void setHideKeyBoard() {
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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

    @SuppressLint("ResourceAsColor")
    private void setFavoriteStationText() {
        TextView textViewHome = setFavoriteStationBinding.textViewHome;
        TextView textViewSchool = setFavoriteStationBinding.textViewSchool;

        if (homeStation != null) {
            if (SearchHomeActivity.homeStation != null) {
                textViewHome.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                textViewHome.setText(" " + SearchHomeActivity.homeStation.getStationName() + "역 " + SearchHomeActivity.homeStation.getLineNum());
            } else {
                textViewHome.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                textViewHome.setText(" " + homeStation.getStationName() + "역 " + homeStation.getLineNum());
            }
        } else if (SearchHomeActivity.homeStation != null) {
            textViewHome.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
            textViewHome.setText(" " + SearchHomeActivity.homeStation.getStationName() + "역 " + SearchHomeActivity.homeStation.getLineNum());
        } else {
            textViewHome.setTextColor(ContextCompat.getColor(this, R.color.dark_gray2));
            textViewHome.setText(R.string.home_station_hint);
        }

        if (schoolStation != null) {
            if (SearchSchoolActivity.schoolStation != null) {
                textViewSchool.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                textViewSchool.setText(" " + SearchSchoolActivity.schoolStation.getStationName() + "역 " + SearchSchoolActivity.schoolStation.getLineNum());
            } else {
                textViewSchool.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                textViewSchool.setText(" " + schoolStation.getStationName() + "역 " + schoolStation.getLineNum());
            }
        } else if (SearchSchoolActivity.schoolStation != null) {
            textViewSchool.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
            textViewSchool.setText(" " + SearchSchoolActivity.schoolStation.getStationName() + "역 " + SearchSchoolActivity.schoolStation.getLineNum());
        } else {
            textViewSchool.setTextColor(ContextCompat.getColor(this, R.color.dark_gray2));
            textViewSchool.setText(R.string.school_station_hint);
        }
    }

    private void setEditMode() {
        if (homeStation == null && schoolStation == null) {
            editMode = true;
            setFavoriteStationBinding.textViewHome.setText(R.string.home_station_hint);
            setFavoriteStationBinding.textViewSchool.setText(R.string.school_station_hint);
            setFavoriteStationBinding.homeSearchButton.setVisibility(View.VISIBLE);
            setFavoriteStationBinding.schoolSearchButton.setVisibility(View.VISIBLE);
            setFavoriteStationBinding.setFavoriteStationBtn.setText("등록하기");
        } else {
            editMode = false;
            setFavoriteStationBinding.homeSearchButton.setVisibility(View.GONE);
            setFavoriteStationBinding.schoolSearchButton.setVisibility(View.GONE);
            setFavoriteStationBinding.setFavoriteStationBtn.setText("수정하기");
        }
    }

    private void getUserData() {
        firebaseFirestore.collection("users")
                .whereEqualTo("userUId", LoginActivity.userUId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                homeStation = documentSnapshot.get("homeStation", Station.class);
                                schoolStation = documentSnapshot.get("schoolStation", Station.class);

                                String homeStationText = homeStation.getStationName() + "역 " + homeStation.getLineNum();
                                String schoolStationText = schoolStation.getStationName() + "역 " + schoolStation.getLineNum();

                                setFavoriteStationBinding.textViewHome.setTextColor(R.color.dark_gray);
                                setFavoriteStationBinding.textViewSchool.setTextColor(R.color.dark_gray);
                                setFavoriteStationBinding.textViewHome.setText(homeStationText);
                                setFavoriteStationBinding.textViewSchool.setText(schoolStationText);

                                setEditMode();
                            }
                        } else {
                            Log.e("MAKAR", "Firestore에서 userData 검색 중 오류 발생: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                SearchHomeActivity.homeStation = null;
                SearchSchoolActivity.schoolStation = null;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setToolBar() {
        setFavoriteStationBinding.toolbarSetFavoriteStation.toolbarText.setText("자주 가는 역 설정");
        setFavoriteStationBinding.toolbarSetFavoriteStation.toolbarImage.setVisibility(View.GONE);
        setFavoriteStationBinding.toolbarSetFavoriteStation.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar() {
        setSupportActionBar(setFavoriteStationBinding.toolbarSetFavoriteStation.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

}