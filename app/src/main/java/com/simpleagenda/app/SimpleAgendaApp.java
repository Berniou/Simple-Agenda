package com.simpleagenda.app;

import android.app.Application;

import androidx.annotation.NonNull;

import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.AppDatabase;

public class SimpleAgendaApp extends Application {

    private AgendaRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase db = AppDatabase.get(this);
        repository = new AgendaRepository(this, db);
    }

    @NonNull
    public AgendaRepository getRepository() {
        return repository;
    }
}
