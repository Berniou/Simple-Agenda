package com.simpleagenda.app.ui.agenda;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.util.Date;
import java.util.List;

public class AgendaViewModel extends ViewModel {
    private final TaskRepository repository;
    private final LiveData<List<Task>> tasksForToday;
    
    public AgendaViewModel(TaskRepository repository) {
        this.repository = repository;
        this.tasksForToday = repository.getTasksForDate(new Date());
    }
    
    public LiveData<List<Task>> getTasksForToday() {
        return tasksForToday;
    }
    
    public void insertTask(Task task) {
        repository.insertTask(task);
    }
    
    public void updateTask(Task task) {
        repository.updateTask(task);
    }
    
    public void deleteTask(Task task) {
        repository.deleteTask(task);
    }
    
    public void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted());
        repository.updateTask(task);
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TaskRepository repository;
        
        public Factory(TaskRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AgendaViewModel.class)) {
                return (T) new AgendaViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
