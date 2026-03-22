package com.simpleagenda.app.ui.alarm;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.simpleagenda.app.R;
import com.simpleagenda.app.databinding.ActivityAlarmBinding;
import com.simpleagenda.app.ui.main.MainActivity;

/**
 * Écran plein écran lors d’un rappel (via {@link android.app.Notification.Builder#setFullScreenIntent}).
 */
public class AlarmActivity extends AppCompatActivity {

    public static final String EXTRA_SCHEDULED_ID = "extra_scheduled_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_START_TIME = "extra_start_time";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAlarmBinding b = ActivityAlarmBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        String title = getIntent().getStringExtra(EXTRA_TASK_TITLE);
        String time = getIntent().getStringExtra(EXTRA_START_TIME);
        if (title != null) {
            b.alarmTitle.setText(title);
        }
        if (time != null) {
            b.alarmTime.setText(getString(R.string.notification_task_text, time));
        }

        b.alarmDismiss.setOnClickListener(v -> finish());

        b.alarmOpenPlanning.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });
    }
}
