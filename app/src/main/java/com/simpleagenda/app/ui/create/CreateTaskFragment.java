package com.simpleagenda.app.ui.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.TaskCategory;
import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

import java.util.ArrayList;
import java.util.List;

public class CreateTaskFragment extends Fragment {
    private CreateTaskViewModel viewModel;
    private TimeBlockAdapter timeBlockAdapter;
    private RecyclerView recyclerViewTimeBlocks;
    private EditText editTaskTitle;
    private EditText editTaskDescription;
    private RadioGroup radioGroupDuration;
    private ChipGroup chipGroupCategory;
    private Button buttonSave;
    private Button buttonClear;
    private TaskCategory selectedCategory = TaskCategory.BLUE;
    private int selectedDuration = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
        setupClickListeners();
    }

    private void setupViews(View view) {
        recyclerViewTimeBlocks = view.findViewById(R.id.recycler_view_time_blocks);
        editTaskTitle = view.findViewById(R.id.edit_task_title);
        editTaskDescription = view.findViewById(R.id.edit_task_description);
        radioGroupDuration = view.findViewById(R.id.radio_group_duration);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);
        buttonSave = view.findViewById(R.id.button_save);
        buttonClear = view.findViewById(R.id.button_clear);
        
        timeBlockAdapter = new TimeBlockAdapter();
        recyclerViewTimeBlocks.setLayoutManager(new GridLayoutManager(getContext(), 4)); // 4 colonnes pour les heures
        recyclerViewTimeBlocks.setAdapter(timeBlockAdapter);
        
        timeBlockAdapter.setOnTimeBlockClickListener(this::showTimeBlockDetails);
        timeBlockAdapter.setOnTimeBlockLongClickListener(timeBlock -> {
            viewModel.deleteTimeBlock(timeBlock);
        });
        
        // Setup duration selection
        radioGroupDuration.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_1h) selectedDuration = 1;
            else if (checkedId == R.id.radio_2h) selectedDuration = 2;
            else if (checkedId == R.id.radio_3h) selectedDuration = 3;
            else if (checkedId == R.id.radio_4h) selectedDuration = 4;
        });
        
        // Setup category selection
        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                if (checkedId == R.id.chip_blue) selectedCategory = TaskCategory.BLUE;
                else if (checkedId == R.id.chip_green) selectedCategory = TaskCategory.GREEN;
                else if (checkedId == R.id.chip_orange) selectedCategory = TaskCategory.ORANGE;
            }
        });
        
        // Select 1h by default
        radioGroupDuration.check(R.id.radio_1h);
        // Select blue by default
        chipGroupCategory.check(R.id.chip_blue);
    }

    private void setupViewModel() {
        TimeBlockRepository repository = new TimeBlockRepository(
            AppDatabase.getInstance(requireContext()).timeBlockDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new CreateTaskViewModel.Factory(repository)
        ).get(CreateTaskViewModel.class);
        
        viewModel.getAllTimeBlocks().observe(getViewLifecycleOwner(), timeBlocks -> {
            timeBlockAdapter.submitList(timeBlocks);
        });
    }

    private void setupClickListeners() {
        buttonSave.setOnClickListener(v -> {
            saveCurrentTask();
        });
        
        buttonClear.setOnClickListener(v -> {
            clearCurrentTask();
        });
    }

    private void saveCurrentTask() {
        String title = editTaskTitle.getText().toString().trim();
        String description = editTaskDescription.getText().toString().trim();
        
        if (!title.isEmpty()) {
            // Créer une nouvelle tâche à partir des blocs horaire
            viewModel.createTaskFromTimeBlocks(title, description);
            
            // Vider les champs
            clearCurrentTask();
        }
    }

    private void clearCurrentTask() {
        editTaskTitle.setText("");
        editTaskDescription.setText("");
        // Reset selections to defaults
        radioGroupDuration.check(R.id.radio_1h);
        chipGroupCategory.check(R.id.chip_blue);
        selectedDuration = 1;
        selectedCategory = TaskCategory.BLUE;
        viewModel.clearCurrentTimeBlocks();
    }

    private void showTimeBlockDetails(TimeBlock timeBlock) {
        // TODO: Implémenter un dialog pour modifier le bloc horaire
    }
}
