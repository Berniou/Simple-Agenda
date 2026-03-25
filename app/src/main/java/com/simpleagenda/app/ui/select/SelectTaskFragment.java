package com.simpleagenda.app.ui.select;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

import java.util.List;

public class SelectTaskFragment extends Fragment {
    private SelectTaskViewModel viewModel;
    private TaskSelectionAdapter taskSelectionAdapter;
    private RecyclerView recyclerViewTaskSelection;
    private TextView textSelectedCount;
    private Button buttonAddToDay;
    private Button buttonClearSelection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
        setupClickListeners();
    }

    private void setupViews(View view) {
        recyclerViewTaskSelection = view.findViewById(R.id.recycler_view_task_selection);
        textSelectedCount = view.findViewById(R.id.text_selected_count);
        buttonAddToDay = view.findViewById(R.id.button_add_to_day);
        buttonClearSelection = view.findViewById(R.id.button_clear_selection);
        
        taskSelectionAdapter = new TaskSelectionAdapter();
        recyclerViewTaskSelection.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTaskSelection.setAdapter(taskSelectionAdapter);
        
        taskSelectionAdapter.setOnTaskSelectionListener(this::onTaskSelectionChanged);
        taskSelectionAdapter.setOnTaskClickListener(this::onTaskClicked);
    }

    private void setupViewModel() {
        TimeBlockRepository repository = new TimeBlockRepository(
            AppDatabase.getInstance(requireContext()).timeBlockDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new SelectTaskViewModel.Factory(repository)
        ).get(SelectTaskViewModel.class);
        
        viewModel.getUnscheduledTimeBlocks().observe(getViewLifecycleOwner(), timeBlocks -> {
            taskSelectionAdapter.submitList(timeBlocks);
            updateSelectedCount();
        });
    }

    private void setupClickListeners() {
        buttonAddToDay.setOnClickListener(v -> {
            viewModel.addSelectedTasksToDay();
            // Naviguer vers la page de planification
            // TODO: Implémenter navigation
        });
        
        buttonClearSelection.setOnClickListener(v -> {
            viewModel.clearSelection();
        });
    }

    private void onTaskSelectionChanged(int selectedCount) {
        updateSelectedCount();
        
        // Activer/désactiver le bouton d'ajout
        buttonAddToDay.setEnabled(selectedCount > 0);
    }

    private void onTaskClicked(TimeBlock timeBlock) {
        // Toggle la sélection
        viewModel.toggleTaskSelection(timeBlock);
    }

    private void updateSelectedCount() {
        List<TimeBlock> selectedTasks = viewModel.getSelectedTasks();
        int count = selectedTasks != null ? selectedTasks.size() : 0;
        
        if (count == 0) {
            textSelectedCount.setText("Aucune tâche sélectionnée");
        } else if (count == 1) {
            textSelectedCount.setText("1 tâche sélectionnée");
        } else {
            textSelectedCount.setText(count + " tâches sélectionnées");
        }
    }
}
