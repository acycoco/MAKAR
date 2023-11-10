package com.example.makar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.makar.LoginActivity;
import com.example.makar.R;
import com.example.makar.SignupActivity;
import com.example.makar.databinding.Onboarding1Binding;
import com.example.makar.databinding.Onboarding3Binding;

public class PageFragment3 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.onboarding3, container, false);
        Button startButton = view.findViewById(R.id.start_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager2 viewPager = requireActivity().findViewById(R.id.pager);
                startActivity(new Intent(getActivity(), LoginActivity.class));

            }
        });

        return view;
    }
}
