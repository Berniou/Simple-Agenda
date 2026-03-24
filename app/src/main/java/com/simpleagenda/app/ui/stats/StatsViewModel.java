package com.simpleagenda.app.ui.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatsViewModel extends ViewModel {
    private final TaskRepository repository;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Task>> completedTasks;
    private LiveData<List<Task>> todayTasks;
    
    public StatsViewModel(TaskRepository repository) {
        this.repository = repository;
        this.allTasks = repository.getAllTasks();
        this.completedTasks = repository.getTasksByCompletionStatus(true);
        this.todayTasks = repository.getTasksForDate(new Date());
    }
    
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    
    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }
    
    public LiveData<List<Task>> getTodayTasks() {
        return todayTasks;
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TaskRepository repository;
        
        public Factory(TaskRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(StatsViewModel.class)) {
                return (T) new StatsViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
