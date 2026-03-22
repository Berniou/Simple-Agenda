package com.simpleagenda.app.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {Task.class, ScheduledTask.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    /**
     * Même tâche replanifiable sur plusieurs jours : index unique (taskId, dayMillis)
     * à la place de taskId seul.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP INDEX IF EXISTS `index_scheduled_tasks_taskId`");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_scheduled_tasks_taskId_dayMillis` "
                    + "ON `scheduled_tasks` (`taskId`, `dayMillis`)");
        }
    };

    public abstract TaskDao taskDao();

    public abstract ScheduledTaskDao scheduledTaskDao();

    public static AppDatabase get(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "simple_agenda.db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
