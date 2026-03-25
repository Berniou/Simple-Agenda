package com.simpleagenda.app.ui.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.TimeBlock;
import com.simpleagenda.app.data.repository.TimeBlockRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleFragment extends Fragment {
    private ScheduleViewModel viewModel;
    private ScheduleAdapter scheduleAdapter;
    private RecyclerView recyclerViewSchedule;
    private TextView textDateHeader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
        setupDragAndDrop();
    }

    private void setupViews(View view) {
        recyclerViewSchedule = view.findViewById(R.id.recycler_view_schedule);
        textDateHeader = view.findViewById(R.id.text_date_header);
        
        scheduleAdapter = new ScheduleAdapter();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 24); // 24 colonnes pour les heures
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerViewSchedule.setLayoutManager(layoutManager);
        recyclerViewSchedule.setAdapter(scheduleAdapter);
        
        scheduleAdapter.setOnTimeBlockMoveListener(this::onTimeBlockMoved);
        scheduleAdapter.setOnTimeBlockClickListener(this::onTimeBlockClicked);
    }

    private void setupViewModel() {
        TimeBlockRepository repository = new TimeBlockRepository(
            AppDatabase.getInstance(requireContext()).timeBlockDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new ScheduleViewModel.Factory(repository)
        ).get(ScheduleViewModel.class);
        
        viewModel.getScheduledTimeBlocks().observe(getViewLifecycleOwner(), timeBlocks -> {
            scheduleAdapter.submitList(timeBlocks);
            updateDateHeader();
        });
    }

    private void setupDragAndDrop() {
        // Configuration du drag & drop pour les blocs horaires
        scheduleAdapter.enableDragAndDrop();
    }

    private void updateDateHeader() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault());
        String today = dateFormat.format(Calendar.getInstance().getTime());
        textDateHeader.setText(today);
    }

    private void onTimeBlockMoved(TimeBlock fromBlock, int toHour) {
        // Déplacer le bloc vers le nouvel horaire
        fromBlock.setHour(toHour);
        viewModel.updateTimeBlock(fromBlock);
    }

    private void onTimeBlockClicked(TimeBlock timeBlock) {
        // Afficher les détails du bloc
        // TODO: Implémenter dialog de modification
    }
}
