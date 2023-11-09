package com.example.makar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static NotificationManager notificationManager;
    private static NotificationCompat.Builder builder;


    public static void showNotification(String title, String text, Context context) {
        //make notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "MAKAR";
            String descriptionText = "MAKAR Nofitication";

            NotificationChannel channel = new NotificationChannel("1", name, NotificationManager.IMPORTANCE_DEFAULT);
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

        //show notification
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);

        notificationManager.notify(222, builder.build());

        Log.d("makar", "Show Notification");
    }
}
