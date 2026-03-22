package com.simpleagenda.app.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simpleagenda.app.R;
import com.simpleagenda.app.SimpleAgendaApp;
import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.databinding.FragmentPendingTasksBinding;
import com.simpleagenda.app.ui.task.NewTaskActivity;

import java.util.List;

public class PendingTasksFragment extends Fragment {

    private FragmentPendingTasksBinding binding;
    private AgendaRepository repository;
    private final PendingTasksAdapter adapter = new PendingTasksAdapter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ((SimpleAgendaApp) requireActivity().getApplication()).getRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPendingTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTasks.setAdapter(adapter);
        binding.recyclerTasks.setNestedScrollingEnabled(false);

        repository.observeBacklog().observe(getViewLifecycleOwner(), this::onBacklog);

        binding.fabAdd.setOnClickListener(v -> openNewTask());
        binding.cardAddPlaceholder.setOnClickListener(v -> openNewTask());
    }

    private void onBacklog(List<Task> tasks) {
        adapter.submit(tasks);
        int count = tasks == null ? 0 : tasks.size();
        binding.statTasksCount.setText(getString(R.string.free_tasks_count, count));
        int totalHours = 0;
        if (tasks != null) {
            for (Task t : tasks) {
                totalHours += t.getDurationHours();
            }
        }
        binding.statTotalDuration.setText(getString(R.string.duration_hours_short, totalHours));
        binding.statPriority.setText("—");
    }

    private void openNewTask() {
        startActivity(new Intent(requireContext(), NewTaskActivity.class));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
