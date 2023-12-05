package com.example.makar.main.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.makar.R;
import com.example.makar.main.MainActivity;

public class ResetRouteDialog extends Dialog {
    Button positiveBtn;
    Button negativeBtn;
    MainActivity mainActivity;

    public ResetRouteDialog(@NonNull Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
        setContentView(R.layout.dialog_reset_route);


        positiveBtn = findViewById(R.id.reset_route_btn);
        negativeBtn = findViewById(R.id.close_reset_route_btn);


        positiveBtn.setOnClickListener(view -> {
            //경로 초기화
            mainActivity.onResetRouteBtnClicked();
            dismiss();
        });

        negativeBtn.setOnClickListener(view -> {
            dismiss();
        });
    }
}
