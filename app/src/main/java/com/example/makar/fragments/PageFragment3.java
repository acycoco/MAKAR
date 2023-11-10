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

import com.example.makar.LoginActivity;
import com.example.makar.R;
import com.example.makar.databinding.Onboarding3Binding;

public class PageFragment3 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.onboarding3, container, false);

        Onboarding3Binding binding = Onboarding3Binding.inflate(getLayoutInflater());
        binding.startButton.setOnClickListener(view -> {
            System.out.print("dddd");
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        return rootView;
    }
}
