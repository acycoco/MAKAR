package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.makar.data.ActivityUtil;
import com.example.makar.databinding.ActivityMyPageBinding;
import com.example.makar.mypage.dialog.LogoutDialog;
import com.example.makar.mypage.dialog.SetGetOffAlarmDialog;

public class MyPageActivity extends AppCompatActivity {

    ActivityMyPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActionBar();
        ActivityUtil.setToolbar(binding.toolbarMyPage, "마이페이지");

        binding.getOffSettingButton.setOnClickListener(view -> {
            setGetOffAlarm();
        });

        binding.favoriteStationSettingButton.setOnClickListener(view -> {
            updateUI(SetFavoriteStationActivity.class);
        });

        binding.favoriteRouteSettingButton.setOnClickListener(view -> {
            updateUI(SetFavoriteRouteActivity.class);
        });

        binding.termsOfServiceButton.setOnClickListener(view -> {
            updateUI(TermsOfServiceActivity.class);
        });

        binding.privacyPolicyTextButton.setOnClickListener(view -> {
            updateUI(PrivatePolicyActivity.class);
        });

        binding.openSourceUsageInformationButton.setOnClickListener(view -> {
            updateUI(OpenSourceLicenseActivity.class);
        });

        binding.logoutButton.setOnClickListener(view -> {
            LogoutDialog logoutDialog = new LogoutDialog(this);
            logoutDialog.show();
        });
    }

    //하차 알림 설정 다이얼로그
    private void setGetOffAlarm() {
        SetGetOffAlarmDialog setGetOffAlarmDialog = new SetGetOffAlarmDialog(this);
        setGetOffAlarmDialog.show();
    }

    private void updateUI(Class contextClass) {
        startActivity(new Intent(MyPageActivity.this, contextClass));
    }


    //toolbar
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
        setSupportActionBar(binding.toolbarMyPage.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}