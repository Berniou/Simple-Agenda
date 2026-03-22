package com.simpleagenda.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(Task task);

    @Query("SELECT * FROM tasks WHERE id NOT IN (SELECT taskId FROM scheduled_tasks) ORDER BY id DESC")
    LiveData<List<Task>> observeBacklog();

    @Query("SELECT * FROM tasks WHERE id NOT IN (SELECT taskId FROM scheduled_tasks) ORDER BY id DESC")
    List<Task> getBacklogSync();

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getById(long id);

    @Query("SELECT * FROM tasks WHERE id IN (:ids)")
    List<Task> getByIds(List<Long> ids);
}
