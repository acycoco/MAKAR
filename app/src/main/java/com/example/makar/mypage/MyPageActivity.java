package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.makar.databinding.ActivityMyPageBinding;
import com.example.makar.mypage.dialog.LogoutDialog;
import com.example.makar.mypage.dialog.SetGetOffAlarmDialog;

public class MyPageActivity extends AppCompatActivity {

    ActivityMyPageBinding myPageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPageBinding = ActivityMyPageBinding.inflate(getLayoutInflater());
        setContentView(myPageBinding.getRoot());

        setSupportActionBar(myPageBinding.toolbarMyPage.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        myPageBinding.toolbarMyPage.toolbarText.setText("마이페이지");
        myPageBinding.toolbarMyPage.toolbarImage.setVisibility(View.GONE);
        myPageBinding.toolbarMyPage.toolbarButton.setVisibility(View.GONE);

        myPageBinding.getOffSettingButton.setOnClickListener(view -> {
            setGetOffAlarm();
        });

        myPageBinding.favoriteStationSettingButton.setOnClickListener(view -> {
            updateUI(SetFavoriteStationActivity.class);
        });

        myPageBinding.favoriteRouteSettingButton.setOnClickListener(view -> {
            updateUI(SetFavoriteRouteActivity.class);
        });

        myPageBinding.termsOfServiceButton.setOnClickListener(view -> {
            updateUI(TermsOfServiceActivity.class);
        });

        myPageBinding.privacyPolicyTextButton.setOnClickListener(view -> {
            updateUI(PrivatePolicyActivity.class);
        });

        myPageBinding.openSourceUsageInformationButton.setOnClickListener(view -> {
            updateUI(OpenSourceLicenseActivity.class);
        });

        myPageBinding.logoutButton.setOnClickListener(view -> {
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
}