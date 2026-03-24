package com.simpleagenda.app.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.repository.TaskRepository;

public class SettingsFragment extends Fragment {
    private SettingsViewModel viewModel;
    private ImageView imageProfile;
    private TextView textUserName;
    private TextView textUserEmail;
    private Button buttonEditProfile;
    private Button buttonExport;
    private Button buttonImport;
    private Button buttonClearData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
        setupClickListeners();
    }

    private void setupViews(View view) {
        imageProfile = view.findViewById(R.id.image_profile);
        textUserName = view.findViewById(R.id.text_user_name);
        textUserEmail = view.findViewById(R.id.text_user_email);
        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        buttonExport = view.findViewById(R.id.button_export);
        buttonImport = view.findViewById(R.id.button_import);
        buttonClearData = view.findViewById(R.id.button_clear_data);
    }

    private void setupViewModel() {
        TaskRepository repository = new TaskRepository(
            AppDatabase.getInstance(requireContext()).taskDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new SettingsViewModel.Factory(repository)
        ).get(SettingsViewModel.class);
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to profile edit
        });
        
        buttonExport.setOnClickListener(v -> {
            viewModel.exportTasks();
        });
        
        buttonImport.setOnClickListener(v -> {
            viewModel.importTasks();
        });
        
        buttonClearData.setOnClickListener(v -> {
            // TODO: Show confirmation dialog
            viewModel.clearAllTasks();
        });
    }
}
