package com.simpleagenda.app.data.repository;

import androidx.lifecycle.LiveData;

import com.simpleagenda.app.data.dao.TaskDao;
import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.model.TaskCategory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    
    public TaskRepository(TaskDao taskDao) {
        this.taskDao = taskDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }
    
    public LiveData<List<Task>> getTasksForDate(Date date) {
        return taskDao.getTasksForDate(date);
    }
    
    public LiveData<List<Task>> getTasksForDateRange(Date startDate, Date endDate) {
        return taskDao.getTasksForDateRange(startDate, endDate);
    }
    
    public LiveData<List<Task>> getTasksByCategory(TaskCategory category) {
        return taskDao.getTasksByCategory(category);
    }
    
    public LiveData<List<Task>> getTasksByCompletionStatus(boolean completed) {
        return taskDao.getTasksByCompletionStatus(completed);
    }
    
    public LiveData<Task> getTaskById(Long id) {
        return taskDao.getTaskById(id);
    }
    
    public void insertTask(Task task) {
        executorService.execute(() -> taskDao.insertTask(task));
    }
    
    public void updateTask(Task task) {
        executorService.execute(() -> taskDao.updateTask(task));
    }
    
    public void deleteTask(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }
    
    public void deleteTaskById(Long id) {
        executorService.execute(() -> taskDao.deleteTaskById(id));
    }
    
    public void updateTaskCompletion(Long id, boolean completed) {
        executorService.execute(() -> taskDao.updateTaskCompletion(id, completed));
    }
}
