package com.simpleagenda.app.ui.create;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private TextView selectedCategory;
    private Button buttonSave;
    private Button buttonClear;

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
        selectedCategory = view.findViewById(R.id.selected_category);
        buttonSave = view.findViewById(R.id.button_save);
        buttonClear = view.findViewById(R.id.button_clear);
        
        timeBlockAdapter = new TimeBlockAdapter();
        recyclerViewTimeBlocks.setLayoutManager(new GridLayoutManager(getContext(), 4)); // 4 colonnes pour les heures
        recyclerViewTimeBlocks.setAdapter(timeBlockAdapter);
        
        timeBlockAdapter.setOnTimeBlockClickListener(timeBlock -> {
            // Afficher les détails du bloc horaire
            showTimeBlockDetails(timeBlock);
        });
        
        timeBlockAdapter.setOnTimeBlockLongClickListener(timeBlock -> {
            // Supprimer le bloc horaire
            viewModel.deleteTimeBlock(timeBlock);
        });
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
        selectedCategory.setText("Aucune");
        viewModel.clearCurrentTimeBlocks();
    }

    private void showTimeBlockDetails(TimeBlock timeBlock) {
        // TODO: Implémenter un dialog pour modifier le bloc horaire
    }
}
