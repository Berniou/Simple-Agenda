package com.simpleagenda.app.ui.unplanned;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

public class UnplannedViewModel extends ViewModel {
    private final TimeBlockRepository repository;
    private LiveData<List<TimeBlock>> unscheduledTimeBlocks;
    
    public UnplannedViewModel(TimeBlockRepository repository) {
        this.repository = repository;
        this.unscheduledTimeBlocks = repository.getUnscheduledTimeBlocks();
    }
    
    public LiveData<List<TimeBlock>> getUnscheduledTimeBlocks() {
        return unscheduledTimeBlocks;
    }
    
    public void deleteTimeBlock(TimeBlock timeBlock) {
        repository.deleteTimeBlock(timeBlock);
    }
    
    public void scheduleTimeBlock(TimeBlock timeBlock) {
        timeBlock.setScheduled(true);
        repository.updateTimeBlock(timeBlock);
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TimeBlockRepository repository;
        
        public Factory(TimeBlockRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(UnplannedViewModel.class)) {
                return (T) new UnplannedViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
