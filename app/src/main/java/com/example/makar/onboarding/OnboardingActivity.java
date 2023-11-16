package com.example.makar.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.makar.R;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences를 사용하여 사용자가 앱을 처음 실행하는지 확인
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);

        // 만약 처음 실행이라면 온보딩 화면 표시
        if (isFirstRun) {
            setContentView(R.layout.activity_onboarding);

            ViewPager2 viewPager = findViewById(R.id.pager);
            OnboardingViewPager pagerAdapter = new OnboardingViewPager(this);
            viewPager.setAdapter(pagerAdapter);

            // 사용자가 앱을 처음 실행했음을 기록
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        } else {
            // 처음 실행이 아니면 메인 액티비티로 이동
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // 온보딩 액티비티를 종료하여 뒤로 가기 버튼을 눌렀을 때 다시 보이지 않도록 함
        }
    }
}
