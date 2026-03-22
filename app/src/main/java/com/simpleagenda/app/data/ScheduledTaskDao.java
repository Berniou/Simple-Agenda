package com.simpleagenda.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduledTaskDao {

    @Insert
    long insert(ScheduledTask scheduled);

    @Update
    void update(ScheduledTask scheduled);

    @Delete
    void delete(ScheduledTask scheduled);

    @Query("SELECT * FROM scheduled_tasks WHERE id = :id")
    ScheduledTask getById(long id);

    @Transaction
    @Query("SELECT * FROM scheduled_tasks WHERE dayMillis = :dayMillis ORDER BY startMinutesFromMidnight ASC")
    LiveData<List<ScheduledTaskWithTask>> observeForDay(long dayMillis);

    @Transaction
    @Query("SELECT * FROM scheduled_tasks WHERE dayMillis = :dayMillis ORDER BY startMinutesFromMidnight ASC")
    List<ScheduledTaskWithTask> getForDaySync(long dayMillis);

    @Query("SELECT * FROM scheduled_tasks")
    List<ScheduledTask> getAll();
}
