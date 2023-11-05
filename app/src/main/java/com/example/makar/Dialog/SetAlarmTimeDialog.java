package com.example.makar.Dialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.example.makar.R;

public class SetAlarmTimeDialog extends Dialog {
    Context context;
    TimePicker timePicker;
    Button positiveBtn;
    Button negativeBtn;
    int alarmTime = 10;
    private OnDataReceivedListener listener;


    public SetAlarmTimeDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
        setContentView(R.layout.dialog_set_alarm_time);

        timePicker = findViewById(R.id.timepicker);
        timePicker.setIs24HourView(true);
        //picker 단위 커스텀 필요
        positiveBtn = findViewById(R.id.set_alarm_time_btn);
        negativeBtn = findViewById(R.id.close_alarm_time_btn);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    alarmTime = minute;
            }
        });

        positiveBtn.setOnClickListener(view -> {
            //분 전달 -> alarmTime 변경
            sendDataToFirstDialog(String.valueOf(alarmTime));
            Toast.makeText(context, "막차 "+alarmTime+"분 전 알림이 울립니다", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        negativeBtn.setOnClickListener(view -> {
            dismiss();
        });
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.listener = listener;
    }

    public void sendDataToFirstDialog(String data) {
        if (listener != null) {
            listener.onDataReceived(data);
        }
    }

    public interface OnDataReceivedListener {
        void onDataReceived(String data);
    }
}
