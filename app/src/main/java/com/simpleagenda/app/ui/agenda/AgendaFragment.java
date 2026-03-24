package com.simpleagenda.app.ui.agenda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.util.List;

public class AgendaFragment extends Fragment {
    private AgendaViewModel viewModel;
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView(view);
        setupFab(view);
        setupViewModel();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        taskAdapter = new TaskAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);
        
        taskAdapter.setOnTaskClickListener(task -> {
            // Navigate to task detail
            // TODO: Implement navigation to TaskDetailFragment
        });
        
        taskAdapter.setOnTaskCheckedChangeListener((task, isChecked) -> {
            viewModel.toggleTaskCompletion(task);
        });
    }

    private void setupFab(View view) {
        fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> {
            // Navigate to add task
            // TODO: Implement navigation to AddTaskFragment
        });
    }

    private void setupViewModel() {
        TaskRepository repository = new TaskRepository(
            AppDatabase.getInstance(requireContext()).taskDao()
        );
        
        viewModel = new ViewModelProvider(
            this, 
            new AgendaViewModel.Factory(repository)
        ).get(AgendaViewModel.class);
        
        viewModel.getTasksForToday().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
        });
    }
}
