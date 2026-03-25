package com.simpleagenda.app.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

import com.simpleagenda.app.data.Converters;
import com.simpleagenda.app.data.dao.TimeBlockDao;
import com.simpleagenda.app.data.model.TimeBlock;

@Database(entities = {TimeBlock.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static AppDatabase INSTANCE;
    
    public abstract TimeBlockDao timeBlockDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "simple_agenda_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return INSTANCE;
    }
    
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
