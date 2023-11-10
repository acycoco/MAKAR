package com.example.makar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.makar.LoginActivity;
import com.example.makar.R;
import com.example.makar.databinding.Onboarding1Binding;
import com.example.makar.databinding.Onboarding3Binding;

public class PageFragment1 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("dd", "ddd");

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.onboarding1, container,false);
        Onboarding1Binding binding = Onboarding1Binding.inflate(getLayoutInflater());
        binding.skipButton1.setOnClickListener(view -> {
            Log.d("dd", "eeee");
            ViewPager viewPager = getActivity().findViewById(R.id.pager);
            viewPager.setCurrentItem(2);
        });

        return rootView;
    }
}
