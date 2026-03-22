package com.simpleagenda.app.data;

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Planification avec la tâche associée (relation Room 1–n exposée en liste).
 */
public class ScheduledTaskWithTask {

    @Embedded
    private ScheduledTask scheduled;

    @Relation(
            parentColumn = "taskId",
            entityColumn = "id"
    )
    private List<Task> tasks;

    public ScheduledTask getScheduled() {
        return scheduled;
    }

    public void setScheduled(ScheduledTask scheduled) {
        this.scheduled = scheduled;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Nullable
    public Task getTask() {
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    public int endMinutesFromMidnight() {
        Task t = getTask();
        if (t == null) {
            return scheduled.getStartMinutesFromMidnight();
        }
        return scheduled.getStartMinutesFromMidnight() + t.durationMinutes();
    }
}
