package com.simpleagenda.app.ui.select;

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
import com.simpleagenda.app.data.model.TaskCategory;
import com.simpleagenda.app.data.model.TimeBlock;

public class TaskSelectionAdapter extends ListAdapter<TimeBlock, TaskSelectionAdapter.TaskSelectionViewHolder> {
    
    private OnTaskSelectionListener onTaskSelectionListener;
    private OnTaskClickListener onTaskClickListener;
    
    public interface OnTaskSelectionListener {
        void onTaskSelectionChanged(int selectedCount);
    }
    
    public interface OnTaskClickListener {
        void onTaskClick(TimeBlock timeBlock);
    }
    
    public TaskSelectionAdapter() {
        super(DIFF_CALLBACK);
    }
    
    public void setOnTaskSelectionListener(OnTaskSelectionListener listener) {
        this.onTaskSelectionListener = listener;
    }
    
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.onTaskClickListener = listener;
    }
    
    private static final DiffUtil.ItemCallback<TimeBlock> DIFF_CALLBACK = new DiffUtil.ItemCallback<TimeBlock>() {
        @Override
        public boolean areItemsTheSame(@NonNull TimeBlock oldItem, @NonNull TimeBlock newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull TimeBlock oldItem, @NonNull TimeBlock newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getHour() == newItem.getHour() &&
                   oldItem.getCategory() == newItem.getCategory();
        }
    };
    
    @NonNull
    @Override
    public TaskSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_selection, parent, false);
        return new TaskSelectionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskSelectionViewHolder holder, int position) {
        TimeBlock timeBlock = getItem(position);
        holder.bind(timeBlock);
    }
    
    class TaskSelectionViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBoxSelected;
        private TextView textTitle;
        private TextView textDuration;
        private View categoryIndicator;
        private TimeBlock currentTimeBlock;
        
        public TaskSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxSelected = itemView.findViewById(R.id.checkbox_selected);
            textTitle = itemView.findViewById(R.id.text_title);
            textDuration = itemView.findViewById(R.id.text_duration);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
        }
        
        public void bind(TimeBlock timeBlock) {
            currentTimeBlock = timeBlock;
            
            // Titre
            textTitle.setText(timeBlock.getTitle());
            
            // Durée
            textDuration.setText(timeBlock.getDuration() + "h");
            
            // Couleur de catégorie
            int colorRes = getCategoryColor(timeBlock.getCategory());
            categoryIndicator.setBackgroundColor(itemView.getContext().getColor(colorRes));
            
            // Background de sélection
            itemView.setBackgroundResource(timeBlock.isScheduled() ? 
                R.drawable.selected_task_background : 
                R.drawable.unselected_task_background);
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (onTaskClickListener != null) {
                    onTaskClickListener.onTaskClick(timeBlock);
                }
            });
            
            // Checkbox listener
            checkBoxSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (onTaskSelectionListener != null) {
                    // Simuler la sélection (dans une vraie implémentation, 
                    // on maintiendrait une liste de tâches sélectionnées)
                    onTaskSelectionListener.onTaskSelectionChanged(isChecked ? 1 : 0);
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
