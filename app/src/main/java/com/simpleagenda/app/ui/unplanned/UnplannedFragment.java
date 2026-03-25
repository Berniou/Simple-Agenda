package com.simpleagenda.app.ui.unplanned;

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

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

import java.util.List;

public class UnplannedFragment extends Fragment {
    private UnplannedViewModel viewModel;
    private UnplannedAdapter unplannedAdapter;
    private RecyclerView recyclerViewUnplanned;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unplanned, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
    }

    private void setupViews(View view) {
        recyclerViewUnplanned = view.findViewById(R.id.recycler_view_unplanned);
        
        unplannedAdapter = new UnplannedAdapter();
        recyclerViewUnplanned.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUnplanned.setAdapter(unplannedAdapter);
        
        unplannedAdapter.setOnTimeBlockClickListener(this::onTimeBlockClicked);
        unplannedAdapter.setOnTimeBlockLongClickListener(this::onTimeBlockLongClicked);
    }

    private void setupViewModel() {
        TimeBlockRepository repository = new TimeBlockRepository(
            AppDatabase.getInstance(requireContext()).timeBlockDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new UnplannedViewModel.Factory(repository)
        ).get(UnplannedViewModel.class);
        
        viewModel.getUnscheduledTimeBlocks().observe(getViewLifecycleOwner(), timeBlocks -> {
            unplannedAdapter.submitList(timeBlocks);
        });
    }

    private void onTimeBlockClicked(TimeBlock timeBlock) {
        // Ajouter la tâche à la sélection pour l'ajouter à une journée
        // TODO: Naviguer vers la page de sélection
    }

    private void onTimeBlockLongClicked(TimeBlock timeBlock) {
        // Supprimer la tâche
        viewModel.deleteTimeBlock(timeBlock);
    }
}
