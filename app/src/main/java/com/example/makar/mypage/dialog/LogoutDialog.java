package com.example.makar.mypage.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.makar.onboarding.LoginActivity;
import com.example.makar.R;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutDialog extends Dialog {
    Button logoutBtn;
    Button negativeBtn;
    Context context;

    public LogoutDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
        setContentView(R.layout.dialog_logout);


        logoutBtn = findViewById(R.id.logout_btn);
        negativeBtn = findViewById(R.id.cancel_btn);


        logoutBtn.setOnClickListener(view -> {
            context.startActivity(new Intent(context, LoginActivity.class));
            FirebaseAuth.getInstance().signOut();
            dismiss();
        });

        negativeBtn.setOnClickListener(view -> {
            dismiss();
        });
    }
}
