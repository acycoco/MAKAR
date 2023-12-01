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

        ActivityUtil.setActionBar(this, binding.toolbarPrivatePolicy.getRoot());
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

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}