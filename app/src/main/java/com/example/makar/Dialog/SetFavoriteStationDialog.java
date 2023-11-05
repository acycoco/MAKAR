package com.example.makar.Dialog;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.makar.NonRouteMainActivity;
import com.example.makar.R;
import com.example.makar.SetFavoriteStationActivity;

public class SetFavoriteStationDialog extends Dialog {
    Button positiveBtn;
    Button negativeBtn;

    public SetFavoriteStationDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_set_favorite_station);

        positiveBtn = findViewById(R.id.set_favorite_station_btn);
        negativeBtn = findViewById(R.id.close_set_favorite_station_btn);

        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, SetFavoriteStationActivity.class));
                //인자로 들어온 context Activity에서 새로운 Activity start
                dismiss();
            }
        });

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
