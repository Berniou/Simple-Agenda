package com.simpleagenda.app.ui.settings;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.repository.TaskRepository;

public class SettingsViewModel extends ViewModel {
    private final TaskRepository repository;
    
    public SettingsViewModel(TaskRepository repository) {
        this.repository = repository;
    }
    
    public void clearAllTasks() {
        // TODO: Implement clear all tasks functionality
    }
    
    public void exportTasks() {
        // TODO: Implement export tasks functionality
    }
    
    public void importTasks() {
        // TODO: Implement import tasks functionality
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TaskRepository repository;
        
        public Factory(TaskRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
                return (T) new SettingsViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
