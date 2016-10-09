package io.sympli.find_e.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.ui.main.Instance;
import io.sympli.find_e.ui.main.MainActivity;
import io.sympli.find_e.ui.main.StartActivity;

public final class NotificationUtils {

    private static final int ID_ERROR = 0;
    private static final int ID_ORDER = 1;

    public static void sendDisconnectedNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID_ORDER);

        Intent notificationIntent = ApplicationController.getLastInstance() == Instance.MAIN ?
                new Intent(context, MainActivity.class) :
                new Intent(context, StartActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        String title = "Connection lost";
        String text = "Tap to open last received tag location";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.aiia_logo)
                .setColor(Color.BLACK)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(intent);
        Notification notification = mBuilder.build();

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (alarmSound != null) {
            notification.sound = alarmSound;
        }

        notification.priority |= Notification.PRIORITY_MAX;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(ID_ORDER, notification);
    }

    public static void removeAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
