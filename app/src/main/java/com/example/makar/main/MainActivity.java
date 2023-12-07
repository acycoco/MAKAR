package com.example.makar.main;

import static com.example.makar.NotificationHelper.showNotification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.data.ActivityUtil;
import com.example.makar.data.Route;
import com.example.makar.data.Adapter.RouteListAdapter;
import com.example.makar.data.Station;
import com.example.makar.data.User;
import com.example.makar.main.dialog.ResetRouteDialog;
import com.example.makar.main.dialog.SetMakarAlarmDialog;
import com.example.makar.main.dialog.SetFavoriteStationDialog;
import com.example.makar.R;
import com.example.makar.onboarding.LoginActivity;
import com.example.makar.route.listener.OnRouteListClickListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static boolean notiflag = false; //비동기 진행 유무에 대한 플래그
    private static boolean makarnotiflag = false; //막차 알림 noti 유무에 대한 플래그
    private int leftTime; //막차까지 남은 시간
    private Date makarTime; //임시 막차 시간
    private Date getOffTime; //임시 하차 시간
    public static Boolean isRouteSet = false; //막차 알림을 위한 플래그
    public static Boolean isGetOffSet = false; //하차 알림을 위한 플래그
    private ActivityMainBinding binding;
    public static User user = new User();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    public static List<Route> favoriteRoutes = new ArrayList<>();
    public static List<Route> recentRoutes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActivityUtil();
        setButtonListener();
        setFavoriteStationDialog();
        createUser();
        Log.d("MAKAR_MAIN", user.toString());

        //setRecyclerView(); //경로 관련 recyclerView set
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserData();
        //setRecyclerView(); //경로 관련 recyclerView set
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.mainSetActionBar(this, binding.toolbarMain.getRoot());
        ActivityUtil.mainSetToolbar(binding.toolbarMain);
    }

    // MARK: setButtonListener()
    private void setButtonListener() {
        binding.toolbarMain.toolbarButton.setOnClickListener(view -> {
            updateUI(MyPageActivity.class);
        });

        /**==경로 설정 Main==**/
        //경로초기화 버튼 클릭 리스너
        binding.resetRouteBtn.setOnClickListener(view -> {
            resetRoute();
        });

        //막차 알림 설정 버튼 클릭 리스너
        binding.setAlarmBtn.setOnClickListener(view -> {
            //현재 alarmTime을 다이얼로그에 넘김
            setMakarAlarm();
        });

        //경로 변경하기 버튼 클릭 리스너
        binding.changeRouteBtn.setOnClickListener(view -> {
            updateUI(SetRouteActivity.class);
        });

        /**==경로 미설정 Main==**/
        //경로 설정 버튼 클릭 리스너
        binding.setRouteBtn.setOnClickListener(view -> {
            updateUI(SetRouteActivity.class);
        });
    }

    // MARK: createUser() - 최초 1회 User 객체 생성
    private void createUser() {
        DocumentReference userRef = firebaseFirestore.collection("users").document(LoginActivity.userUId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    // 해당 유저의 정보가 파이어베이스에 저장되어 있지 않은 경우
                    user.setUserUId(LoginActivity.userUId);
                    // User 객체를 파이어베이스에 저장
                    userRef.set(user)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("MAKAR_MAIN_TEST", "User 객체 저장 성공");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MAKAR_MAIN_TEST", "User 객체 저장 실패");
                            });
                } else {
                    user.setUserUId(LoginActivity.userUId);
                }
            } else {
                Log.e("MAKAR_MAIN_TEST", "파이어베이스에서 정보 가져오기 실패");
            }
        });
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

                                // MARK: 즐겨찾는 역 불러와서 user에 저장 - setFavoriteStation
                                Station homeStation = documentSnapshot.get("homeStation", Station.class);
                                Station schoolStation = documentSnapshot.get("schoolStation", Station.class);
                                user.setFavoriteStation(homeStation, schoolStation);
                                // 설정한 즐겨찾는 역 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: Home : " + user.getHomeStation());
                                Log.d("MAKAR_MAIN_TEST", "MAIN: School : " + user.getSchoolStation());

                                // MARK: 설정한 경로 불러와서 user에 저장 - setSelectedRoute
                                Route selectedRoute = documentSnapshot.get("selectedRoute", Route.class);
                                user.setSelectedRoute(selectedRoute);
                                // 설정한 경로 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: selectedRoute : " + user.getSelectedRoute());

                                // MARK: 출발지, 도착지 불러와서 user에 저장
                                Station sourceStation = documentSnapshot.get("sourceStation", Station.class);
                                Station destinationStation = documentSnapshot.get("destinationStation", Station.class);
                                user.setRouteStation(sourceStation, destinationStation);
                                // 설정한 출발지, 도착지 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: sourceStation : " + user.getSourceStation());
                                Log.d("MAKAR_MAIN_TEST", "MAIN: destinationStation : " + user.getDestinationStation());

                                // MARK: 즐겨찾는 경로 불러와서 저장 - favoriteRoute
                                Route favoriteRoute1 = documentSnapshot.get("favoriteRoute1", Route.class);
                                user.setFavoriteRoute1(favoriteRoute1);
                                // 설정한 경로 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: favoriteRoute2 : " + user.getFavoriteRoute1());

                                Route favoriteRoute2 = documentSnapshot.get("favoriteRoute1", Route.class);
                                user.setFavoriteRoute2(favoriteRoute2);
                                // 설정한 경로 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: favoriteRoute2 : " + user.getFavoriteRoute2());

                                Route favoriteRoute3 = documentSnapshot.get("favoriteRoute3", Route.class);
                                user.setFavoriteRoute3(favoriteRoute3);
                                // 설정한 경로 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: favoriteRoute3 : " + user.getFavoriteRoute3());

                                createFavoriteRoutes();

                                // MARK: 최근 경로 불러와서 저장 - recentRoutes
                                Route recentRoute1 = documentSnapshot.get("recentRoute1", Route.class);
                                user.setRecentRoute1(recentRoute1);
                                // 설정한 경로 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: recentRoute1 : " + user.getRecentRoute1());

                                Route recentRoute2 = documentSnapshot.get("recentRoute2", Route.class);
                                user.setRecentRoute2(recentRoute2);
                                // 설정한 경로 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: recentRoute2 : " + user.getRecentRoute2());

                                Route recentRoute3 = documentSnapshot.get("recentRoute3", Route.class);
                                user.setRecentRoute3(recentRoute3);
                                // 설정한 경로 local에 잘 저장됐는지 확인
                                Log.d("MAKAR_MAIN_TEST", "MAIN: recentRoute3 : " + user.getRecentRoute3());

                                createRecentRoutes();

                                // TODO: 최근 경로 불러오기 에러
                                // MARK: 최근 경로 불러와서 저장 - recentRouteArr
                                try {
                                    List<Route> recentRouteArr = new ArrayList<Route>();
                                    recentRouteArr = documentSnapshot.get("recentRouteArr", List.class);
                                    Log.d("MAKAR_MAIN_SUCCESS", "recentRouteArr : " + recentRouteArr);
                                    user.setRecentRouteArr(recentRouteArr);
                                    // 최근 경로 local에 잘 저장됐는지 확인
                                    Log.d("MAKAR_MAIN_TEST", "MAIN: sourceStation : " + user.getRecentRouteArr());
                                } catch (Exception e) {
                                    Log.e("MAKAR_MAIN_ERROR", "recentRouteArr 가져오는 중 오류 발생: " + e.getMessage());
                                }

                                // MARK: 막차, 하차 알림 불러와서 user에 저장
                                int makarAlarmTime = documentSnapshot.get("makarAlarmTime", Integer.class);
                                int getoffAlarmTime = documentSnapshot.get("getOffAlarmTime", Integer.class);
                                if (makarAlarmTime <= 0) makarAlarmTime = 10;
                                if (getoffAlarmTime <= 0) getoffAlarmTime = 10;

                                user.setMakarAlarmTime(makarAlarmTime);
                                user.setGetOffAlarmTime(getoffAlarmTime);
                                Log.d("MAKAR_MAIN_TEST", "MakarAlarmTime : " + user.getMakarAlarmTime());
                                Log.d("MAKAR_MAIN_TEST", "GetOffAlarmTime : " + user.getGetOffAlarmTime());
                                Log.d("MAKAR_MAIN_TEST", "NotiFlag : "+notiflag);
                                Log.d("MAKAR_MAIN_TEST", "MakarNotiFlag : "+makarnotiflag);

                                if (user.getSourceStation() == null || user.getDestinationStation() == null) {
                                    isRouteSet = false;
                                    leftTime = 0;
                                    MainActivityChangeView.changeView(
                                            binding,
                                            isRouteSet,
                                            leftTime,
                                            "",
                                            "");
                                    Log.d("MAKAR_MAIN", "route is UnSet");

                                    //막차, 하차 시간 현재로 설정
                                    makarTime = new Date();
                                    getOffTime = makarTime;
                                    //경로 초기화
                                    //setRouteUnset();
                                    if(selectedRoute != null){
                                        Toast.makeText(MainActivity.this, "경로 설정 중 오류가 발생했습니다\n다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    isRouteSet = true;
                                    isGetOffSet = true;
                                    startNotification();
                                    leftTime = 10;
                                    if(!notiflag) {
                                        notiflag = true;
                                        startNotification();
                                    }


                                    //막차, 하차 시간 설정
                                    makarTime = selectedRoute.getMakarTime();
                                    int alarmTime = user.getSelectedRoute().getTotalTime() - getoffAlarmTime;
                                    getOffTime = setAlarmTime(makarTime, alarmTime); //하차 알림 시간 설정  (makarTime + 차 탑승 시간 - getOffAlarmTime)

                                    Log.d("TIMETEST", "makarTime(Set) : " + makarTime);
                                    Log.d("TIMETEST", "getOffTime(Set) : " + getOffTime);
                                    Log.d("TIMETEST", "alarmTime(Set) : " + alarmTime);

                                    if (alarmTime < 10) {
                                        //탑승시간보다 하차 알림 시간이 이전일 경우 경로 초기화
                                        Toast.makeText(MainActivity.this, "열차 탑승 시간이 하차 알림 시간보다 짧습니다", Toast.LENGTH_SHORT).show();
                                        setRouteUnset();
                                        isGetOffSet = false;
                                    } else {
                                        MainActivityChangeView.changeView(
                                                binding,
                                                isRouteSet,
                                                leftTime,
                                                user.getSourceStation().getStationName() + "역 " + user.getSourceStation().getLineNum(),
                                                user.getDestinationStation().getStationName() + "역 " + user.getDestinationStation().getLineNum());
                                        Log.d("MAKAR", "route is Set");
                                        showNotification("TEST", "TEST", MainActivity.this);
                                    }
                                }
                            }
                        } else {
                            Log.e("MAKAR_MAIN_ERROR", "Firestore에서 userData 검색 중 오류 발생: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void createFavoriteRoutes() {
        if (favoriteRoutes.size() < 3) {
            if (user.getFavoriteRoute1() != null) {
                favoriteRoutes.add(user.getFavoriteRoute1());
                if (user.getFavoriteRoute2() != null) {
                    favoriteRoutes.add((user.getFavoriteRoute2()));
                    if (user.getFavoriteRoute3() != null) {
                        favoriteRoutes.add(user.getFavoriteRoute3());
                    }
                }
            }
        }

        setRecyclerView();
    }

    private void createRecentRoutes() {
        if (recentRoutes.size() < 3) {
            if (user.getRecentRoute1() != null) {
                recentRoutes.add(user.getRecentRoute1());
                if (user.getRecentRoute2() != null) {
                    recentRoutes.add((user.getRecentRoute2()));
                    if (user.getRecentRoute3() != null) {
                        recentRoutes.add(user.getRecentRoute3());
                    }
                }
            }
        }

        setRecyclerView();
    }

    // MARK: 최초 로그인시에만 자주 가는 역 설정 다이얼로그
    private void setFavoriteStationDialog() {
        if (LoginActivity.isFirstLogin && user.getHomeStation() == null && user.getSchoolStation() == null) {
            SetFavoriteStationDialog setFavoriteStationDialog = new SetFavoriteStationDialog(this);
            setFavoriteStationDialog.show();
            LoginActivity.isFirstLogin = false; // 최초 로그인 여부 업데이트
        }
    }

    //막차 알림 설정 다이얼로그
    private void setMakarAlarm() {
        SetMakarAlarmDialog setMakarAlarmDialog = new SetMakarAlarmDialog(this);
        setMakarAlarmDialog.show();
    }

    //경로 초기화 다이얼로그
    private void resetRoute() {
        ResetRouteDialog resetRouteDialog = new ResetRouteDialog(this);
        resetRouteDialog.show();
    }

    public void onResetRouteBtnClicked() {
        //초기화 버튼을 누를 시 경로 초기화 실행
        setRouteUnset();
        isGetOffSet = false;

        //user 객체 초기화
        user.setSelectedRoute(null);
        user.setRouteStation(null, null);

        MainActivityChangeView.changeView(
                binding, false, 0, "", "");
        Toast.makeText(this, R.string.reset_route_toast, Toast.LENGTH_SHORT).show();
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
        binding.mainTitleText.setText(spannableString);
    }


    //비동기_막차알림 실행
    private void startNotification() {
        Runnable runnable;
        Handler handler = new Handler();

        runnable = new Runnable() { //비동기
            @Override
            public void run() {
                if (isRouteSet) {
                    leftTime = checkNotificationTime(makarTime);
                    if (leftTime <= 0) {
                        //막차 시간 달성
                        setRouteUnset();
                        Log.d("MAKAR", "MAKAR: 막차 시간이 되었습니다");
                    } else {
                        //남은 시간 계산
                        int timeDifferenceMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(leftTime);
                        Log.d("MAKAR", "LeftTime : " + timeDifferenceMinutes);

                        //경로는 설정되어있으나 시간 미달
                        if (timeDifferenceMinutes == user.getMakarAlarmTime() && !makarnotiflag) {
                            //막차까지 남은 시간이 지정한 알림 시간이면 notification show
                            showNotification("MAKAR 막차 알림", "막차까지 " + timeDifferenceMinutes + "분 남았습니다", MainActivity.this);
                            makarnotiflag = true;
                        }
                        //title text 변경
                        changeMainTitleText(timeDifferenceMinutes);
                    }
                } else if (isGetOffSet) {
                    if (checkNotificationTime(getOffTime) < 0) {
                        //현재 시간이 하차 알림 시간이면 notification show
                        showNotification("MAKAR 하차 알림", "하차까지 " + user.getGetOffAlarmTime() + "분 남았습니다", MainActivity.this); //text 수정 필요, %d는 설정한 하차 알림 시간(10)
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


    //막차 알림 시간 측정
    private int checkNotificationTime(Date date) {
        Date currentTime = new Date();
        Log.d("MAKAR", "currentTime : " + String.valueOf(currentTime));
        //밀리 초 차이 비교
        return (int) (date.getTime() - currentTime.getTime());
    }

    private void setRouteUnset() {
        isRouteSet = false;

        //TODO 임시 출발지, 도착지 초기화
        SetRouteActivity.sourceStation = null;
        SetRouteActivity.destinationStation = null;
        SetRouteActivity.briefToSourceStation = null;
        SetRouteActivity.briefToDestinationStation = null;
        SetRouteActivity.selectedRoute = null;

        //선택된 루트, 출발역, 도착역 초기화
        deleteStation("sourceStation");
        deleteStation("destinationStation");

        binding.resetRouteBtn.setVisibility(View.GONE);
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
                    documentReference.update("selectedRoute", null); //선택된 루트 초기화
                } else {
                    Log.d("MAKAR", "Station 초기화 중 오류 발생");
                }
            }
        });
    }

    private void updateUI(Class contextClass) {
        startActivity(new Intent(MainActivity.this, contextClass));
    }

    static public Date setAlarmTime(Date date, int alarmTime) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date); //시간 설정
        if (alarmTime >= 10) {
            cal.add(Calendar.MINUTE, alarmTime); //분 연산
            return new Date(String.valueOf(cal.getTime()));
        } else {
            return new Date(String.valueOf(cal.getTime()));
        }
    }

    private void setRecyclerView() {
        // MARK: 최근 경로
        RecyclerView recentRouteRecyclerView = binding.recentRouteRecyclerView;
        RouteListAdapter recentRouteListAdapter = new RouteListAdapter(this, recentRoutes);
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
                            reference.update("selectedRoute", route);
                            reference.update("recentRouteArr", user.getRecentRouteArr());
                        }
                    }
                });
            }
        });

        // MARK: 즐겨찾는 경로
        RecyclerView favoriteRouteRecyclerView = binding.favoriteRouteRecyclerView;
        RouteListAdapter favoriteRouteListAdapter = new RouteListAdapter(this, favoriteRoutes);
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
                            reference.update("favoriteRouteArr", user.getFavoriteRouteArr());

                            //최근 경로에 해당 즐겨찾는 경로 추가
                            addRouteToList(user.getRecentRouteArr(), route);
                            reference.update("recentRouteArr", user.getRecentRouteArr());
                        }
                    }
                });
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