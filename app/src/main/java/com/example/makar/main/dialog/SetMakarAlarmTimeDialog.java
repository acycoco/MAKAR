package com.example.makar.main.dialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.makar.data.dialog.SetAlarmTimeDialog;
import com.example.makar.main.MainActivity;
import com.example.makar.R;

public class SetMakarAlarmTimeDialog extends Dialog  implements SetAlarmTimeDialog {
    Context context;
    NumberPicker alarmTimePicker;
    private String[] timeArr = {"10", "20", "30", "40", "50", "60"};

    private Button positiveBtn, negativeBtn;
    private String makarAlarmTime = MainActivity.user.getMakarAlarmTime();


    public SetMakarAlarmTimeDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
        setContentView(R.layout.dialog_set_makar_alarm_time);

        alarmTimePicker = findViewById(R.id.makar_alarm_time_picker);
        alarmTimePicker.setDisplayedValues(timeArr);
        alarmTimePicker.setMinValue(0);
        alarmTimePicker.setMaxValue(5);
        //alarmTimePicker.setValue(Integer.parseInt(alarmTime));

        positiveBtn = findViewById(R.id.set_makar_alarm_time_btn);
        negativeBtn = findViewById(R.id.close_makar_alarm_time_btn);


        alarmTimePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makarAlarmTime = timeArr[newVal];
            }
        });

        positiveBtn.setOnClickListener(view -> {
            //분 전달 -> alarmTime 변경
            sendDataToFirstDialog(makarAlarmTime);
            Toast.makeText(context, "막차 "+makarAlarmTime+"분 전 알림이 울립니다", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        negativeBtn.setOnClickListener(view -> {
            dismiss();
        });
    }

    public void sendDataToFirstDialog(String data) {
       MainActivity.user.setMakarAlarmTime(data);
       SetMakarAlarmDialog.makarAlarmTime = data;
       SetMakarAlarmDialog.setAlarmTimeBtn.setText(data+"분 전 알림");
    }


}
