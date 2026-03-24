package com.simpleagenda.app.ui.agenda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.model.Task;
import com.simpleagenda.app.data.model.TaskCategory;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {
    
    private OnTaskClickListener onTaskClickListener;
    private OnTaskCheckedChangeListener onTaskCheckedChangeListener;
    
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }
    
    public interface OnTaskCheckedChangeListener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }
    
    public TaskAdapter() {
        super(DIFF_CALLBACK);
    }
    
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.onTaskClickListener = listener;
    }
    
    public void setOnTaskCheckedChangeListener(OnTaskCheckedChangeListener listener) {
        this.onTaskCheckedChangeListener = listener;
    }
    
    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.isCompleted() == newItem.isCompleted() &&
                   oldItem.getCategory() == newItem.getCategory();
        }
    };
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);
    }
    
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView textTime;
        private TextView textTitle;
        private TextView textDescription;
        private CheckBox checkboxCompleted;
        private View categoryIndicator;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.text_time);
            textTitle = itemView.findViewById(R.id.text_title);
            textDescription = itemView.findViewById(R.id.text_description);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
        }
        
        public void bind(Task task) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            if (task.getStartTime() != null) {
                textTime.setText(timeFormat.format(task.getStartTime()));
            } else {
                textTime.setText("");
            }
            
            textTitle.setText(task.getTitle());
            textTitle.setAlpha(task.isCompleted() ? 0.6f : 1.0f);
            
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                textDescription.setText(task.getDescription());
                textDescription.setVisibility(View.VISIBLE);
                textDescription.setAlpha(task.isCompleted() ? 0.6f : 0.8f);
            } else {
                textDescription.setVisibility(View.GONE);
            }
            
            checkboxCompleted.setChecked(task.isCompleted());
            
            // Set category color
            int colorRes = getCategoryColor(task.getCategory());
            categoryIndicator.setBackgroundColor(itemView.getContext().getColor(colorRes));
            
            itemView.setOnClickListener(v -> {
                if (onTaskClickListener != null) {
                    onTaskClickListener.onTaskClick(task);
                }
            });
            
            checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (onTaskCheckedChangeListener != null) {
                    onTaskCheckedChangeListener.onTaskCheckedChanged(task, isChecked);
                }
            });
        }
        
        private int getCategoryColor(TaskCategory category) {
            switch (category) {
                case BLUE:
                    return R.color.task_blue;
                case GREEN:
                    return R.color.task_green;
                case ORANGE:
                    return R.color.task_orange;
                default:
                    return R.color.task_blue;
            }
        }
    }
}
