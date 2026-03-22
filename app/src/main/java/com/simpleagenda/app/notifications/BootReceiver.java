package com.simpleagenda.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Reprogramme les alarmes après redémarrage.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        TaskReminderScheduler scheduler = new TaskReminderScheduler(context);
        scheduler.rescheduleAllUpcoming();
    }
}
