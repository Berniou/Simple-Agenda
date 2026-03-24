package com.simpleagenda.app.ui.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarViewModel extends ViewModel {
    private final TaskRepository repository;
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private Calendar currentCalendar;
    private LiveData<List<Task>> tasksForMonth;
    
    public CalendarViewModel(TaskRepository repository) {
        this.repository = repository;
        this.currentCalendar = Calendar.getInstance();
        updateTasksForMonth();
    }
    
    public String getCurrentMonthYear() {
        return monthYearFormat.format(currentCalendar.getTime());
    }
    
    public void navigateToPreviousMonth() {
        currentCalendar.add(Calendar.MONTH, -1);
        updateTasksForMonth();
    }
    
    public void navigateToNextMonth() {
        currentCalendar.add(Calendar.MONTH, 1);
        updateTasksForMonth();
    }
    
    public void goToToday() {
        currentCalendar = Calendar.getInstance();
        updateTasksForMonth();
    }
    
    public LiveData<List<Task>> getTasksForMonth() {
        return tasksForMonth;
    }
    
    public LiveData<List<Task>> getTasksForDate(Date date) {
        return repository.getTasksForDate(date);
    }
    
    private void updateTasksForMonth() {
        Calendar startCalendar = (Calendar) currentCalendar.clone();
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        
        Calendar endCalendar = (Calendar) currentCalendar.clone();
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);
        
        tasksForMonth = repository.getTasksForDateRange(
            startCalendar.getTime(), 
            endCalendar.getTime()
        );
    }
    
    public int getCurrentYear() {
        return currentCalendar.get(Calendar.YEAR);
    }
    
    public int getCurrentMonth() {
        return currentCalendar.get(Calendar.MONTH);
    }
    
    public boolean isToday(int day, int month, int year) {
        Calendar today = Calendar.getInstance();
        return day == today.get(Calendar.DAY_OF_MONTH) &&
               month == today.get(Calendar.MONTH) &&
               year == today.get(Calendar.YEAR);
    }
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TaskRepository repository;
        
        public Factory(TaskRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CalendarViewModel.class)) {
                return (T) new CalendarViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
