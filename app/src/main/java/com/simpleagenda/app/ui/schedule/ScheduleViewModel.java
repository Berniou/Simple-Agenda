package com.simpleagenda.app.ui.schedule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

import java.util.List;

public class ScheduleViewModel extends ViewModel {
    private final TimeBlockRepository repository;
    private LiveData<List<TimeBlock>> scheduledTimeBlocks;
    
    public ScheduleViewModel(TimeBlockRepository repository) {
        this.repository = repository;
        this.scheduledTimeBlocks = repository.getScheduledTimeBlocks();
    }
    
    public LiveData<List<TimeBlock>> getScheduledTimeBlocks() {
        return scheduledTimeBlocks;
    }
    
    public void updateTimeBlock(TimeBlock timeBlock) {
        repository.updateTimeBlock(timeBlock);
    }
    
    public void deleteTimeBlock(TimeBlock timeBlock) {
        repository.deleteTimeBlock(timeBlock);
    }
    
    public void moveTimeBlock(TimeBlock timeBlock, int newHour) {
        timeBlock.setHour(newHour);
        repository.updateTimeBlock(timeBlock);
    }
    
    public void addTimeBlockToSchedule(TimeBlock timeBlock) {
        timeBlock.setScheduled(true);
        repository.updateTimeBlock(timeBlock);
    }
    
    public void removeTimeBlockFromSchedule(TimeBlock timeBlock) {
        timeBlock.setScheduled(false);
        repository.updateTimeBlock(timeBlock);
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TimeBlockRepository repository;
        
        public Factory(TimeBlockRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ScheduleViewModel.class)) {
                return (T) new ScheduleViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
