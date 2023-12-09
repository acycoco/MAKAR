package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.makar.data.ActivityUtil;
import com.example.makar.data.MAKARTimeTable;
import com.example.makar.data.TimeTable;
import com.example.makar.databinding.ActivityMyPageBinding;
import com.example.makar.mypage.dialog.LogoutDialog;
import com.example.makar.mypage.dialog.SetGetOffAlarmDialog;

import org.json.JSONException;

import java.io.IOException;

public class MyPageActivity extends AppCompatActivity {

    ActivityMyPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new MyTask().execute();

        TimeTable timeTable = new TimeTable(this);
        timeTable.TimeTableFormatter();

        setActivityUtil();
        setButtonListener();
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                MAKARTimeTable.main(null); // MAKARTimeTable 클래스의 main 함수 실행
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarMyPage.getRoot());
        ActivityUtil.setToolbar(binding.toolbarMyPage, "마이페이지");
    }

    // MARK: setButtonListener()
    private void setButtonListener() {
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

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}