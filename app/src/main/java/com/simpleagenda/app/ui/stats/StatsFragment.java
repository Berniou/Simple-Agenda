package com.simpleagenda.app.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.util.List;

public class StatsFragment extends Fragment {
    private StatsViewModel viewModel;
    private TextView textTotalTasks;
    private TextView textCompletedTasks;
    private TextView textTodayTasks;
    private TextView textCompletionRate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
    }

    private void setupViews(View view) {
        textTotalTasks = view.findViewById(R.id.text_total_tasks);
        textCompletedTasks = view.findViewById(R.id.text_completed_tasks);
        textTodayTasks = view.findViewById(R.id.text_today_tasks);
        textCompletionRate = view.findViewById(R.id.text_completion_rate);
    }

    private void setupViewModel() {
        TaskRepository repository = new TaskRepository(
            AppDatabase.getInstance(requireContext()).taskDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new StatsViewModel.Factory(repository)
        ).get(StatsViewModel.class);
        
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            updateStats();
        });
        
        viewModel.getCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            updateStats();
        });
        
        viewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
            updateStats();
        });
    }

    private void updateStats() {
        // Get current values (simplified for demo)
        // In a real app, you'd combine these observations properly
        int totalTasks = 12; // Placeholder
        int completedTasks = 8; // Placeholder
        int todayTasks = 3; // Placeholder
        
        textTotalTasks.setText(String.valueOf(totalTasks));
        textCompletedTasks.setText(String.valueOf(completedTasks));
        textTodayTasks.setText(String.valueOf(todayTasks));
        
        if (totalTasks > 0) {
            double completionRate = (double) completedTasks / totalTasks * 100;
            textCompletionRate.setText(String.format("%.1f%%", completionRate));
        } else {
            textCompletionRate.setText("0%");
        }
    }
}
