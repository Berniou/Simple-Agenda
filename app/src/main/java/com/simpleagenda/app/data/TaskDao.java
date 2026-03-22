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

    /** Toutes les tâches créées (bibliothèque, replanifiables). */
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    LiveData<List<Task>> observeAllTasks();

    /**
     * Tâches encore disponibles pour une journée donnée (pas encore placées ce jour-là).
     */
    @Query("SELECT * FROM tasks WHERE id NOT IN (SELECT taskId FROM scheduled_tasks WHERE dayMillis = :dayMillis) ORDER BY id DESC")
    LiveData<List<Task>> observeAvailableForDay(long dayMillis);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getById(long id);

    @Query("SELECT * FROM tasks WHERE id IN (:ids)")
    List<Task> getByIds(List<Long> ids);
}
