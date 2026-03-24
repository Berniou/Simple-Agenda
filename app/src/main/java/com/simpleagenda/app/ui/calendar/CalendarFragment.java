package com.simpleagenda.app.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.database.AppDatabase;
import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.repository.TaskRepository;

import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {
    private CalendarViewModel viewModel;
    private CalendarAdapter calendarAdapter;
    private RecyclerView recyclerViewCalendar;
    private TextView textMonthYear;
    private ImageButton buttonPrevious;
    private ImageButton buttonNext;
    private ImageButton buttonToday;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupViewModel();
        setupClickListeners();
    }

    private void setupViews(View view) {
        recyclerViewCalendar = view.findViewById(R.id.recycler_view_calendar);
        textMonthYear = view.findViewById(R.id.text_month_year);
        buttonPrevious = view.findViewById(R.id.button_previous);
        buttonNext = view.findViewById(R.id.button_next);
        buttonToday = view.findViewById(R.id.button_today);
        
        calendarAdapter = new CalendarAdapter();
        recyclerViewCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        recyclerViewCalendar.setAdapter(calendarAdapter);
        
        calendarAdapter.setOnDateClickListener(day -> {
            // Navigate to agenda view for selected date
            // TODO: Implement navigation to agenda with selected date
        });
    }

    private void setupViewModel() {
        TaskRepository repository = new TaskRepository(
            AppDatabase.getInstance(requireContext()).taskDao()
        );
        
        viewModel = new ViewModelProvider(
            this,
            new CalendarViewModel.Factory(repository)
        ).get(CalendarViewModel.class);
        
        viewModel.getTasksForMonth().observe(getViewLifecycleOwner(), tasks -> {
            updateCalendar();
        });
        
        updateMonthDisplay();
    }

    private void setupClickListeners() {
        buttonPrevious.setOnClickListener(v -> {
            viewModel.navigateToPreviousMonth();
            updateMonthDisplay();
        });
        
        buttonNext.setOnClickListener(v -> {
            viewModel.navigateToNextMonth();
            updateMonthDisplay();
        });
        
        buttonToday.setOnClickListener(v -> {
            viewModel.goToToday();
            updateMonthDisplay();
        });
    }

    private void updateMonthDisplay() {
        textMonthYear.setText(viewModel.getCurrentMonthYear());
        updateCalendar();
    }

    private void updateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(viewModel.getCurrentYear(), viewModel.getCurrentMonth(), 1);
        
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        calendarAdapter.updateCalendar(firstDayOfWeek, daysInMonth, viewModel.getCurrentYear(), viewModel.getCurrentMonth());
    }
}
