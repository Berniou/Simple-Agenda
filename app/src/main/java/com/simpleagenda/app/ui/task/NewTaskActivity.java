package com.simpleagenda.app.ui.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.simpleagenda.app.R;
import com.simpleagenda.app.SimpleAgendaApp;
import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.databinding.ActivityNewTaskBinding;
import com.simpleagenda.app.ui.common.TaskPalette;

public class NewTaskActivity extends AppCompatActivity {

    private ActivityNewTaskBinding binding;
    private AgendaRepository repository;

    private int colorIndex = 0;
    private int durationHours = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = ((SimpleAgendaApp) getApplication()).getRepository();

        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        ChipGroup colorGroup = binding.colorGroup;
        colorGroup.check(R.id.color_0);
        colorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                return;
            }
            if (checkedId == R.id.color_0) {
                colorIndex = 0;
            } else if (checkedId == R.id.color_1) {
                colorIndex = 1;
            } else if (checkedId == R.id.color_2) {
                colorIndex = 2;
            } else if (checkedId == R.id.color_3) {
                colorIndex = 3;
            } else if (checkedId == R.id.color_4) {
                colorIndex = 4;
            }
            colorIndex = Math.max(0, Math.min(colorIndex, TaskPalette.COUNT - 1));
        });

        MaterialButtonToggleGroup durationGroup = binding.durationToggle;
        durationGroup.check(R.id.duration_2);
        durationGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.duration_1) {
                durationHours = 1;
            } else if (checkedId == R.id.duration_2) {
                durationHours = 2;
            } else if (checkedId == R.id.duration_3) {
                durationHours = 3;
            } else if (checkedId == R.id.duration_4) {
                durationHours = 4;
            }
            updateDurationLabel();
        });

        updateDurationLabel();

        binding.buttonSave.setOnClickListener(v -> save());
    }

    private void updateDurationLabel() {
        binding.durationLabel.setText(getString(R.string.duration_selected_format, durationHours));
    }

    private void save() {
        String title = binding.inputTitle.getText() != null
                ? binding.inputTitle.getText().toString().trim()
                : "";
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, R.string.task_title_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        String description = binding.inputDescription.getText() != null
                ? binding.inputDescription.getText().toString().trim()
                : "";

        Task task = new Task();
        task.setTitle(title);
        task.setColorIndex(colorIndex);
        task.setDurationHours(durationHours);
        task.setDescription(description);

        repository.insertTask(task, this::finish);
    }
}
