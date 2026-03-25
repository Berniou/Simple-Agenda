package com.simpleagenda.app.ui.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.model.TaskCategory;
import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

import java.util.List;

public class CreateTaskViewModel extends ViewModel {
    private final TimeBlockRepository repository;
    private LiveData<List<TimeBlock>> allTimeBlocks;
    
    public CreateTaskViewModel(TimeBlockRepository repository) {
        this.repository = repository;
        this.allTimeBlocks = repository.getAllTimeBlocks();
    }
    
    public LiveData<List<TimeBlock>> getAllTimeBlocks() {
        return allTimeBlocks;
    }
    
    public void insertTimeBlock(TimeBlock timeBlock) {
        repository.insertTimeBlock(timeBlock);
    }
    
    public void updateTimeBlock(TimeBlock timeBlock) {
        repository.updateTimeBlock(timeBlock);
    }
    
    public void deleteTimeBlock(TimeBlock timeBlock) {
        repository.deleteTimeBlock(timeBlock);
    }
    
    public void createTimeBlock(int hour, String title) {
        TimeBlock timeBlock = new TimeBlock();
        timeBlock.setHour(hour);
        timeBlock.setTitle(title);
        timeBlock.setDuration(1); // 1 heure par défaut
        timeBlock.setCategory(TaskCategory.BLUE);
        repository.insertTimeBlock(timeBlock);
    }
    
    public void createTaskFromTimeBlocks(String taskTitle, String taskDescription) {
        // Créer une tâche à partir des blocs horaire
        Task task = new Task();
        task.setTitle(taskTitle);
        task.setDescription(taskDescription);
        task.setCategory(TaskCategory.BLUE);
        
        // TODO: Convertir les blocs horaire en tâche planifiée
        // Pour l'instant, marquer tous les blocs comme planifiés
        List<TimeBlock> timeBlocks = allTimeBlocks.getValue();
        if (timeBlocks != null) {
            for (TimeBlock block : timeBlocks) {
                if (block.getTitle() != null && !block.getTitle().isEmpty()) {
                    block.setScheduled(true);
                    repository.updateTimeBlock(block);
                }
            }
        }
    }
    
    public void clearCurrentTimeBlocks() {
        List<TimeBlock> timeBlocks = allTimeBlocks.getValue();
        if (timeBlocks != null) {
            for (TimeBlock block : timeBlocks) {
                if (block.isScheduled()) {
                    block.setScheduled(false);
                    repository.updateTimeBlock(block);
                }
            }
        }
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TimeBlockRepository repository;
        
        public Factory(TimeBlockRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CreateTaskViewModel.class)) {
                return (T) new CreateTaskViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
