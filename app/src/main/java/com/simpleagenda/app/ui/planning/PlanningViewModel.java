package com.simpleagenda.app.ui.planning;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.simpleagenda.app.SimpleAgendaApp;
import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.ScheduledTaskWithTask;
import com.simpleagenda.app.data.Task;

import java.util.List;

public class PlanningViewModel extends AndroidViewModel {

    private final MutableLiveData<Long> dayMillis = new MutableLiveData<>();
    public final LiveData<List<ScheduledTaskWithTask>> scheduledForDay;
    /** Tâches encore ajoutables au jour affiché (non planifiées ce jour-là). */
    public final LiveData<List<Task>> tasksAvailableForDay;

    public PlanningViewModel(@NonNull Application application) {
        super(application);
        AgendaRepository repo = ((SimpleAgendaApp) application).getRepository();
        scheduledForDay = Transformations.switchMap(dayMillis, repo::observeScheduledForDay);
        tasksAvailableForDay = Transformations.switchMap(dayMillis, repo::observeTasksAvailableForDay);
    }

    public void setDayMillis(long millis) {
        dayMillis.setValue(millis);
    }
}
