package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.example.makar.R;
import com.example.makar.databinding.ActivityOpenSourceLicenseBinding;

public class OpenSourceLicenseActivity extends AppCompatActivity {
    ActivityOpenSourceLicenseBinding openSourceLicenseBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openSourceLicenseBinding = ActivityOpenSourceLicenseBinding.inflate(getLayoutInflater());
        setContentView(openSourceLicenseBinding.getRoot());

        setActionBar();
        setToolBar();
        setWebView(); //web setting
    }


    private void setWebView(){
        WebSettings webSettings = openSourceLicenseBinding.webViewOpenSourceLicense.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        openSourceLicenseBinding.webViewOpenSourceLicense.loadUrl("https://docs.google.com/document/d/e/2PACX-1vQ7Yca6G3aODYl_5uEJOoZ1RnSmwfDInh22CUlYfwTzsPwcVWiY6EuPqTJe-xdukRjOSviBEBuhDutX/pub");
        openSourceLicenseBinding.webViewOpenSourceLicense.setWebViewClient(new WebViewClient());
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

    private void setToolBar(){
        openSourceLicenseBinding.toolbarOpenSourceLicense.toolbarText.setText("오픈소스 라이선스");
        openSourceLicenseBinding.toolbarOpenSourceLicense.toolbarImage.setVisibility(View.GONE);
        openSourceLicenseBinding.toolbarOpenSourceLicense.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar(){
        setSupportActionBar(openSourceLicenseBinding.toolbarOpenSourceLicense.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}