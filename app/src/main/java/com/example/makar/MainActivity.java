package com.example.makar;

import static com.example.makar.NotificationHelper.showNotification;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.makar.Dialog.SetAlarmDialog;
import com.example.makar.Dialog.SetFavoriteStationDialog;
import com.example.makar.databinding.ActivityMainBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    long leftTime; //막차까지 남은 시간
    String makarTimeString = "2023-11-10 01:58:30"; //임시 막차 시간
    String getOffTimeString = "2023-11-10 01:59:50"; //임시 하차 시간 (막차시간 + 차 탑승 시간 - 하차 알림 시간)
    public static Boolean isRouteSet = false; //막차 알림을 위한 플래그
    public Boolean isGetOffSet = false; //하차 알림을 위한 플래그
    public static String alarmTime = "10"; //설정한 알람 시간
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarRouteMain);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);


        /**==마이페이지==**/
        //툴바 마이페이지 클릭 리스너
        binding.toolbarRouteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /**==경로 설정 Main==**/
        //시간표 버튼 클릭 리스너
        binding.timetableBtn.setOnClickListener(view -> {
        });

        //막차 알림 설정 버튼 클릭 리스너
        binding.setAlarmBtn.setOnClickListener(view -> {
            //현재 alarmTime을 다이얼로그에 넘김
            setAlarm();
            Toast.makeText(MainActivity.this, "설정된 알림 시간 : " + alarmTime, Toast.LENGTH_SHORT).show();
        });

        //경로 변경하기 버튼 클릭 리스너
        binding.changeRouteBtn.setOnClickListener(view -> {
            updateUI(SetRouteActivity.class);
        });


        /**==경로 미설정 Main==**/
        //경로 설정 버튼 클릭 리스너
        binding.setRouteBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SetRouteActivity.class));

            //onStart에서 경로 설정 유무에 따라 아래 코드 실행
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
                        if(leftTime == Long.parseLong(alarmTime)){
                            //막차까지 남은 시간이 지정한 알림 시간이면 notification show
                            showNotification("MAKAR 막차 알림", "막차까지 "+leftTime+"분 남았습니다", MainActivity.this);
                        }
                            //남은 시간 계산
                            long timeDifferenceMinutes = TimeUnit.MILLISECONDS.toMinutes(leftTime);
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
        if (isRouteSet) {
            //route 설정된 메인화면
            binding.timetableBtn.setVisibility(View.VISIBLE);
            binding.mainDestinationText.setText("source  ->  destination"); //출발지, 도착지
            //수정 필요
            binding.mainTitleText.setText("막차까지 "+leftTime+"분 남았습니다"); //막차까지 남은 시간
            binding.mainDestinationText.setText("destination"); //도착지 이름
            binding.changeRouteBtn.setVisibility(View.VISIBLE);
            binding.setAlarmBtn.setVisibility(View.VISIBLE);
            binding.setRouteBtn.setVisibility(View.GONE);
            binding.favoriteRouteText.setVisibility(View.GONE);
            binding.recentRouteText.setVisibility(View.GONE);
        } else {
            //route 미설정 화면
            setFavoriteStation();
            binding.timetableBtn.setVisibility(View.GONE);
            binding.mainDestinationText.setText("출발역  ->  도착역");
            binding.mainTitleText.setText("경로를 설정해주세요");
            binding.mainDestinationText.setText("MAKAR");
            binding.changeRouteBtn.setVisibility(View.GONE);
            binding.setAlarmBtn.setVisibility(View.GONE);
            binding.setRouteBtn.setVisibility(View.VISIBLE);
            binding.favoriteRouteText.setVisibility(View.VISIBLE);
            binding.recentRouteText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);

        return true;
    }

    //자주 가는 역 설정 다이얼로그
    private void setFavoriteStation() {
        SetFavoriteStationDialog setFavoriteStationDialog = new SetFavoriteStationDialog(this);
        setFavoriteStationDialog.show();
    }

    //막차 알림 설정 다이얼로그
    private void setAlarm() {
        SetAlarmDialog setAlarmDialog = new SetAlarmDialog(this);
        setAlarmDialog.show();
    }

    //메인 타이틀 텍스트 동적 변경
    private void changeMainTitleText(long minute) {
        // 문자열 중 %d 부분에 빨간색 스타일 적용
        String formattedText = String.format(getString(R.string.main_title_text), minute);
        SpannableString spannableString = new SpannableString(formattedText);

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        spannableString.setSpan(foregroundColorSpan, 5,
                    6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //string index 직접 접근해서 색 변경

        binding.mainTitleText.setText(spannableString);
    }


    /**
     * 막차 알림 시간 측정
     **/
    //수정 필요
    private long checkNotificationTime(String TimeString) {
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
        return specifiedDateTime.getTime() - currentTime.getTime();
        //밀리 초 차이 비교
    }

    private void setRouteUnset() {
        //조건 추가 필요
        isRouteSet = false;
        //경로 제거 필요
        updateUI(MainActivity.class);
    }

    private void updateUI(Class contextClass) {
        startActivity(new Intent(MainActivity.this, contextClass));
    }
}