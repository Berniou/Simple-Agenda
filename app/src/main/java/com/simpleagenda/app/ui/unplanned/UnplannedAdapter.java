package com.simpleagenda.app.ui.unplanned;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.model.TaskCategory;
import com.simpleagenda.app.data.model.TimeBlock;

public class UnplannedAdapter extends ListAdapter<TimeBlock, UnplannedAdapter.UnplannedViewHolder> {
    
    private OnTimeBlockClickListener onTimeBlockClickListener;
    private OnTimeBlockLongClickListener onTimeBlockLongClickListener;
    
    public interface OnTimeBlockClickListener {
        void onTimeBlockClick(TimeBlock timeBlock);
    }
    
    public interface OnTimeBlockLongClickListener {
        void onTimeBlockLongClick(TimeBlock timeBlock);
    }
    
    public UnplannedAdapter() {
        super(DIFF_CALLBACK);
    }
    
    public void setOnTimeBlockClickListener(OnTimeBlockClickListener listener) {
        this.onTimeBlockClickListener = listener;
    }
    
    public void setOnTimeBlockLongClickListener(OnTimeBlockLongClickListener listener) {
        this.onTimeBlockLongClickListener = listener;
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
    public UnplannedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_unplanned_task, parent, false);
        return new UnplannedViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UnplannedViewHolder holder, int position) {
        TimeBlock timeBlock = getItem(position);
        holder.bind(timeBlock);
    }
    
    class UnplannedViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textDuration;
        private View categoryIndicator;
        
        public UnplannedViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textDuration = itemView.findViewById(R.id.text_duration);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
        }
        
        public void bind(TimeBlock timeBlock) {
            // Titre
            textTitle.setText(timeBlock.getTitle());
            
            // Durée
            textDuration.setText(timeBlock.getDuration() + "h");
            
            // Couleur de catégorie
            int colorRes = getCategoryColor(timeBlock.getCategory());
            categoryIndicator.setBackgroundColor(itemView.getContext().getColor(colorRes));
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (onTimeBlockClickListener != null) {
                    onTimeBlockClickListener.onTimeBlockClick(timeBlock);
                }
            });
            
            // Long click pour supprimer
            itemView.setOnLongClickListener(v -> {
                if (onTimeBlockLongClickListener != null) {
                    onTimeBlockLongClickListener.onTimeBlockLongClick(timeBlock);
                }
                return true;
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
