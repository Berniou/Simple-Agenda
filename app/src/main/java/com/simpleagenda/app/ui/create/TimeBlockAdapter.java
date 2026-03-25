package com.simpleagenda.app.ui.create;

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

import java.util.Locale;

public class TimeBlockAdapter extends ListAdapter<TimeBlock, TimeBlockAdapter.TimeBlockViewHolder> {
    
    private OnTimeBlockClickListener onTimeBlockClickListener;
    private OnTimeBlockLongClickListener onTimeBlockLongClickListener;
    
    public interface OnTimeBlockClickListener {
        void onTimeBlockClick(TimeBlock timeBlock);
    }
    
    public interface OnTimeBlockLongClickListener {
        void onTimeBlockLongClick(TimeBlock timeBlock);
    }
    
    public TimeBlockAdapter() {
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
    public TimeBlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_block, parent, false);
        return new TimeBlockViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TimeBlockViewHolder holder, int position) {
        TimeBlock timeBlock = getItem(position);
        holder.bind(timeBlock);
    }
    
    class TimeBlockViewHolder extends RecyclerView.ViewHolder {
        private TextView textHour;
        private TextView textTitle;
        private View categoryIndicator;
        private View background;
        
        public TimeBlockViewHolder(@NonNull View itemView) {
            super(itemView);
            textHour = itemView.findViewById(R.id.text_hour);
            textTitle = itemView.findViewById(R.id.text_title);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            background = itemView.findViewById(R.id.background);
        }
        
        public void bind(TimeBlock timeBlock) {
            // Afficher l'heure (0-23)
            textHour.setText(String.format(Locale.getDefault(), "%02dh", timeBlock.getHour()));
            
            // Afficher le titre ou vide
            if (timeBlock.getTitle() != null && !timeBlock.getTitle().isEmpty()) {
                textTitle.setText(timeBlock.getTitle());
                textTitle.setAlpha(1.0f);
            } else {
                textTitle.setText("");
                textTitle.setAlpha(0.3f);
            }
            
            // Couleur de catégorie
            int colorRes = getCategoryColor(timeBlock.getCategory());
            categoryIndicator.setBackgroundColor(itemView.getContext().getColor(colorRes));
            
            // Style si planifié ou non
            if (timeBlock.isScheduled()) {
                background.setBackgroundColor(itemView.getContext().getColor(R.color.surface_container_lowest));
            } else {
                background.setBackgroundColor(itemView.getContext().getColor(R.color.surface_container_low));
            }
            
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
