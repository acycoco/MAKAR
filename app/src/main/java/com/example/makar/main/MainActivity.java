package com.example.makar.main;

import static com.example.makar.NotificationHelper.showNotification;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.data.Route;
import com.example.makar.data.Adapter.RouteListAdapter;
import com.example.makar.data.Station;
import com.example.makar.data.User;
import com.example.makar.main.dialog.SetMakarAlarmDialog;
import com.example.makar.main.dialog.SetFavoriteStationDialog;
import com.example.makar.R;
import com.example.makar.mypage.SetFavoriteStationActivity;
import com.example.makar.onboarding.LoginActivity;
import com.example.makar.route.OnRouteListClickListener;
import com.example.makar.route.SetRouteActivity;
import com.example.makar.databinding.ActivityMainBinding;
import com.example.makar.mypage.MyPageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private int leftTime; //막차까지 남은 시간
    private String makarTimeString = "2023-11-29 21:27:20"; //임시 막차 시간
    private String getOffTimeString = "2023-11-10 13:59:50"; //임시 하차 시간
    public static Boolean isRouteSet = false; //막차 알림을 위한 플래그
    public static Boolean isGetOffSet = false; //하차 알림을 위한 플래그
    private ActivityMainBinding mainBinding;
    private static List<Route> favoriteRouteArr = new ArrayList<>(3); //즐겨찾는 경로
    //    public static List<Route> recentRouteArr = new ArrayList<>(3); //최근경로
    public static User user = new User(LoginActivity.userUId);

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        LoginActivity.userUId = FirebaseAuth.getInstance().getUid();
        //현재 사용자의 uid get

        setActionBar();
        setToolBar();
        getUserData();
        //setRecyclerView(); //경로 관련 recyclerView set


        mainBinding.toolbarMain.toolbarButton.setOnClickListener(view -> {
            updateUI(MyPageActivity.class);
        });

        /**==경로 설정 Main==**/
        //시간표 버튼 클릭 리스너
        mainBinding.timetableBtn.setOnClickListener(view -> {
            updateUI(TimeTableActivity.class);
        });

        //막차 알림 설정 버튼 클릭 리스너
        mainBinding.setAlarmBtn.setOnClickListener(view -> {
            //현재 alarmTime을 다이얼로그에 넘김
            setMakarAlarm();
        });

        //경로 변경하기 버튼 클릭 리스너
        mainBinding.changeRouteBtn.setOnClickListener(view -> {
            updateUI(SetRouteActivity.class);
        });


        /**==경로 미설정 Main==**/
        //경로 설정 버튼 클릭 리스너
        mainBinding.setRouteBtn.setOnClickListener(view -> {
            updateUI(SetRouteActivity.class);

        });
    }

    /**
     * 비동기_막차알림 실행
     **/
    private void startNotification() {
        Runnable runnable;
        Handler handler = new Handler();

        runnable = new Runnable() { //비동기
            @Override
            public void run() {
                if (isRouteSet) {
                    leftTime = checkNotificationTime(makarTimeString);
                    if (leftTime <= 0) {
                        //막차 시간 달성
                        setRouteUnset();
                        Log.d("MAKAR", "MAKAR: 막차 시간이 되었습니다");
                    } else {
                        //남은 시간 계산
                        int timeDifferenceMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(leftTime);
                        Log.d("MAKAR", "LeftTime : " + timeDifferenceMinutes);

                        //경로는 설정되어있으나 시간 미달
                        if (timeDifferenceMinutes == Integer.parseInt(user.getMakarAlarmTime())) {
                            //막차까지 남은 시간이 지정한 알림 시간이면 notification show
                            showNotification("MAKAR 막차 알림", "막차까지 " + timeDifferenceMinutes + "분 남았습니다", MainActivity.this);
                        }
                        //title text 변경
                        changeMainTitleText(timeDifferenceMinutes);
                    }
                } else if (isGetOffSet) {
                    if (checkNotificationTime(getOffTimeString) < 0) {
                        //현재 시간이 하차 시간이면 notification show
                        showNotification("MAKAR 하차 알림", "하차까지 "+user.getGetOffAlarmTime()+"분 남았습니다", MainActivity.this); //text 수정 필요, %d는 설정한 하차 알림 시간(10)
                        isGetOffSet = false;
                    }
                }
                //notification 이후 경로 설정 해제, runnable remove
                else {
                    handler.removeCallbacks(this);
                    Log.d("MAKAR", "remove runnable");
                    return;
                }
                handler.postDelayed(this, 10000); // 10초마다 체크
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserData();
        //setRecyclerView(); //경로 관련 recyclerView set
        Log.d("MAKAR", "onRecyclerView : userRecent : "+user.getRecentRouteArr());

    }

    //자주 가는 역 설정 다이얼로그
    private void setFavoriteStation() {
        //자주가는 역 설정X, 경로 설정X 일 때, 자주 가는 역 설정 다이얼로그 띄움
        //TODO 자주가는 역 변수 수정 필요
        if (SetFavoriteStationActivity.homeStation == null || SetFavoriteStationActivity.schoolStation == null) {
            SetFavoriteStationDialog setFavoriteStationDialog = new SetFavoriteStationDialog(this);
            setFavoriteStationDialog.show();
        }
    }

    //막차 알림 설정 다이얼로그
    private void setMakarAlarm() {
        SetMakarAlarmDialog setMakarAlarmDialog = new SetMakarAlarmDialog(this);
        setMakarAlarmDialog.show();
    }

    //메인 타이틀 텍스트 동적 변경
    private void changeMainTitleText(int minute) {
        int length = String.valueOf(minute).length();

        // 문자열 중 %d 부분에 빨간색 스타일 적용
        String formattedText = String.format(getString(R.string.main_title_text), minute);
        SpannableString spannableString = new SpannableString(formattedText);

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        spannableString.setSpan(foregroundColorSpan, 5,
                5 + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //string index 직접 접근해서 색 변경
        mainBinding.mainTitleText.setText(spannableString);
    }


    /**
     * 막차 알림 시간 측정
     **/
    //수정 필요
    private int checkNotificationTime(String TimeString) {
        Date currentTime = new Date();
        Log.d("MAKAR", "currentTime : " + String.valueOf(currentTime));

        //현재 시간과 막차 시간 - 알림 시간 비교
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date specifiedDateTime;
        try {
            specifiedDateTime = sdf.parse(TimeString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return (int) (specifiedDateTime.getTime() - currentTime.getTime());
        //밀리 초 차이 비교
    }

    private void setRouteUnset() {
        isRouteSet = false;

        //TODO 임시 출발지, 도착지 초기화
        SetRouteActivity.sourceStation = null;
        SetRouteActivity.destinationStation = null;

        deleteStation("sourceStation");
        deleteStation("destinationStation");
        updateUI(MainActivity.class);
        finish();
    }

    private void deleteStation(String path) {
        Task<QuerySnapshot> usersCollection = firebaseFirestore.collection("users")
                .whereEqualTo("userUId", LoginActivity.userUId).get();
        usersCollection.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    DocumentReference documentReference = documentSnapshot.getReference();

                    documentReference.update(path, null);
                } else {
                    Log.d("MAKAR", "Station 초기화 중 오류 발생");
                }
            }
        });
    }

    private void updateUI(Class contextClass) {
        startActivity(new Intent(MainActivity.this, contextClass));
    }

    private void getUserData() {
        firebaseFirestore.collection("users")
                .whereEqualTo("userUId", LoginActivity.userUId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                //즐겨찾는 역 등록
                                Station homeStation = documentSnapshot.get("homeStation", Station.class);
                                Station schoolStation = documentSnapshot.get("schoolStation", Station.class);
                                user.setFavoriteStation(homeStation, schoolStation);
                                Log.d("MAKARTEST", "MAIN: Home : "+user.getHomeStation());
                                Log.d("MAKARTEST", "MAIN: School : "+user.getSchoolStation());

                                //출발, 도착지 등록
                                Station sourceStation = documentSnapshot.get("sourceStation", Station.class);
                                Station destinationStation = documentSnapshot.get("destinationStation", Station.class);
                                user.setRouteStation(sourceStation, destinationStation);
//                                Route selectedRoute = documentSnapshot.get("selectedRoute", Route.class);
//                                user.setSelectedRoute(selectedRoute);
                                Log.d("MAKAR", "MAIN: Source : "+user.getSourceStation());
                                Log.d("MAKAR", "MAIN: Destination : "+user.getDestinationStation());
                                Log.d("MAKAR", "MAIN: selectedRoute : "+user.getSelectedRoute());

                                //즐겨찾는 경로, 최근 경로 등록
                                user.setRecentRouteArr((List<Route>) documentSnapshot.get("recentRouteArr"));
                                Log.d("MAKARTEST", "user.recentArr : "+user.getRecentRouteArr().toString());
                                user.setFavoriteRouteArr((List<Route>) documentSnapshot.get("favoriteRouteArr"));
                                //막차, 하차 알림
                                String makarAlarmTime = documentSnapshot.get("makarAlarmTime", String.class);
                                String getoffAlarmTime = documentSnapshot.get("getOffAlarmTime", String.class);
                                if(makarAlarmTime == null) makarAlarmTime = "10";
                                if(getoffAlarmTime == null) getoffAlarmTime = "10";
                                user.setMakarAlarmTime(makarAlarmTime);
                                user.setGetOffAlarmTime(getoffAlarmTime);
                                Log.d("MAKARTEST", "MakarAlarmTime : "+user.getMakarAlarmTime());
                                Log.d("MAKARTEST", "GetOffAlarmTime : "+user.getGetOffAlarmTime());


                                if (user.getSourceStation() == null || user.getDestinationStation() == null) {
                                    isRouteSet = false;
                                    leftTime = 0;
                                    MainActivityChangeView.changeView(
                                            mainBinding,
                                            isRouteSet,
                                            leftTime,
                                            "",
                                            "");
                                    Log.d("MAKAR", "route is UnSet");
                                } else {
                                    isRouteSet = true;
                                    isGetOffSet = true;
                                    leftTime = 10;
//                                    startNotification();

                                    //makarTimeString = ""; //막차 시간 설정
                                    //getOffTimeString = ""; //하차 시간 설정  (makarTimeString + 차 탑승 시간 - getOffAlarmTime)

                                    MainActivityChangeView.changeView(
                                            mainBinding,
                                            isRouteSet,
                                            leftTime,
                                            user.getSourceStation().getStationName() + "역 " + user.getSourceStation().getLineNum(),
                                            user.getDestinationStation().getStationName() + "역 " + user.getDestinationStation().getLineNum());
                                    Log.d("MAKAR", "route is Set");
                                }
                            }
                        } else {
                            Log.e("MAKAR", "Firestore에서 userData 검색 중 오류 발생: " + task.getException().getMessage());
                        }
                    }
                });
    }


    private void setActionBar() {
        setSupportActionBar(mainBinding.toolbarMain.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    private void setToolBar() {
        mainBinding.toolbarMain.toolbarText.setVisibility(View.GONE);
    }

    private void setRecyclerView() {
        //최근경로
        RecyclerView recentRouteRecyclerView = mainBinding.recentRouteText;
        RouteListAdapter recentRouteListAdapter = new RouteListAdapter(this, user.getRecentRouteArr());
        Log.d("MAKAR", "onRecyclerView : userRecent : "+user.getRecentRouteArr());
        recentRouteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentRouteRecyclerView.setAdapter(recentRouteListAdapter);
        recentRouteListAdapter.setOnRouteClickListener(new OnRouteListClickListener() {
            @Override
            public void onListRouteClick(Route route) {
                Log.d("MAKAR", route.toString());
                Task<QuerySnapshot> usersCollection = firebaseFirestore.collection("users").whereEqualTo("userUId", LoginActivity.userUId).get();

                //최근 경로 수정
                //TODO: collection 추가 수정 필요
                isRouteSet = true;
                isGetOffSet = true;

                usersCollection.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentReference reference = task.getResult().getDocuments().get(0).getReference();
                            //Route에 출발지, 도착지만 따로 Station type으로 저장
                            reference.update("sourceStation", route.getSourceStation());
                            reference.update("destinationStation", route.getDestinationStation());
                            reference.update("recentRouteArr", user.getRecentRouteArr());
                        }
                    }
                });
//                finish();
            }
        });


        //즐겨찾는 경로
        RecyclerView favoriteRouteRecyclerView = mainBinding.favoriteRouteText;
        RouteListAdapter favoriteRouteListAdapter = new RouteListAdapter(this, favoriteRouteArr);
        favoriteRouteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteRouteRecyclerView.setAdapter(favoriteRouteListAdapter);
        favoriteRouteListAdapter.setOnRouteClickListener(new OnRouteListClickListener() {
            @Override
            public void onListRouteClick(Route route) {
                Log.d("MAKAR", route.toString());
                Task<QuerySnapshot> usersCollection = firebaseFirestore.collection("users").whereEqualTo("userUId", LoginActivity.userUId).get();

                //TODO: collection 추가 수정 필요
                isRouteSet = true;
                isGetOffSet = true;
                usersCollection.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentReference reference = task.getResult().getDocuments().get(0).getReference();
                            //Route에 출발지, 도착지만 따로 Station type으로 저장
                            reference.update("sourceStation", route.getSourceStation());
                            reference.update("destinationStation", route.getDestinationStation());
                            reference.update("favoriteRouteArr", favoriteRouteArr);

                            //최근 경로에 해당 즐겨찾는 경로 추가
                            addRouteToList(user.getRecentRouteArr(), route);
                            reference.update("recentRouteArr", user.getRecentRouteArr());
                        }
                    }
                });
//                finish();
            }
        });
    }

    public static void addRouteToList(List<Route> list, Route route) {
        int size = list.size();
        if (size >= 3) {
            list.set(1, list.get(0));
            list.set(2, list.get(1));
            list.set(0, route);
            for (int i = 3; i < size; i++) {
                list.remove(i);
            }
        } else {
            list.add(route);
        }
        for (int i = 0; i < list.size(); i++) {
            Log.d("MAKAR", "routeList :" + list.get(i));
        }
    }
}