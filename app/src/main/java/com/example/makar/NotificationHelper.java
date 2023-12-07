package com.example.makar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.example.makar.main.MainActivity;

public class NotificationHelper {
    private static NotificationManager notificationManager;
    private static NotificationCompat.Builder builder;


    public static void showNotification(String title, String text, Context context) {
        //make notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "MAKAR";
            String descriptionText = "MAKAR Nofitication";

            NotificationChannel channel = new NotificationChannel("1", name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(descriptionText);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(R.color.main_color);

            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(context, "1");
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        //set intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //show notification
        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);

        notificationManager.notify(1, builder.build());

        Log.d("MAKAR", "Show Notification");
    }
}
