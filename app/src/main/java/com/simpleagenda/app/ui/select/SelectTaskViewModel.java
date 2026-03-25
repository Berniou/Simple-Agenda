package com.simpleagenda.app.ui.select;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

import java.util.ArrayList;
import java.util.List;

public class SelectTaskViewModel extends ViewModel {
    private final TimeBlockRepository repository;
    private LiveData<List<TimeBlock>> unscheduledTimeBlocks;
    private List<TimeBlock> selectedTasks;
    
    public SelectTaskViewModel(TimeBlockRepository repository) {
        this.repository = repository;
        this.unscheduledTimeBlocks = repository.getUnscheduledTimeBlocks();
        this.selectedTasks = new ArrayList<>();
    }
    
    public LiveData<List<TimeBlock>> getUnscheduledTimeBlocks() {
        return unscheduledTimeBlocks;
    }
    
    public List<TimeBlock> getSelectedTasks() {
        return selectedTasks;
    }
    
    public void toggleTaskSelection(TimeBlock timeBlock) {
        if (selectedTasks.contains(timeBlock)) {
            selectedTasks.remove(timeBlock);
        } else {
            selectedTasks.add(timeBlock);
        }
    }
    
    public void clearSelection() {
        selectedTasks.clear();
    }
    
    public void addSelectedTasksToDay() {
        // Ajouter les tâches sélectionnées à la journée du jour
        for (TimeBlock timeBlock : selectedTasks) {
            timeBlock.setScheduled(true);
            repository.updateTimeBlock(timeBlock);
        }
        
        // Vider la sélection après ajout
        clearSelection();
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TimeBlockRepository repository;
        
        public Factory(TimeBlockRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SelectTaskViewModel.class)) {
                return (T) new SelectTaskViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
