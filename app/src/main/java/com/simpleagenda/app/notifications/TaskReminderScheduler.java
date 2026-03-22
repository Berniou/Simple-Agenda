package com.simpleagenda.app.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.simpleagenda.app.data.AppDatabase;
import com.simpleagenda.app.data.ScheduledTask;
import com.simpleagenda.app.data.ScheduledTaskDao;
import com.simpleagenda.app.data.ScheduledTaskWithTask;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.data.TaskDao;

import java.util.List;

/**
 * Planifie les alarmes système pour les rappels (15 min avant le début).
 */
public class TaskReminderScheduler {

    private static final long REMINDER_LEAD_MS = 15L * 60L * 1000L;

    private final Context appCtx;
    private final ScheduledTaskDao scheduledDao;
    private final TaskDao taskDao;

    public TaskReminderScheduler(@NonNull Context context) {
        this.appCtx = context.getApplicationContext();
        AppDatabase db = AppDatabase.get(appCtx);
        this.scheduledDao = db.scheduledTaskDao();
        this.taskDao = db.taskDao();
    }

    public void scheduleAllForDay(long dayMillis) {
        List<ScheduledTaskWithTask> list = scheduledDao.getForDaySync(dayMillis);
        for (ScheduledTaskWithTask st : list) {
            scheduleOne(st.getScheduled());
        }
    }

    public void reschedule(long scheduledId) {
        cancel(scheduledId);
        ScheduledTask st = scheduledDao.getById(scheduledId);
        if (st != null) {
            scheduleOne(st);
        }
    }

    public void cancel(long scheduledId) {
        AlarmManager am = (AlarmManager) appCtx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            return;
        }
        PendingIntent pi = alarmPendingIntent(scheduledId);
        am.cancel(pi);
        pi.cancel();
    }

    public void rescheduleAllUpcoming() {
        List<ScheduledTask> all = scheduledDao.getAll();
        for (ScheduledTask st : all) {
            scheduleOne(st);
        }
    }

    private void scheduleOne(@NonNull ScheduledTask st) {
        Task task = taskDao.getById(st.getTaskId());
        if (task == null) {
            return;
        }
        long startMs = st.getDayMillis() + (long) st.getStartMinutesFromMidnight() * 60_000L;
        long trigger = startMs - REMINDER_LEAD_MS;
        if (trigger <= System.currentTimeMillis()) {
            return;
        }
        AlarmManager am = (AlarmManager) appCtx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            return;
        }
        PendingIntent pi = alarmPendingIntent(st.getId());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, trigger, pi);
        }
    }

    private PendingIntent alarmPendingIntent(long scheduledId) {
        Intent intent = new Intent(appCtx, TaskReminderReceiver.class);
        intent.putExtra(TaskReminderReceiver.EXTRA_SCHEDULED_ID, scheduledId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(appCtx, alarmRequestCode(scheduledId), intent, flags);
    }

    private static int alarmRequestCode(long scheduledId) {
        return (int) (scheduledId % Integer.MAX_VALUE);
    }
}
