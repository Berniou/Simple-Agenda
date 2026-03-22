package com.simpleagenda.app.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.simpleagenda.app.R;
import com.simpleagenda.app.ui.alarm.AlarmActivity;
import com.simpleagenda.app.ui.main.MainActivity;

/**
 * Canaux et notifications de rappel (heads-up + plein écran).
 */
public final class NotificationHelper {

    public static final String CHANNEL_TASKS = "channel_tasks";

    private NotificationHelper() {
    }

    public static void ensureChannels(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) {
            return;
        }
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_TASKS,
                context.getString(R.string.channel_tasks_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        ch.setDescription(context.getString(R.string.channel_tasks_desc));
        ch.enableVibration(true);
        nm.createNotificationChannel(ch);
    }

    public static void showTaskReminder(
            @NonNull Context context,
            long scheduledId,
            @NonNull String taskTitle,
            @NonNull String startTimeLabel
    ) {
        ensureChannels(context);

        Intent fullScreen = new Intent(context, AlarmActivity.class);
        fullScreen.putExtra(AlarmActivity.EXTRA_SCHEDULED_ID, scheduledId);
        fullScreen.putExtra(AlarmActivity.EXTRA_TASK_TITLE, taskTitle);
        fullScreen.putExtra(AlarmActivity.EXTRA_START_TIME, startTimeLabel);
        fullScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPi = PendingIntent.getActivity(
                context,
                (int) (scheduledId % Integer.MAX_VALUE),
                fullScreen,
                pendingFlags()
        );

        Intent content = new Intent(context, MainActivity.class);
        PendingIntent contentPi = PendingIntent.getActivity(
                context,
                0,
                content,
                pendingFlags()
        );

        String title = context.getString(R.string.notification_task_title, taskTitle);
        String text = context.getString(R.string.notification_task_text, startTimeLabel);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_TASKS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(contentPi)
                .setFullScreenIntent(fullScreenPi, true);

        NotificationManagerCompat.from(context).notify((int) (scheduledId % Integer.MAX_VALUE), b.build());
    }

    private static int pendingFlags() {
        int f = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            f |= PendingIntent.FLAG_IMMUTABLE;
        }
        return f;
    }
}
