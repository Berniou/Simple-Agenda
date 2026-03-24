package com.simpleagenda.app.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    
    private List<Integer> days = new ArrayList<>();
    private int currentYear;
    private int currentMonth;
    private OnDateClickListener onDateClickListener;
    
    public interface OnDateClickListener {
        void onDateClick(int day);
    }
    
    public void setOnDateClickListener(OnDateClickListener listener) {
        this.onDateClickListener = listener;
    }
    
    public void updateCalendar(int firstDayOfWeek, int daysInMonth, int year, int month) {
        this.currentYear = year;
        this.currentMonth = month;
        
        days.clear();
        
        // Add empty spaces for days before month starts
        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(null);
        }
        
        // Add all days of the month
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Integer day = days.get(position);
        holder.bind(day, currentYear, currentMonth);
    }
    
    @Override
    public int getItemCount() {
        return days.size();
    }
    
    class CalendarViewHolder extends RecyclerView.ViewHolder {
        private TextView textDay;
        private View taskIndicator;
        
        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.text_day);
            taskIndicator = itemView.findViewById(R.id.task_indicator);
        }
        
        public void bind(Integer day, int year, int month) {
            if (day == null) {
                textDay.setText("");
                textDay.setAlpha(0.3f);
                taskIndicator.setVisibility(View.GONE);
                itemView.setClickable(false);
            } else {
                textDay.setText(String.valueOf(day));
                textDay.setAlpha(1.0f);
                
                // Check if this day is today
                java.util.Calendar today = java.util.Calendar.getInstance();
                boolean isToday = day == today.get(java.util.Calendar.DAY_OF_MONTH) &&
                                month == today.get(java.util.Calendar.MONTH) &&
                                year == today.get(java.util.Calendar.YEAR);
                
                if (isToday) {
                    textDay.setTextColor(itemView.getContext().getColor(R.color.primary));
                    textDay.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                } else {
                    textDay.setTextColor(itemView.getContext().getColor(R.color.on_surface));
                    textDay.setTypeface(android.graphics.Typeface.DEFAULT);
                }
                
                // Show task indicator (for now, just show it for demonstration)
                // TODO: Check if there are tasks for this day
                taskIndicator.setVisibility(Math.random() > 0.7 ? View.VISIBLE : View.GONE);
                
                itemView.setClickable(true);
                itemView.setOnClickListener(v -> {
                    if (onDateClickListener != null) {
                        onDateClickListener.onDateClick(day);
                    }
                });
            }
        }
    }
}
