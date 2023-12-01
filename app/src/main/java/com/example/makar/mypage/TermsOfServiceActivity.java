package com.example.makar.mypage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.example.makar.data.ActivityUtil;
import com.example.makar.databinding.ActivityTermsOfServiceBinding;

public class TermsOfServiceActivity extends AppCompatActivity {
    ActivityTermsOfServiceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTermsOfServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityUtil.setActionBar(this, binding.toolbarTermsOfService.getRoot());
        ActivityUtil.setToolbar(binding.toolbarTermsOfService, "서비스 이용약관");
        setWebView(); //web setting
    }

    private void setWebView() {
        WebSettings webSettings = binding.webViewTermsOfService.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        binding.webViewTermsOfService.loadUrl("https://docs.google.com/document/d/e/2PACX-1vRCzWR0UB4YI35JMZFiKsFP1yuJhoCHz52jdQuChF0gb9K0gVA9MyQogTyng0WzGxb9CeR7wrSvQGEU/pub");
        binding.webViewTermsOfService.setWebViewClient(new WebViewClient());
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
}