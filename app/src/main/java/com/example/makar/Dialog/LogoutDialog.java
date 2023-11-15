package com.example.makar.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.makar.LoginActivity;
import com.example.makar.R;

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
            dismiss();
        });

        negativeBtn.setOnClickListener(view -> {
            dismiss();
        });
    }
}
