package com.example.makar.main.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.makar.main.MainActivity;
import com.example.makar.R;

public class SetAlarmDialog extends Dialog {
    private Context context;
    private Button positiveBtn, negativeBtn;
    static Button setAlarmTimeBtn;
    static String alarmTime = MainActivity.alarmTime;

    public SetAlarmDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
        setContentView(R.layout.dialog_set_alarm);

        positiveBtn = findViewById(R.id.set_alarm_btn);
        negativeBtn = findViewById(R.id.cancel_alarm_btn);
        setAlarmTimeBtn = findViewById(R.id.set_alarm_time_btn);
        //MainActivity에서 받아온 alarmTime으로 텍스트 설정
        setAlarmTimeBtn.setText(alarmTime+"분 전 알림");

        positiveBtn.setOnClickListener(view -> {
            //막차 알림 설정
            sendDataToMainActivity(alarmTime);
            dismiss();
            Toast.makeText(context, R.string.set_alarm_success, Toast.LENGTH_SHORT).show();
            Log.d("alarm", "SetAlarm : SUCCESS");
        });

        setAlarmTimeBtn.setOnClickListener(view -> {
            //막차 알림 시간 설정
            showTimePickerDialog();
        });

        negativeBtn.setOnClickListener(view -> dismiss());

    }

    public void showTimePickerDialog() {
//        BottomSheetDialog setAlarmTimeDialog = new BottomSheetDialog(context);
//              View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_alarm_time, null);
//              setAlarmTimeDialog.setContentView(dialogView);
//              setAlarmTimeDialog.create();
//              setAlarmTimeDialog.show();

              SetAlarmTimeDialog setAlarmTimeDialog = new SetAlarmTimeDialog(context);
              setAlarmTimeDialog.show();
    }

    public void sendDataToMainActivity(String data) {
        MainActivity.alarmTime = data;
    }
}
