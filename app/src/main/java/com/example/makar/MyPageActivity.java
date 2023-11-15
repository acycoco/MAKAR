package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.example.makar.Dialog.LogoutDialog;
import com.example.makar.Dialog.SetAlarmDialog;
import com.example.makar.databinding.ActivityMyPageBinding;

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
            startActivity(new Intent(MyPageActivity.this, SetGetOffActivity.class));
        });

        myPageBinding.favoriteStationSettingButton.setOnClickListener(view -> {
            startActivity(new Intent(MyPageActivity.this, SetFavoriteStationActivity.class));
        });

        myPageBinding.favoriteRouteSettingButton.setOnClickListener(view -> {
            startActivity(new Intent(MyPageActivity.this, SetFavoriteRouteActivity.class));
        });

        myPageBinding.termsOfServiceButton.setOnClickListener(view -> {
            startActivity(new Intent(MyPageActivity.this, TermsOfServiceActivity.class));
        });

        myPageBinding.privacyPolicyTextButton.setOnClickListener(view -> {
            startActivity(new Intent(MyPageActivity.this, PrivatePolicyActivity.class));
        });

        myPageBinding.openSourceUsageInformationButton.setOnClickListener(view -> {
            startActivity(new Intent(MyPageActivity.this, OpenSourceLicenseActivity.class));
        });

        myPageBinding.logoutButton.setOnClickListener(view -> {
            LogoutDialog logoutDialog = new LogoutDialog(this);
            logoutDialog.show();
        });
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