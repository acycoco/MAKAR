package com.example.makar.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.makar.R;

public class SetAlarmDialog extends Dialog {
    Button positiveBtn;
    Button negativeBtn;
    Button setAlarmTimeBtn;

    public SetAlarmDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_set_alarm);

        positiveBtn = findViewById(R.id.set_alarm_btn);
        negativeBtn = findViewById(R.id.cancel_alarm_btn);
        setAlarmTimeBtn = findViewById(R.id.set_alarm_time_btn);

        positiveBtn.setOnClickListener(view -> {
                //막차 알림 설정

                dismiss();
                Toast.makeText(context, R.string.set_alarm_success, Toast.LENGTH_SHORT).show();
                Log.d("alarm", "SetAlarm : SUCCESS");
        });

        setAlarmTimeBtn.setOnClickListener(view -> {
            //막차 알림 시간 설정
        });

        negativeBtn.setOnClickListener(view -> {
            dismiss();
        });

    }
}
