package com.example.makar.main.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.makar.data.dialog.SetAlarmDialog;
import com.example.makar.main.MainActivity;
import com.example.makar.R;
import com.example.makar.onboarding.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SetMakarAlarmDialog extends Dialog implements SetAlarmDialog {
    private Context context;
    private Button positiveBtn, negativeBtn;
    static Button setAlarmTimeBtn;
    static String makarAlarmTime;

    public SetMakarAlarmDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
        setContentView(R.layout.dialog_set_makar_alarm);

        makarAlarmTime = MainActivity.user.getMakarAlarmTime();

        positiveBtn = findViewById(R.id.set_makar_alarm_btn);
        negativeBtn = findViewById(R.id.cancel_makar_alarm_btn);
        setAlarmTimeBtn = findViewById(R.id.set_makar_alarm_time_btn);
        //MainActivity에서 받아온 alarmTime으로 텍스트 설정
        setAlarmTimeBtn.setText(makarAlarmTime+"분 전 알림");

        positiveBtn.setOnClickListener(view -> {
            //막차 알림 설정
            sendDataToMainActivity(makarAlarmTime);
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
              SetMakarAlarmTimeDialog setMakarAlarmTimeDialog = new SetMakarAlarmTimeDialog(context);
              setMakarAlarmTimeDialog.show();
    }

    public void sendDataToMainActivity(String data) {
        Task<QuerySnapshot> usersCollection = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("userUId", LoginActivity.userUId).get();
        usersCollection.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentReference reference = task.getResult().getDocuments().get(0).getReference();
                    reference.update("makarAlarmTime", makarAlarmTime);
                }
            }
        });    }
}
