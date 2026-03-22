package com.simpleagenda.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "scheduled_tasks",
        foreignKeys = @ForeignKey(
                entity = Task.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "taskId", unique = true),
                @Index("dayMillis")
        }
)
public class ScheduledTask {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long taskId;

    /** Début de journée (minuit local) en millisecondes. */
    private long dayMillis;

    /** Minutes depuis minuit (ex. 8h00 → 480). */
    private int startMinutesFromMidnight;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getDayMillis() {
        return dayMillis;
    }

    public void setDayMillis(long dayMillis) {
        this.dayMillis = dayMillis;
    }

    public int getStartMinutesFromMidnight() {
        return startMinutesFromMidnight;
    }

    public void setStartMinutesFromMidnight(int startMinutesFromMidnight) {
        this.startMinutesFromMidnight = startMinutesFromMidnight;
    }
}
