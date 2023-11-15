package com.example.makar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.example.makar.databinding.ActivityTermsOfServiceBinding;

public class TermsOfServiceActivity extends AppCompatActivity {
    ActivityTermsOfServiceBinding termsOfServiceBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_policy);

        termsOfServiceBinding = ActivityTermsOfServiceBinding.inflate(getLayoutInflater());
        setContentView(termsOfServiceBinding.getRoot());

        setSupportActionBar(termsOfServiceBinding.toolbarTermsOfService.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        termsOfServiceBinding.toolbarTermsOfService.toolbarText.setText("서비스 이용약관");
        termsOfServiceBinding.toolbarTermsOfService.toolbarImage.setVisibility(View.GONE);
        termsOfServiceBinding.toolbarTermsOfService.toolbarButton.setVisibility(View.GONE);

        WebSettings webSettings = termsOfServiceBinding.webViewTermsOfService.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        termsOfServiceBinding.webViewTermsOfService.loadUrl("https://docs.google.com/document/d/e/2PACX-1vRCzWR0UB4YI35JMZFiKsFP1yuJhoCHz52jdQuChF0gb9K0gVA9MyQogTyng0WzGxb9CeR7wrSvQGEU/pub");
        termsOfServiceBinding.webViewTermsOfService.setWebViewClient(new WebViewClient());

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