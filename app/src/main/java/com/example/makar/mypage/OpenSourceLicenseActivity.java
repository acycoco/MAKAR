package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.example.makar.data.ActivityUtil;
import com.example.makar.databinding.ActivityOpenSourceLicenseBinding;

public class OpenSourceLicenseActivity extends AppCompatActivity {
    ActivityOpenSourceLicenseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOpenSourceLicenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActionBar();
        ActivityUtil.setToolbar(binding.toolbarOpenSourceLicense, "오픈소스 라이선스");
        setWebView(); //web setting
    }

    private void setWebView() {
        WebSettings webSettings = binding.webViewOpenSourceLicense.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        binding.webViewOpenSourceLicense.loadUrl("https://docs.google.com/document/d/e/2PACX-1vQ7Yca6G3aODYl_5uEJOoZ1RnSmwfDInh22CUlYfwTzsPwcVWiY6EuPqTJe-xdukRjOSviBEBuhDutX/pub");
        binding.webViewOpenSourceLicense.setWebViewClient(new WebViewClient());
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
        setSupportActionBar(binding.toolbarOpenSourceLicense.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}