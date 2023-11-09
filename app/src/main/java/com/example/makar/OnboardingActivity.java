package com.example.makar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.makar.databinding.ActivityOnboardingBinding;
import com.example.makar.databinding.ActivitySetRouteBinding;
import com.example.makar.fragments.PageFragment1;
import com.example.makar.fragments.PageFragment2;
import com.example.makar.fragments.PageFragment3;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOnboardingBinding binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.pager.setOffscreenPageLimit(3);

        OnboardingViewPager adapter = new OnboardingViewPager(getSupportFragmentManager(), 1);

        PageFragment1 fragment1 = new PageFragment1();
        adapter.addItem(fragment1);

        PageFragment2 fragment2 = new PageFragment2();
        adapter.addItem(fragment2);

        PageFragment3 fragment3 = new PageFragment3();
        adapter.addItem(fragment3);

        binding.pager.setAdapter(adapter);
    }
}