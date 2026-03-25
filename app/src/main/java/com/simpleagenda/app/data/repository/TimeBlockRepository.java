package com.simpleagenda.app.data.repository;

import androidx.lifecycle.LiveData;

import com.simpleagenda.app.data.dao.TimeBlockDao;
import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.model.TaskCategory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeBlockRepository {
    private final TimeBlockDao timeBlockDao;
    private final ExecutorService executorService;
    
    public TimeBlockRepository(TimeBlockDao timeBlockDao) {
        this.timeBlockDao = timeBlockDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<TimeBlock>> getAllTimeBlocks() {
        return timeBlockDao.getAllTimeBlocks();
    }
    
    public LiveData<List<TimeBlock>> getUnscheduledTimeBlocks() {
        return timeBlockDao.getUnscheduledTimeBlocks();
    }
    
    public LiveData<List<TimeBlock>> getScheduledTimeBlocks() {
        return timeBlockDao.getScheduledTimeBlocks();
    }
    
    public LiveData<List<TimeBlock>> getTimeBlocksByCategory(TaskCategory category) {
        return timeBlockDao.getTimeBlocksByCategory(category);
    }
    
    public LiveData<TimeBlock> getTimeBlockByHour(int hour) {
        return timeBlockDao.getTimeBlockByHour(hour);
    }
    
    public LiveData<TimeBlock> getTimeBlockById(Long id) {
        return timeBlockDao.getTimeBlockById(id);
    }
    
    public void insertTimeBlock(TimeBlock timeBlock) {
        executorService.execute(() -> timeBlockDao.insertTimeBlock(timeBlock));
    }
    
    public void updateTimeBlock(TimeBlock timeBlock) {
        executorService.execute(() -> timeBlockDao.updateTimeBlock(timeBlock));
    }
    
    public void deleteTimeBlock(TimeBlock timeBlock) {
        executorService.execute(() -> timeBlockDao.deleteTimeBlock(timeBlock));
    }
    
    public void deleteTimeBlockById(Long id) {
        executorService.execute(() -> timeBlockDao.deleteTimeBlockById(id));
    }
    
    public void updateTimeBlockScheduled(Long id, boolean scheduled) {
        executorService.execute(() -> timeBlockDao.updateTimeBlockScheduled(id, scheduled));
    }
}
