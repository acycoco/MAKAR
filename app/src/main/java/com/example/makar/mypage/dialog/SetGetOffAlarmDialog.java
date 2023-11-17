package com.example.makar.mypage.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.makar.data.SetAlarmDialog;
import com.example.makar.main.MainActivity;

public class SetGetOffAlarmDialog extends Dialog implements SetAlarmDialog {
    private Context context;
    private Button positiveBtn, negativeBtn;
    static Button setAlarmTimeBtn;
    static String alarmTime = MainActivity.alarmTime;

    public SetGetOffAlarmDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }
}
