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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.makar.main.dialog.SetMakarAlarmDialog;
import com.example.makar.main.dialog.SetFavoriteStationDialog;
import com.example.makar.R;
import com.example.makar.route.SetRouteActivity;
import com.example.makar.databinding.ActivityMainBinding;
import com.example.makar.mypage.MyPageActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private String destination = "destination"; //임시 도착지 이름
    private String source = "source"; //임시 출발지 이름
    private int leftTime; //막차까지 남은 시간
    private String makarTimeString = "2023-11-19 14:36:30"; //임시 막차 시간
    private String getOffTimeString = "2023-11-10 13:59:50"; //임시 하차 시간
    public static Boolean isRouteSet = false; //막차 알림을 위한 플래그
    private static Boolean isGetOffSet = false; //하차 알림을 위한 플래그
    public static String alarmTime = "10"; //설정한 막차 알람 시간
    public static String getOffAlarmTime = "10"; //하차 알림 시간
    private ActivityMainBinding mainBinding;
    private String userUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        //현재 사용자의 uid get
        userUid = getUserUid();
        //db 접근에 이용

        //경로 설정 유무 체크
        //checkRouteSet();

        //set toolbar
        setSupportActionBar(mainBinding.toolbarMain.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mainBinding.toolbarMain.toolbarText.setVisibility(View.GONE);

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
            setAlarm();
        });

        //경로 변경하기 버튼 클릭 리스너
        mainBinding.changeRouteBtn.setOnClickListener(view -> {
            updateUI(SetRouteActivity.class);
        });


        /**==경로 미설정 Main==**/
        //경로 설정 버튼 클릭 리스너
        mainBinding.setRouteBtn.setOnClickListener(view -> {
            updateUI(SetRouteActivity.class);

            //임시 경로 설정 플래그 수정
            isRouteSet = true;
            isGetOffSet = true;
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
                    if ( leftTime <= 0) {
                        //막차 시간 달성
                        setRouteUnset();
                    } else {
                        //경로는 설정되어있으나 시간 미달
                        if(leftTime == Integer.parseInt(alarmTime)){
                            //막차까지 남은 시간이 지정한 알림 시간이면 notification show
                            showNotification("MAKAR 막차 알림", "막차까지 "+leftTime+"분 남았습니다", MainActivity.this);
                        }
                            //남은 시간 계산
                            int timeDifferenceMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(leftTime);
                            Log.d("makar", "LeftTime : " + timeDifferenceMinutes);
                            //title text 변경
                            changeMainTitleText(timeDifferenceMinutes);
                    }
                } else if (isGetOffSet) {
                    if (checkNotificationTime(getOffTimeString) < 0) {
                        //현재 시간이 하차 시간이면 notification show
                        showNotification("MAKAR 하차 알림", "하차까지 %d분 남았습니다", MainActivity.this); //text 수정 필요, %d는 설정한 하차 알림 시간(10)
                        setRouteUnset();
                        isGetOffSet = false;
                    }
                }
                //notification 이후 경로 설정 해제, runnable remove
                else {
                    handler.removeCallbacks(this);
                    Log.d("makar", "remove runnable");
                }
                handler.postDelayed(this, 10000); // 10초마다 체크
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startNotification();
        if(!MainActivityChangeView.changeView(mainBinding, leftTime, source, destination))
            setFavoriteStation();
        //경로 설정 유무에 따라 view component change
    }

    //자주 가는 역 설정 다이얼로그
    private void setFavoriteStation() {
        //자주가는 역 설정X, 경로 설정X 일 때, 자주 가는 역 설정 다이얼로그 띄움
        //if(/**자주가는 역 설정 안되어있을 시 **/) {
            SetFavoriteStationDialog setFavoriteStationDialog = new SetFavoriteStationDialog(this);
            setFavoriteStationDialog.show();
        //}
    }

    //막차 알림 설정 다이얼로그
    private void setAlarm() {
        SetMakarAlarmDialog setMakarAlarmDialog = new SetMakarAlarmDialog(this);
        setMakarAlarmDialog.show();
    }

    //메인 타이틀 텍스트 동적 변경
    private void changeMainTitleText(int minute) {
        int length = String.valueOf(minute).length();
        Log.d("daeun", String.valueOf(length));

        // 문자열 중 %d 부분에 빨간색 스타일 적용
        String formattedText = String.format(getString(R.string.main_title_text), minute);
        SpannableString spannableString = new SpannableString(formattedText);

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        spannableString.setSpan(foregroundColorSpan, 5,
                   5 + length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //string index 직접 접근해서 색 변경
        mainBinding.mainTitleText.setText(spannableString);
    }


    /**
     * 막차 알림 시간 측정
     **/
    //수정 필요
    private int checkNotificationTime(String TimeString) {
        Date currentTime = new Date();
        Log.d("makar", "currentTime : " + String.valueOf(currentTime));

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
        //조건 추가 필요
        isRouteSet = false;
        //db에서 경로 제거 필요
        updateUI(MainActivity.class);
    }

    private String getUserUid(){
        Intent intent = getIntent();
        return intent.getStringExtra("uid");
    }

    private void updateUI(Class contextClass) {
        startActivity(new Intent(MainActivity.this, contextClass));
        finish();
    }

//    private void checkRouteSet(){
//        if(/**db에 경로 설정이 되어 있으면**/){
//            isRouteSet = true;
//            isGetOffSet = true;
//            //makarTimeString = ""; //막차 시간 설정
//            //getOffTimeString = ""; //하차 시간 설정  (makarTimeString + 차 탑승 시간 - getOffAlarmTime)
//            //source = ""; //출발지 설정
//            //destination = ""; //도착지 설정
//            /**출발지, 도착지 설정을 setRouteActivity에서 한 번에 초기화 할지, MainActivity에서 db에 접근해서 따로 초기화 할지 상의 필요**/
//        }
//    }
}