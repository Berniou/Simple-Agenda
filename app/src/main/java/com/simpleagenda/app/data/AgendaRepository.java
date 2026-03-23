package com.simpleagenda.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.simpleagenda.app.notifications.TaskReminderScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Règles métier : file d’attente, placement sans chevauchement, déplacement sur la grille.
 */
public class AgendaRepository {

    public static final int DAY_START_MINUTES = 6 * 60;
    public static final int DAY_END_MINUTES = 22 * 60;
    public static final int DEFAULT_FIRST_SLOT_MINUTES = 8 * 60;
    public static final int SNAP_STEP_MINUTES = 15;

    private final TaskDao taskDao;
    private final ScheduledTaskDao scheduledDao;
    //TODO
    // private final TaskReminderScheduler reminderScheduler;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public AgendaRepository(@NonNull Context appContext, @NonNull AppDatabase db) {
        Context ctx = appContext.getApplicationContext();
        this.taskDao = db.taskDao();
        this.scheduledDao = db.scheduledTaskDao();
        //  TODO: injecter un vrai scheduler qui utilise AlarmManager ou WorkManager, et pas un fake qui ne fait rien
        // this.reminderScheduler = new TaskReminderScheduler(ctx);
    }

    /** Bibliothèque : toutes les tâches créées. */
    public LiveData<List<Task>> observeAllTasks() {
        return taskDao.observeAllTasks();
    }

    /** Tâches qu’on peut encore ajouter au jour choisi (pas encore planifiées ce jour-là). */
    public LiveData<List<Task>> observeTasksAvailableForDay(long dayMillis) {
        return taskDao.observeAvailableForDay(dayMillis);
    }

    public LiveData<List<ScheduledTaskWithTask>> observeScheduledForDay(long dayMillis) {
        return scheduledDao.observeForDay(dayMillis);
    }

    public void insertTask(@NonNull Task task, @NonNull Runnable onDone) {
        io.execute(() -> {
            taskDao.insert(task);
            main.post(onDone);
        });
    }

    public void scheduleTasksForDay(long dayMillis, @NonNull List<Long> taskIdsInOrder,
                                    @NonNull Runnable onDone,
                                    @Nullable Runnable onNoRoom) {
        io.execute(() -> {
            List<ScheduledTaskWithTask> existing = scheduledDao.getForDaySync(dayMillis);
            List<Interval> busy = new ArrayList<>();
            for (ScheduledTaskWithTask st : existing) {
                Task t = st.getTask();
                if (t == null) {
                    continue;
                }
                int s = st.getScheduled().getStartMinutesFromMidnight();
                busy.add(new Interval(s, s + t.durationMinutes()));
            }
            Collections.sort(busy, Comparator.comparingInt(a -> a.start));

            boolean anyPlaced = false;
            for (Long taskId : taskIdsInOrder) {
                Task task = taskDao.getById(taskId);
                if (task == null) {
                    continue;
                }
                if (scheduledDao.countForTaskAndDay(taskId, dayMillis) > 0) {
                    continue;
                }
                int duration = task.durationMinutes();
                int slot = findNextSlot(duration, busy);
                if (slot < 0) {
                    continue;
                }
                ScheduledTask row = new ScheduledTask();
                row.setTaskId(taskId);
                row.setDayMillis(dayMillis);
                row.setStartMinutesFromMidnight(slot);
                scheduledDao.insert(row);
                busy.add(new Interval(slot, slot + duration));
                Collections.sort(busy, Comparator.comparingInt(a -> a.start));
                anyPlaced = true;
            }
            //TODO
            //reminderScheduler.scheduleAllForDay(dayMillis);
            final boolean placed = anyPlaced;
            final boolean hadIds = !taskIdsInOrder.isEmpty();
            main.post(() -> {
                if (!placed && onNoRoom != null && hadIds) {
                    onNoRoom.run();
                } else {
                    onDone.run();
                }
            });
        });
    }

    /**
     * Place une tâche à l’heure choisie sur la journée (trous entre les blocs autorisés).
     */
    public void scheduleTaskAt(long taskId, long dayMillis, int rawStartMinutes,
                               @NonNull Runnable onSuccess,
                               @NonNull java.util.function.Consumer<String> onError) {
        io.execute(() -> {
            if (scheduledDao.countForTaskAndDay(taskId, dayMillis) > 0) {
                main.post(() -> onError.accept("already"));
                return;
            }
            Task task = taskDao.getById(taskId);
            if (task == null) {
                main.post(() -> onError.accept("missing"));
                return;
            }
            int duration = task.durationMinutes();
            int snapped = (rawStartMinutes / SNAP_STEP_MINUTES) * SNAP_STEP_MINUTES;
            snapped = Math.max(DAY_START_MINUTES, snapped);
            int maxStart = DAY_END_MINUTES - duration;
            if (maxStart < DAY_START_MINUTES) {
                main.post(() -> onError.accept("bounds"));
                return;
            }
            maxStart = (maxStart / SNAP_STEP_MINUTES) * SNAP_STEP_MINUTES;
            if (snapped > maxStart) {
                snapped = maxStart;
            }
            List<ScheduledTaskWithTask> day = scheduledDao.getForDaySync(dayMillis);
            if (!canPlace(-1L, snapped, duration, day)) {
                main.post(() -> onError.accept("overlap"));
                return;
            }
            ScheduledTask row = new ScheduledTask();
            row.setTaskId(taskId);
            row.setDayMillis(dayMillis);
            row.setStartMinutesFromMidnight(snapped);
            scheduledDao.insert(row);
            reminderScheduler.scheduleAllForDay(dayMillis);
            main.post(onSuccess);
        });
    }

    public void moveScheduled(long scheduledId, int rawStartMinutes,
                              @NonNull Runnable onSuccess,
                              @NonNull java.util.function.Consumer<String> onError) {
        io.execute(() -> {
            ScheduledTask st = scheduledDao.getById(scheduledId);
            if (st == null) {
                main.post(() -> onError.accept("Introuvable"));
                return;
            }
            Task task = taskDao.getById(st.getTaskId());
            if (task == null) {
                main.post(() -> onError.accept("Introuvable"));
                return;
            }
            int snapped = snapToGrid(rawStartMinutes);
            int duration = task.durationMinutes();
            List<ScheduledTaskWithTask> day = scheduledDao.getForDaySync(st.getDayMillis());
            if (!canPlace(st.getId(), snapped, duration, day)) {
                main.post(() -> onError.accept("overlap"));
                return;
            }
            st.setStartMinutesFromMidnight(snapped);
            scheduledDao.update(st);
            //TODO
            // reminderScheduler.reschedule(st.getId());
            main.post(onSuccess);
        });
    }

    public void clearScheduledForDay(long dayMillis, @NonNull Runnable onDone) {
        io.execute(() -> {
            scheduledDao.deleteForDay(dayMillis);
            main.post(onDone);
        });
    }

    /**
     * Moves a scheduled task and reorders other conflicting tasks automatically.
     * When a task is moved to a new position, any overlapping tasks are shifted
     * to make room, creating a cascading reorder effect.
     */
    public void moveScheduledWithReorder(long scheduledId, int rawStartMinutes,
                                        @NonNull Runnable onSuccess,
                                        @NonNull java.util.function.Consumer<String> onError) {
        io.execute(() -> {
            ScheduledTask st = scheduledDao.getById(scheduledId);
            if (st == null) {
                main.post(() -> onError.accept("Introuvable"));
                return;
            }
            Task task = taskDao.getById(st.getTaskId());
            if (task == null) {
                main.post(() -> onError.accept("Introuvable"));
                return;
            }
            int snapped = snapToGrid(rawStartMinutes);
            int duration = task.durationMinutes();
            long dayMillis = st.getDayMillis();
            List<ScheduledTaskWithTask> day = scheduledDao.getForDaySync(dayMillis);

            // Check if new position is within bounds
            if (snapped < DAY_START_MINUTES || snapped + duration > DAY_END_MINUTES) {
                main.post(() -> onError.accept("bounds"));
                return;
            }

            // Find overlapping tasks and reorder them
            List<ScheduledTaskWithTask> toMove = new ArrayList<>();
            for (ScheduledTaskWithTask stWithTask : day) {
                if (stWithTask.getScheduled().getId() == scheduledId) {
                    continue;
                }
                Task t = stWithTask.getTask();
                if (t == null) {
                    continue;
                }
                int existingStart = stWithTask.getScheduled().getStartMinutesFromMidnight();
                int existingEnd = existingStart + t.durationMinutes();
                int newEnd = snapped + duration;

                // Check if there's an overlap
                if (snapped < existingEnd && existingStart < newEnd) {
                    toMove.add(stWithTask);
                }
            }

            // Sort tasks by start time to determine direction
            Collections.sort(toMove, (a, b) ->
                    Integer.compare(a.getScheduled().getStartMinutesFromMidnight(),
                                   b.getScheduled().getStartMinutesFromMidnight()));

            // Move the main task
            st.setStartMinutesFromMidnight(snapped);
            scheduledDao.update(st);

            // Reorder affected tasks
            int newEnd = snapped + duration;
            for (ScheduledTaskWithTask toReorder : toMove) {
                ScheduledTask stToMove = toReorder.getScheduled();
                Task tToMove = toReorder.getTask();
                if (tToMove == null) {
                    continue;
                }
                int currentStart = stToMove.getStartMinutesFromMidnight();
                int durationToMove = tToMove.durationMinutes();

                // Move task to right after the moved task
                int newStart = newEnd;
                if (newStart + durationToMove <= DAY_END_MINUTES) {
                    stToMove.setStartMinutesFromMidnight(newStart);
                    scheduledDao.update(stToMove);
                    newEnd = newStart + durationToMove;
                }
            }

            reminderScheduler.scheduleAllForDay(dayMillis);
            main.post(onSuccess);
        });
    }

    /**
     * @param excludeScheduledId id de {@link ScheduledTask} à ignorer (bloc déplacé)
     */
    public boolean canPlace(long excludeScheduledId, int start, int durationMinutes,
                            @NonNull List<ScheduledTaskWithTask> sameDay) {
        int end = start + durationMinutes;
        if (start < DAY_START_MINUTES || end > DAY_END_MINUTES) {
            return false;
        }
        for (ScheduledTaskWithTask st : sameDay) {
            if (excludeScheduledId >= 0 && st.getScheduled().getId() == excludeScheduledId) {
                continue;
            }
            Task t = st.getTask();
            if (t == null) {
                continue;
            }
            int os = st.getScheduled().getStartMinutesFromMidnight();
            int oe = os + t.durationMinutes();
            if (start < oe && os < end) {
                return false;
            }
        }
        return true;
    }

    public static int snapToGrid(int minutes) {
        int snapped = (minutes / SNAP_STEP_MINUTES) * SNAP_STEP_MINUTES;
        return Math.max(DAY_START_MINUTES, Math.min(snapped, DAY_END_MINUTES - SNAP_STEP_MINUTES));
    }

    private static int findNextSlot(int durationMinutes, @NonNull List<Interval> busy) {
        if (busy.isEmpty()) {
            if (DEFAULT_FIRST_SLOT_MINUTES + durationMinutes <= DAY_END_MINUTES) {
                return DEFAULT_FIRST_SLOT_MINUTES;
            }
            return -1;
        }
        int cursor = DEFAULT_FIRST_SLOT_MINUTES;
        for (Interval i : busy) {
            if (cursor + durationMinutes <= i.start) {
                return Math.max(cursor, DAY_START_MINUTES);
            }
            cursor = Math.max(cursor, i.end);
        }
        if (cursor + durationMinutes <= DAY_END_MINUTES) {
            return cursor;
        }
        return -1;
    }

    private static final class Interval {
        final int start;
        final int end;

        Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
