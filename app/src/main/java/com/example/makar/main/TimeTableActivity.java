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

        setActivityUtil();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarTimeTable.getRoot());
        ActivityUtil.setToolbar(binding.toolbarTimeTable, "시간표");
    }

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}