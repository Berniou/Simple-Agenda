package com.simpleagenda.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simpleagenda.app.data.AppDatabase;
import com.simpleagenda.app.data.ScheduledTask;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.util.TimeUtils;

public class TaskReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_SCHEDULED_ID = "extra_scheduled_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        long scheduledId = intent.getLongExtra(EXTRA_SCHEDULED_ID, -1L);
        if (scheduledId < 0) {
            return;
        }
        AppDatabase db = AppDatabase.get(context);
        ScheduledTask st = db.scheduledTaskDao().getById(scheduledId);
        if (st == null) {
            return;
        }
        Task task = db.taskDao().getById(st.getTaskId());
        if (task == null) {
            return;
        }
        String time = TimeUtils.formatTime(st.getStartMinutesFromMidnight());
        NotificationHelper.ensureChannels(context);
        NotificationHelper.showTaskReminder(context, scheduledId, task.getTitle(), time);
    }
}
