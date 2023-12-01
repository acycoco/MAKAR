package com.example.makar.main;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.makar.data.ActivityUtil;
import com.example.makar.databinding.ActivityTimeTableBinding;

public class TimeTableActivity extends AppCompatActivity {
    ActivityTimeTableBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTimeTableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityUtil.setActionBar(this, binding.toolbarTimeTable.getRoot());
        ActivityUtil.setToolbar(binding.toolbarTimeTable, "시간표");
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