package com.example.makar.route;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.R;
import com.example.makar.data.ActivityUtil;
import com.example.makar.data.Adapter.RouteAdapter;
import com.example.makar.data.DataConverter;
import com.example.makar.data.Station;
import com.example.makar.data.Route;
import com.example.makar.data.User;
import com.example.makar.databinding.ActivitySetRouteBinding;
import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.main.MainActivity;
import com.example.makar.onboarding.LoginActivity;
import com.example.makar.route.listener.OnBookmarkClickListener;
import com.example.makar.route.listener.OnRouteClickListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class SetRouteActivity extends AppCompatActivity {

    ActivitySetRouteBinding binding;
    RouteRecyclerViewItemBinding recyclerViewItemBinding;

    //임시 출발지, 목적지 변수
    public static Station sourceStation, destinationStation;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private User user = MainActivity.user;
    public static Route selectedRoute;
    public List<Route> resultList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    public static Station briefToSourceStation;
    public static Station briefToDestinationStation;

    private ApiManager apiManager;
    private MakarManager makarManager;
    private TransferManager transferManager;
    private RouteManager routeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //TODO 앱이 시작화면에 초기화하는 코드 -> 나중에 옮겨야됨 (확실히 필요한지는 모르겠음)
//        FirebaseApp.initializeApp(this);

        setActivityUtil();
        setButtonListener();
        setRecyclerView();

        // 출발역, 도착역 데이터가 있다면 받아오기
        sourceStation = user.getSourceStation();
        destinationStation = user.getDestinationStation();

        apiManager = new ApiManager(new ObjectMapper());
        makarManager = new MakarManager(apiManager);
        transferManager = new TransferManager();
        routeManager = new RouteManager(apiManager, makarManager, transferManager);

        //역 엑셀 파일을 db에 올리는 코드 (db초기화 시에만 씀)
//        DataConverter databaseConverter = new DataConverter(this);
////        databaseConverter.readExcelFileAndSave();
////        databaseConverter.createUniqueStationExcelFile();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                databaseConverter.readUniqueStationNameAndSearchStation();
////                databaseConverter.addCleanStationNameAtDB();
////                databaseConverter.createUniqueStationExcelFile();
////                databaseConverter.validateOdsayStationsDataFromDB();
////                databaseConverter.modifyOdsayStationData();
////                databaseConverter.updateStationsCollection();
////                databaseConverter.readExcelFileAndSaveLineMap(2);
////                databaseConverter.copyField("1", "2", "1신창");
////                databaseConverter.copyFieldToAnotherDocument("1", "1", 5, "5하남검단산");
////                databaseConverter.saveNewLine(1, "1", "2", "1신창");
////                databaseConverter.saveReverseTransferInfo();
////                databaseConverter.validateTransferInfo();
////                databaseConverter.validateLineSequences2();
////                databaseConverter.validateLineSequences33("1신창");
//            }
//        }).start();

    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarSetRoute.getRoot());
        ActivityUtil.setToolbar(binding.toolbarSetRoute, "경로 설정하기");
        ActivityUtil.setHideKeyboard(binding.getRoot());
    }

    // MARK: setButtonListener()
    private void setButtonListener() {
        binding.searchSourceButton.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchSourceActivity.class));
        });

        binding.searchDestinationButton.setOnClickListener(view -> {
            startActivity(new Intent(SetRouteActivity.this, SearchDestinationActivity.class));
        });

        //경로 찾기 버튼 클릭 리스너
        binding.searchRouteBtn.setOnClickListener(view -> {
            // 클릭 이벤트 발생 시 새로운 스레드에서 searchRoute 메서드를 실행
            if (sourceStation != null && destinationStation != null && !Objects.equals(sourceStation.getStationName(), destinationStation.getOdsayStationName())) {
                resultList.clear();
                executeSearchRoute();
            } else if (sourceStation == null) {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_1, Toast.LENGTH_SHORT).show();
            } else if (destinationStation == null) {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_2, Toast.LENGTH_SHORT).show();
            } else if (Objects.equals(sourceStation.getStationName(), destinationStation.getStationName())) {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_3, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SetRouteActivity.this, R.string.set_route_error_toast_4, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //sourceBtn, destinationBtn text 변경
        setSearchViewText();
    }

    private void executeSearchRoute() {
        Log.d("MAKAR_SET_ROUTE", "sourceStation : " + sourceStation.getStationName());
        Log.d("MAKAR_SET_ROUTE", "destinationStation : " + destinationStation.getStationName());
        new Thread(() -> {
            try {
                resultList = routeManager.getRoutes(sourceStation, destinationStation);
                Log.d("MAKAR_SET_ROUTE", "resultList : " + resultList.toString());
                if (resultList == null || resultList.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.routeRecyclerView.setVisibility(View.GONE);
                            binding.nonRouteText.setVisibility(View.VISIBLE);
                            Toast.makeText(SetRouteActivity.this, "검색된 경로가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.routeRecyclerView.setVisibility(View.VISIBLE);
                            binding.nonRouteText.setVisibility(View.GONE);
                        }
                    });
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    setRecyclerView();
                });
            } catch (IOException | ExecutionException | InterruptedException e) {
                Log.e("MAKAR_SET_ROUTE", e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }


    private void setSearchViewText() {
        // 서버에 출발역 저장했을 때
        if (sourceStation != null) {
            binding.searchSourceButton.setText("  " + sourceStation.getFullName());
        } else {
            binding.searchSourceButton.setText("");
        }

        // 서버에 도착역 저장했을 때
        if (destinationStation != null) {
            binding.searchDestinationButton.setText("  " + destinationStation.getFullName());
        } else {
            binding.searchDestinationButton.setText("");
        }
    }

    private void setRecyclerView() {
        recyclerView = binding.routeRecyclerView;
        adapter = new RouteAdapter(this, resultList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerViewItemBinding = RouteRecyclerViewItemBinding.inflate(getLayoutInflater());
        adapter.setOnRouteClickListener(new OnRouteClickListener() {
            @Override
            public void onRouteClick(Route route) {
                selectedRoute = route;

                // briefStation 객체 -> Station 객체
                int briefRouteSize = route.getBriefRoute().size();

                String targetSourceStationName = route.getBriefRoute().get(0).getStationName();
                String targetSourceLineNum = route.getBriefRoute().get(0).getLineNumToString();
                Log.d("MAKAR_SET_ROUTE: B SourceStationName", targetSourceStationName);
                Log.d("MAKAR_SET_ROUTE: B SourceLineNum", targetSourceLineNum);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("stations")
                        .whereEqualTo("odsayStationName", targetSourceStationName)
                        .whereEqualTo("lineNum", targetSourceLineNum)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                    Station station = documentSnapshot.toObject(Station.class);
                                    Log.d("MAKAR_SET_ROUTE: BTS", station.toString());
                                    briefToSourceStation = station;
                                    Log.d("MAKAR_SET_ROUTE: BTS", String.valueOf(briefToSourceStation));
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                Log.d("MAKAR_SET_ROUTE: briefToSourceStation", String.valueOf(briefToSourceStation));

                String targetDestinationStationName = route.getBriefRoute().get(briefRouteSize - 1).getStationName();
                String targetDestinationLineNum = route.getBriefRoute().get(briefRouteSize - 1).getLineNumToString();
                Log.d("MAKAR_SET_ROUTE: B DestinationStationName", targetDestinationStationName);
                Log.d("MAKAR_SET_ROUTE: B DestinationLineNum", targetDestinationLineNum);

                db.collection("stations")
                        .whereEqualTo("odsayStationName", targetDestinationStationName)
                        .whereEqualTo("lineNum", targetDestinationLineNum)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                    Station station = documentSnapshot.toObject(Station.class);
                                    Log.d("RouteClick: BTS", station.toString());
                                    briefToDestinationStation = station;
                                    Log.d("MAKAR_SET_ROUTE: BTS", String.valueOf(briefToDestinationStation));
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 검색 실패 시 처리
                                Log.d("MAKARTEST", "find Destination fail");
                            }
                        });

                user.getRecentRouteArr().add(resultList.get(0));
                user.setSelectedRoute(selectedRoute);
                Log.d("MAKAR_SET_ROUTE", selectedRoute.toString());

                // 사용자를 식별해 데이터 저장
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
                                        //Station 수정
                                        documentSnapshot.getReference().update(
                                                "sourceStation", briefToSourceStation,
                                                "destinationStation", briefToDestinationStation,
                                                "selectedRoute", selectedRoute
                                        ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentSnapshot.getId());
                                                    Log.d("MAKAR", "MAIN: 사용자 selectedRoute : " + user.getSelectedRoute());
                                                } else {
                                                    Log.d("MAKAR", "사용자 데이터 수정 실패: ", task.getException());
                                                }
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

                                                        documentReference.update(
                                                                "sourceStation", briefToSourceStation,
                                                                "destinationStation", briefToDestinationStation,
                                                                "selectedRoute", selectedRoute
                                                        ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentReference.getId());
                                                                    Log.d("MAKAR", "MAIN: 사용자 selectedRoute : " + user.getSelectedRoute());
                                                                } else {
                                                                    Log.d("MAKAR", "사용자 데이터 수정 실패: ", task.getException());
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                    MainActivity.isRouteSet = true;
                                    finish();
                                } else {
                                    Toast.makeText(SetRouteActivity.this, R.string.set_favorite_error_toast_3, Toast.LENGTH_SHORT).show();
                                    Log.e("MAKAR", "Firestore에서 사용자 데이터 검색 중 오류 발생: " + task.getException().getMessage());
                                }
                            }
                        });
            }
        });
        adapter.setOnBookmarkClickListener(new OnBookmarkClickListener() {
            @Override
            public void onBookmarkClick(Route route) {
                Route favoriteRoute1 = user.getFavoriteRoute1();
                Route favoriteRoute2 = user.getFavoriteRoute2();
                Route favoriteRoute3 = user.getFavoriteRoute3();
                if (favoriteRoute1 == null) {
                    favoriteRoute1 = route;
                    Route finalFavoriteRoute = favoriteRoute1;
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
                                            //Station 수정
                                            documentSnapshot.getReference().update(
                                                    "favoriteRoute1", finalFavoriteRoute
                                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    user.setFavoriteRoute1(finalFavoriteRoute);
                                                    if (task.isSuccessful()) {
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentSnapshot.getId());
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "MAIN: 사용자 favoriteRoute1 : " + user.getFavoriteRoute1());
                                                    } else {
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "사용자 데이터 수정 실패: ", task.getException());
                                                    }
                                                }
                                            });
                                        } else {
                                            // 값이 존재하지 않는 경우, 새로운 사용자 데이터 생성
                                            firebaseFirestore.collection("users")
                                                    .add(user)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // MARK: 즐겨찾는 경로 등록
                                                            documentReference.update(
                                                                    "favoriteRoute1", finalFavoriteRoute
                                                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    user.setFavoriteRoute1(finalFavoriteRoute);
                                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentReference.getId());
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("MAKAR", "Firestore에 사용자 데이터 수정 중 오류 발생: " + e.getMessage());
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
                                    }
                                }
                            });
                } else if (favoriteRoute2 == null) {
                    favoriteRoute2 = route;
                    Route finalFavoriteRoute2 = favoriteRoute2;
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
                                            //Station 수정
                                            documentSnapshot.getReference().update(
                                                    "favoriteRoute2", finalFavoriteRoute2
                                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    user.setFavoriteRoute2(finalFavoriteRoute2);
                                                    if (task.isSuccessful()) {
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentSnapshot.getId());
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "MAIN: 사용자 favoriteRoute1 : " + user.getFavoriteRoute2());
                                                    } else {
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "사용자 데이터 수정 실패: ", task.getException());
                                                    }
                                                }
                                            });
                                        } else {
                                            // 값이 존재하지 않는 경우, 새로운 사용자 데이터 생성
                                            firebaseFirestore.collection("users")
                                                    .add(user)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // MARK: 즐겨찾는 경로 등록
                                                            documentReference.update(
                                                                    "favoriteRoute2", finalFavoriteRoute2
                                                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    user.setFavoriteRoute2(finalFavoriteRoute2);
                                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentReference.getId());
                                                                }
                                                          }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("MAKAR", "Firestore에 사용자 데이터 수정 중 오류 발생: " + e.getMessage());
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
                                    }
                                }
                            });
                } else if (favoriteRoute3 == null) {
                    favoriteRoute3 = route;
                    Route finalFavoriteRoute3 = favoriteRoute3;
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
                                            //Station 수정
                                            documentSnapshot.getReference().update(
                                                    "favoriteRoute3", finalFavoriteRoute3
                                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    user.setFavoriteRoute3(finalFavoriteRoute3);
                                                    if (task.isSuccessful()) {
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentSnapshot.getId());
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "MAIN: 사용자 favoriteRoute3 : " + user.getFavoriteRoute3());
                                                    } else {
                                                        Log.d("MAKAR_SET_ROUTE_TEST", "사용자 데이터 수정 실패: ", task.getException());
                                                    }
                                                }
                                            });
                                        } else {
                                            // 값이 존재하지 않는 경우, 새로운 사용자 데이터 생성
                                            firebaseFirestore.collection("users")
                                                    .add(user)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // MARK: 즐겨찾는 경로 등록
                                                            documentReference.update(
                                                                    "favoriteRoute3", finalFavoriteRoute3
                                                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    user.setFavoriteRoute3(finalFavoriteRoute3);
                                                                    Log.d("MAKAR", "사용자 데이터가 Firestore에 수정되었습니다. ID: " + documentReference.getId());
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("MAKAR", "Firestore에 사용자 데이터 수정 중 오류 발생: " + e.getMessage());
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
                                    }
                                }
                            });
                } else {
                    Toast.makeText(SetRouteActivity.this, "최대 즐겨찾기 수를 초과했습니다.", Toast.LENGTH_SHORT).show();
                }
                Log.d("MAKAR_SET_ROUTE_TEST", "favoriteRoute11" + favoriteRoute1);

                Log.d("MAKAR_SET_ROUTE_TEST", "favoriteRoute1" + user.getFavoriteRoute1());
                Log.d("MAKAR_SET_ROUTE_TEST", "favoriteRoute2" + user.getFavoriteRoute2());
                Log.d("MAKAR_SET_ROUTE_TEST", "favoriteRoute3" + user.getFavoriteRoute3());
            }
        });
    }

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}