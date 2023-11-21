package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.example.makar.R;
import com.example.makar.databinding.ActivityPrivatePolicyBinding;

public class PrivatePolicyActivity extends AppCompatActivity {
    ActivityPrivatePolicyBinding privatePolicyBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privatePolicyBinding = ActivityPrivatePolicyBinding.inflate(getLayoutInflater());
        setContentView(privatePolicyBinding.getRoot());

        setActionBar();
        setToolBar();
        setWebView(); //web setting
    }

    private void setWebView(){
        WebSettings webSettings = privatePolicyBinding.webViewPrivatePolicy.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        privatePolicyBinding.webViewPrivatePolicy.loadUrl("https://docs.google.com/document/d/e/2PACX-1vTLQ3BLRTtPR63-SpOZqLk4uBKDRx1aLdr1IKPOdLBMFRvXvCjQiRtmBvniVlLykGhpNwx2u6zz9z_a/pub");
        privatePolicyBinding.webViewPrivatePolicy.setWebViewClient(new WebViewClient());
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
        privatePolicyBinding.toolbarPrivatePolicy.toolbarText.setText("개인정보 처리 방침");
        privatePolicyBinding.toolbarPrivatePolicy.toolbarImage.setVisibility(View.GONE);
        privatePolicyBinding.toolbarPrivatePolicy.toolbarButton.setVisibility(View.GONE);
    }

    private void setActionBar(){
        setSupportActionBar(privatePolicyBinding.toolbarPrivatePolicy.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}