package com.simpleagenda.app.ui.addtask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.model.TaskCategory;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.util.Calendar;
import java.util.Date;

public class AddTaskViewModel extends ViewModel {
    private final TaskRepository repository;
    private Task currentTask;
    private boolean isEditMode = false;
    
    public AddTaskViewModel(TaskRepository repository) {
        this.repository = repository;
        this.currentTask = new Task();
        // Set default time to current hour
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        currentTask.setStartTime(calendar.getTime());
        
        calendar.add(Calendar.HOUR, 1);
        currentTask.setEndTime(calendar.getTime());
    }
    
    public void loadTask(Long taskId) {
        if (taskId != null) {
            repository.getTaskById(taskId).observeForever(task -> {
                if (task != null) {
                    currentTask = task;
                    isEditMode = true;
                }
            });
        }
    }
    
    public LiveData<Task> getTask(Long taskId) {
        return repository.getTaskById(taskId);
    }
    
    public void setTitle(String title) {
        currentTask.setTitle(title);
    }
    
    public void setDescription(String description) {
        currentTask.setDescription(description);
    }
    
    public void setStartTime(Date startTime) {
        currentTask.setStartTime(startTime);
    }
    
    public void setEndTime(Date endTime) {
        currentTask.setEndTime(endTime);
    }
    
    public void setCategory(TaskCategory category) {
        currentTask.setCategory(category);
    }
    
    public void saveTask() {
        if (isEditMode) {
            repository.updateTask(currentTask);
        } else {
            repository.insertTask(currentTask);
        }
    }
    
    public void deleteTask() {
        if (isEditMode && currentTask.getId() != null) {
            repository.deleteTask(currentTask);
        }
    }
    
    public Task getCurrentTask() {
        return currentTask;
    }
    
    public boolean isEditMode() {
        return isEditMode;
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TaskRepository repository;
        
        public Factory(TaskRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AddTaskViewModel.class)) {
                return (T) new AddTaskViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
