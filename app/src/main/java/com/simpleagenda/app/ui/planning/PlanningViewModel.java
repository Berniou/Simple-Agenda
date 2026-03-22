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

import java.util.List;

public class PlanningViewModel extends AndroidViewModel {

    private final MutableLiveData<Long> dayMillis = new MutableLiveData<>();
    public final LiveData<List<ScheduledTaskWithTask>> scheduledForDay;

    public PlanningViewModel(@NonNull Application application) {
        super(application);
        AgendaRepository repo = ((SimpleAgendaApp) application).getRepository();
        scheduledForDay = Transformations.switchMap(dayMillis, repo::observeScheduledForDay);
    }

    public void setDayMillis(long millis) {
        dayMillis.setValue(millis);
    }
}
