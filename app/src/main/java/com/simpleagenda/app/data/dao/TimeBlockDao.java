package com.simpleagenda.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.model.TaskCategory;

import java.util.List;

@Dao
public interface TimeBlockDao {
    
    @Query("SELECT * FROM time_blocks ORDER BY hour ASC")
    LiveData<List<TimeBlock>> getAllTimeBlocks();
    
    @Query("SELECT * FROM time_blocks WHERE isScheduled = 0 ORDER BY hour ASC")
    LiveData<List<TimeBlock>> getUnscheduledTimeBlocks();
    
    @Query("SELECT * FROM time_blocks WHERE isScheduled = 1 ORDER BY hour ASC")
    LiveData<List<TimeBlock>> getScheduledTimeBlocks();
    
    @Query("SELECT * FROM time_blocks WHERE category = :category ORDER BY hour ASC")
    LiveData<List<TimeBlock>> getTimeBlocksByCategory(TaskCategory category);
    
    @Query("SELECT * FROM time_blocks WHERE hour = :hour")
    LiveData<TimeBlock> getTimeBlockByHour(int hour);
    
    @Query("SELECT * FROM time_blocks WHERE id = :id")
    LiveData<TimeBlock> getTimeBlockById(Long id);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTimeBlock(TimeBlock timeBlock);
    
    @Update
    void updateTimeBlock(TimeBlock timeBlock);
    
    @Delete
    void deleteTimeBlock(TimeBlock timeBlock);
    
    @Query("DELETE FROM time_blocks WHERE id = :id")
    void deleteTimeBlockById(Long id);
    
    @Query("UPDATE time_blocks SET isScheduled = :scheduled WHERE id = :id")
    void updateTimeBlockScheduled(Long id, boolean scheduled);
}
