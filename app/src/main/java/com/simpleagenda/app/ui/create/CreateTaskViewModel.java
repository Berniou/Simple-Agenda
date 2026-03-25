package com.simpleagenda.app.ui.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
        // Créer des blocs horaire à partir du titre
        // Pour l'instant, créer un bloc simple
        TimeBlock timeBlock = new TimeBlock();
        timeBlock.setTitle(taskTitle);
        timeBlock.setDescription(taskDescription);
        timeBlock.setHour(9); // 9h par défaut
        timeBlock.setDuration(getSelectedDuration()); // Utiliser la durée sélectionnée
        timeBlock.setCategory(getSelectedCategory()); // Utiliser la catégorie sélectionnée
        timeBlock.setScheduled(false); // Non planifié par défaut
        
        repository.insertTimeBlock(timeBlock);
    }
    
    private int getSelectedDuration() {
        // TODO: Récupérer la durée depuis le fragment
        return 1; // 1h par défaut
    }
    
    private TaskCategory getSelectedCategory() {
        // TODO: Récupérer la catégorie depuis le fragment
        return TaskCategory.BLUE; // Bleu par défaut
    }
    
    public void clearCurrentTimeBlocks() {
        // Pour l'instant, ne rien faire
        // TODO: Implémenter la logique de nettoyage
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
