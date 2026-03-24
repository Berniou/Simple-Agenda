package com.simpleagenda.app.ui.addtask;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.TaskCategory;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskFragment extends Fragment {
    private AddTaskViewModel viewModel;
    private EditText editTitle;
    private EditText editDescription;
    private TextView textStartTime;
    private TextView textEndTime;
    private ChipGroup chipGroupCategory;
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonDelete;
    
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
        setupClickListeners();
        
        // Check if we're editing an existing task
        Long taskId = null;
        if (getArguments() != null) {
            taskId = getArguments().getLong("taskId", -1);
            if (taskId != -1) {
                viewModel.loadTask(taskId);
            }
        }
    }

    private void setupViews(View view) {
        editTitle = view.findViewById(R.id.edit_title);
        editDescription = view.findViewById(R.id.edit_description);
        textStartTime = view.findViewById(R.id.text_start_time);
        textEndTime = view.findViewById(R.id.text_end_time);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);
        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonDelete = view.findViewById(R.id.button_delete);
        
        // Initialize time displays
        textStartTime.setText(timeFormat.format(startCalendar.getTime()));
        textEndTime.setText(timeFormat.format(endCalendar.getTime()));
    }

    private void setupViewModel() {
        TaskRepository repository = new TaskRepository(
            AppDatabase.getInstance(requireContext()).taskDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new AddTaskViewModel.Factory(repository)
        ).get(AddTaskViewModel.class);
        
        // Observe task if in edit mode
        Long taskId = getArguments() != null ? getArguments().getLong("taskId", -1) : null;
        if (taskId != null && taskId != -1) {
            viewModel.getTask(taskId).observe(getViewLifecycleOwner(), task -> {
                if (task != null) {
                    editTitle.setText(task.getTitle());
                    editDescription.setText(task.getDescription());
                    
                    if (task.getStartTime() != null) {
                        startCalendar.setTime(task.getStartTime());
                        textStartTime.setText(timeFormat.format(task.getStartTime()));
                    }
                    
                    if (task.getEndTime() != null) {
                        endCalendar.setTime(task.getEndTime());
                        textEndTime.setText(timeFormat.format(task.getEndTime()));
                    }
                    
                    // Select category chip
                    selectCategoryChip(task.getCategory());
                    
                    // Show delete button in edit mode
                    buttonDelete.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setupClickListeners() {
        textStartTime.setOnClickListener(v -> showTimePicker(true));
        textEndTime.setOnClickListener(v -> showTimePicker(false));
        
        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            TaskCategory category = getCategoryFromChip(checkedId);
            if (category != null) {
                viewModel.setCategory(category);
            }
        });
        
        buttonSave.setOnClickListener(v -> saveTask());
        buttonCancel.setOnClickListener(v -> navigateBack());
        buttonDelete.setOnClickListener(v -> deleteTask());
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = isStartTime ? startCalendar : endCalendar;
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                
                if (isStartTime) {
                    textStartTime.setText(timeFormat.format(calendar.getTime()));
                    viewModel.setStartTime(calendar.getTime());
                } else {
                    textEndTime.setText(timeFormat.format(calendar.getTime()));
                    viewModel.setEndTime(calendar.getTime());
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        
        timePickerDialog.show();
    }

    private void saveTask() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        
        if (title.isEmpty()) {
            editTitle.setError("Title is required");
            return;
        }
        
        viewModel.setTitle(title);
        viewModel.setDescription(description);
        viewModel.setStartTime(startCalendar.getTime());
        viewModel.setEndTime(endCalendar.getTime());
        
        // Set category if none selected
        if (chipGroupCategory.getCheckedChipId() == View.NO_ID) {
            viewModel.setCategory(TaskCategory.BLUE);
        }
        
        viewModel.saveTask();
        navigateBack();
    }

    private void deleteTask() {
        viewModel.deleteTask();
        navigateBack();
    }

    private void navigateBack() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }

    private TaskCategory getCategoryFromChip(int chipId) {
        if (chipId == R.id.chip_blue) return TaskCategory.BLUE;
        if (chipId == R.id.chip_green) return TaskCategory.GREEN;
        if (chipId == R.id.chip_orange) return TaskCategory.ORANGE;
        return null;
    }

    private void selectCategoryChip(TaskCategory category) {
        int chipId = 0;
        switch (category) {
            case BLUE:
                chipId = R.id.chip_blue;
                break;
            case GREEN:
                chipId = R.id.chip_green;
                break;
            case ORANGE:
                chipId = R.id.chip_orange;
                break;
        }
        if (chipId != 0) {
            chipGroupCategory.check(chipId);
        }
    }
}
