package com.example.makar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import com.example.makar.Dialog.SetAlarmDialog;
import com.example.makar.Dialog.SetFavoriteStationDialog;
import com.example.makar.databinding.ActivityMainBinding;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    String makarTimeString = "2023-11-09 18:31:30"; //임시 막차 시간
    String getOffTimeString = "2023-11-09 18:31:50"; //임시 하차 시간 (막차시간 + 차 탑승 시간 - 하차 알림 시간)
    public static Boolean isRouteSet = false; //막차 알림을 위한 플래그
    public Boolean isGetOffSet = false; //하차 알림을 위한 플래그
    public static String alarmTime = "10"; //설정한 알람 시간
    private ActivityMainBinding binding;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarRouteMain);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        /**==경로 설정 Main==**/
        //시간표 버튼 클릭 리스너
        binding.timetableBtn.setOnClickListener(view ->{
        });

        //막차 알림 설정 버튼 클릭 리스너
        binding.setAlarmBtn.setOnClickListener(view ->{
            //현재 alarmTime을 다이얼로그에 넘김
            setAlarm();
            Toast.makeText(MainActivity.this, "설정된 알림 시간 : "+alarmTime, Toast.LENGTH_SHORT).show();
        });

        //경로 변경하기 버튼 클릭 리스너
        binding.changeRouteBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SetRouteActivity.class));
        });


        /**==경로 미설정 Main==**/
        //경로 설정 버튼 클릭 리스너
        binding.setRouteBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SetRouteActivity.class));

            //막차 set 유무 따라
            isRouteSet = true;
            isGetOffSet = true;
            startNotification();
        });
    }

    /**비동기_막차알림 실행 **/
    private void startNotification(){
        Runnable runnable;
        Handler handler = new Handler();
        runnable = new Runnable() { //비동기
            @Override
            public void run() {
                if(isRouteSet){
                    if(checkNotificationTime(getCurrentTime(), makarTimeString)) {
                        //현재 시간이 (막차시간 - alarmTime)이면 notification show
                        showNotification("1", "MAKAR 막차 알림", "막차까지 %d분 남았습니다");
                        setRouteUnset();
                    }
                }
                else if(isGetOffSet){
                    if(checkNotificationTime(getCurrentTime(), getOffTimeString)) {
                        //현재 시간이 하차 시간이면 notification show
                        showNotification("1", "MAKAR 하차 알림", "하차까지 %d분 남았습니다"); //text 수정 필요
                        isGetOffSet = false;
                    }
                }
                //notification 이후 경로 설정 해제, runnable remove
                else{
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
        if(isRouteSet){
            //route 설정된 메인화면
            binding.timetableBtn.setVisibility(View.VISIBLE);
            binding.mainDestinationText.setText("source  ->  destination"); //출발지, 도착지
            //수정 필요
            binding.mainTitleText.setText("막차까지 %d분 남았습니다"); //막차까지 남은 시간
            binding.mainDestinationText.setText("destination"); //도착지 이름
            binding.changeRouteBtn.setVisibility(View.VISIBLE);
            binding.setAlarmBtn.setVisibility(View.VISIBLE);
            binding.setRouteBtn.setVisibility(View.GONE);
            binding.favoriteRouteText.setVisibility(View.GONE);
            binding.recentRouteText.setVisibility(View.GONE);
        }
        else {
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
        getMenuInflater ().inflate (R.menu.menu_toolbar, menu);

        return true;
    }

    //자주 가는 역 설정 다이얼로그
    private void setFavoriteStation(){
        SetFavoriteStationDialog setFavoriteStationDialog = new SetFavoriteStationDialog(this);
        setFavoriteStationDialog.show();
    }

    //막차 알림 설정 다이얼로그
    private void setAlarm(){
        SetAlarmDialog setAlarmDialog = new SetAlarmDialog(this);
        setAlarmDialog.show();
    }



    /**막차 알림 시간 측정**/
    //수정 필요
    private Date getCurrentTime(){
        Date currentTime = new Date();
        Log.d("makar", "currentTime : "+String.valueOf(currentTime));
        return currentTime;
    }

    //수정 필요
    private boolean checkNotificationTime(Date currentTime, String TimeString){
        //현재 시간과 막차 시간 - 알림 시간 비교
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date specifiedDateTime;
        try {
            specifiedDateTime = sdf.parse(TimeString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if(currentTime.after(specifiedDateTime)) {
            //지정된 시간을 지나면 Notification 활성화
            //specifiedDateTime : 막차 시간 - 알림 시간 custom
            return true;
        }
        else return false;
    }


    /**막차 알림 Notification**/
    //하차 알림
    private void showNotification(String channelId, String title, String text) {
        createNotificationChannel(channelId);
        createNotification(title, text);
        setRouteUnset();
        Log.d("makar", "Show Notification");
    }

    private void setRouteUnset() {
        //조건 추가 필요
        //막차시간이 종료되면
        isRouteSet = false;
        //경로 제거 필요
        //noti 띄우고 바로 routeUnset할건지 지정한 막차 시간이 되어서야 routeUnset할건지 상의 필요
        //startActivity(new Intent(this, MainActivity.class));
    }

    //Show Notification
    private void createNotificationChannel(String id) {
        String channelId = id;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "MAKAR";
            String descriptionText = "MAKAR Nofitication";

            NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(descriptionText);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(R.color.main_color);

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, channelId);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
    }

    private void createNotification(String title, String text){
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);

        notificationManager.notify(222, builder.build());
    }
}