package com.simpleagenda.app.ui.planning;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simpleagenda.app.R;
import com.simpleagenda.app.SimpleAgendaApp;
import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.ScheduledTaskWithTask;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.databinding.FragmentPlanningBinding;
import com.simpleagenda.app.ui.task.NewTaskActivity;
import com.simpleagenda.app.ui.tasks.TaskSelectAdapter;
import com.simpleagenda.app.ui.tasks.UnscheduledRowAdapter;
import com.simpleagenda.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlanningFragment extends Fragment {

    private FragmentPlanningBinding binding;
    private AgendaRepository repository;
    private PlanningViewModel planningViewModel;
    private final TaskSelectAdapter selectAdapter = new TaskSelectAdapter();
    private final UnscheduledRowAdapter unscheduledAdapter = new UnscheduledRowAdapter();

    /** Tâches encore disponibles pour le jour courant (hors planning de ce jour). */
    private final List<Task> availableForDayCache = new ArrayList<>();
    private List<ScheduledTaskWithTask> lastScheduled = new ArrayList<>();
    private long dayMillis;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ((SimpleAgendaApp) requireActivity().getApplication()).getRepository();
        planningViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(PlanningViewModel.class);
        dayMillis = TimeUtils.todayStartMillis();
        planningViewModel.setDayMillis(dayMillis);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlanningBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerSelectTasks.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerSelectTasks.setAdapter(selectAdapter);

        binding.recyclerUnscheduled.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUnscheduled.setAdapter(unscheduledAdapter);
        binding.recyclerUnscheduled.setNestedScrollingEnabled(false);

        selectAdapter.setListener(this::updateSummaryFromSelection);

        planningViewModel.tasksAvailableForDay.observe(getViewLifecycleOwner(), tasks -> {
            availableForDayCache.clear();
            if (tasks != null) {
                availableForDayCache.addAll(tasks);
            }
            selectAdapter.submit(tasks);
            unscheduledAdapter.submit(tasks);
            updateSummaryFromSelection();
        });

        planningViewModel.scheduledForDay.observe(getViewLifecycleOwner(), scheduled -> {
            lastScheduled = scheduled != null ? new ArrayList<>(scheduled) : new ArrayList<>();
            binding.dayTimeline.setBlocks(lastScheduled);
            updateFocusAndStats(scheduled);
        });

        binding.dayTimeline.setMoveListener((scheduledId, newStart) ->
                repository.moveScheduled(
                        scheduledId,
                        newStart,
                        () -> { },
                        err -> {
                            if ("overlap".equals(err)) {
                                Toast.makeText(requireContext(), R.string.error_overlap, Toast.LENGTH_SHORT).show();
                            }
                            binding.dayTimeline.setBlocks(lastScheduled);
                        }
                ));

        binding.buttonPlan.setOnClickListener(v -> planSelected());

        binding.buttonPrevDay.setOnClickListener(v -> shiftDay(-1));
        binding.buttonNextDay.setOnClickListener(v -> shiftDay(1));

        binding.fabNewTask.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NewTaskActivity.class)));

        refreshDayTitle();
    }

    private void shiftDay(int deltaDays) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dayMillis);
        c.add(Calendar.DAY_OF_MONTH, deltaDays);
        dayMillis = TimeUtils.startOfDayMillis(c.getTimeInMillis());
        planningViewModel.setDayMillis(dayMillis);
        refreshDayTitle();
    }

    private void refreshDayTitle() {
        Locale fr = Locale.FRENCH;
        String label = TimeUtils.formatDayTitle(dayMillis, fr);
        binding.textDayTitle.setText(label);
        binding.summaryDate.setText(label);
    }

    private void updateSummaryFromSelection() {
        Set<Long> sel = selectAdapter.getSelectedIds();
        int selected = sel.size();
        int poolSize = availableForDayCache.size();
        int selHours = selectAdapter.selectedDurationHours();

        int totalPoolHours = 0;
        for (Task t : availableForDayCache) {
            totalPoolHours += t.getDurationHours();
        }

        int denom = Math.max(poolSize, selected);
        binding.summaryTasksRatio.setText(getString(R.string.tasks_selected_ratio, selected, denom));
        binding.summaryHoursRatio.setText(getString(R.string.hours_volume_ratio,
                getString(R.string.duration_hours_short, selHours),
                getString(R.string.duration_hours_short, totalPoolHours)));
        int progress = totalPoolHours == 0 ? 0 : (int) (100f * selHours / totalPoolHours);
        binding.summaryProgress.setProgressCompat(progress, true);
    }

    private void updateFocusAndStats(@Nullable List<ScheduledTaskWithTask> scheduled) {
        if (scheduled == null || scheduled.isEmpty()) {
            binding.focusTitle.setText("—");
            binding.statScheduledTime.setText("0h");
            binding.statTaskCount.setText(getString(R.string.scheduled_tasks_count, 0));
            return;
        }
        ScheduledTaskWithTask first = scheduled.get(0);
        Task t0 = first.getTask();
        binding.focusTitle.setText(t0 != null ? t0.getTitle() : "—");

        int totalMin = 0;
        for (ScheduledTaskWithTask st : scheduled) {
            Task t = st.getTask();
            if (t != null) {
                totalMin += t.durationMinutes();
            }
        }
        int h = totalMin / 60;
        int m = totalMin % 60;
        String timeLabel = m == 0
                ? getString(R.string.duration_hours_short, h)
                : getString(R.string.duration_hours_minutes, h, m);
        binding.statScheduledTime.setText(timeLabel);
        binding.statTaskCount.setText(getString(R.string.scheduled_tasks_count, scheduled.size()));
    }

    private void planSelected() {
        Set<Long> ids = selectAdapter.getSelectedIds();
        if (ids.isEmpty()) {
            Toast.makeText(requireContext(), R.string.selection_subtitle, Toast.LENGTH_SHORT).show();
            return;
        }
        List<Long> ordered = new ArrayList<>(ids);
        repository.scheduleTasksForDay(
                dayMillis,
                ordered,
                () -> Toast.makeText(requireContext(), R.string.plan_success, Toast.LENGTH_SHORT).show(),
                () -> Toast.makeText(requireContext(), R.string.error_no_slot, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
