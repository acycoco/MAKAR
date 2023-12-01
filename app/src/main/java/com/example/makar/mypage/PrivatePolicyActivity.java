package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.example.makar.data.ActivityUtil;
import com.example.makar.databinding.ActivityPrivatePolicyBinding;

public class PrivatePolicyActivity extends AppCompatActivity {
    ActivityPrivatePolicyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPrivatePolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActionBar();
        ActivityUtil.setToolbar(binding.toolbarPrivatePolicy, "개인정보 처리 방침");
        setWebView(); //web setting
    }

    private void setWebView() {
        WebSettings webSettings = binding.webViewPrivatePolicy.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        binding.webViewPrivatePolicy.loadUrl("https://docs.google.com/document/d/e/2PACX-1vTLQ3BLRTtPR63-SpOZqLk4uBKDRx1aLdr1IKPOdLBMFRvXvCjQiRtmBvniVlLykGhpNwx2u6zz9z_a/pub");
        binding.webViewPrivatePolicy.setWebViewClient(new WebViewClient());
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
        setSupportActionBar(binding.toolbarPrivatePolicy.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}