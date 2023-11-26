package com.example.makar.data.dialog;

import android.content.Context;
import android.widget.Button;

public interface SetAlarmDialog {
    void showTimePickerDialog();
    void sendDataToMainActivity(String data);
}
