package com.example.makar.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.makar.Listener.OnDataReceivedListener;
import com.example.makar.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SetAlarmDialog extends Dialog {
    Context context;
    Button positiveBtn;
    Button negativeBtn;
    Button setAlarmTimeBtn;
    static String alarmTime = "10"; //값 저장 후 불러와야 함

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

        positiveBtn.setOnClickListener(view -> {
            //막차 알림 설정
            dismiss();
            Toast.makeText(context, R.string.set_alarm_success, Toast.LENGTH_SHORT).show();
            Log.d("alarm", "SetAlarm : SUCCESS");
        });

        setAlarmTimeBtn.setOnClickListener(view -> {
            //막차 알림 시간 설정
            showTimePickerDialog();
        });

        negativeBtn.setOnClickListener(view -> {
            dismiss();
        });

    }

    public void showTimePickerDialog() {
//        BottomSheetDialog setAlarmTimeDialog = new BottomSheetDialog(context);
//              View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_alarm_time, null);
//              setAlarmTimeDialog.setContentView(dialogView);
//              setAlarmTimeDialog.create();
//              setAlarmTimeDialog.show();

              SetAlarmTimeDialog setAlarmTimeDialog = new SetAlarmTimeDialog(context);
              setAlarmTimeDialog.show();

              setAlarmTimeDialog.setOnDataReceivedListener(new OnDataReceivedListener() {
                  @Override
                  public void onDataReceived(String data) {
                      alarmTime = data;
                      //picker로 정한 시간에 맞춰 alarmTime 변경
                      setAlarmTimeBtn.setText(alarmTime+"분 전 알림");
                  }
              });
    }
}
