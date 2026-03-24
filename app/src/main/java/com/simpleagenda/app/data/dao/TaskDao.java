package com.simpleagenda.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.model.TaskCategory;

import java.util.Date;
import java.util.List;

@Dao
public interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY startTime ASC")
    LiveData<List<Task>> getAllTasks();
    
    @Query("SELECT * FROM tasks WHERE date(startTime) = date(:date) ORDER BY startTime ASC")
    LiveData<List<Task>> getTasksForDate(Date date);
    
    @Query("SELECT * FROM tasks WHERE date(startTime) >= date(:startDate) AND date(startTime) <= date(:endDate) ORDER BY startTime ASC")
    LiveData<List<Task>> getTasksForDateRange(Date startDate, Date endDate);
    
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY startTime ASC")
    LiveData<List<Task>> getTasksByCategory(TaskCategory category);
    
    @Query("SELECT * FROM tasks WHERE isCompleted = :completed ORDER BY startTime ASC")
    LiveData<List<Task>> getTasksByCompletionStatus(boolean completed);
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    LiveData<Task> getTaskById(Long id);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(Task task);
    
    @Update
    void updateTask(Task task);
    
    @Delete
    void deleteTask(Task task);
    
    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteTaskById(Long id);
    
    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    void updateTaskCompletion(Long id, boolean completed);
}
