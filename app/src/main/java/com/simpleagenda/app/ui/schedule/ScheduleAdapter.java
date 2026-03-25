package com.simpleagenda.app.ui.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.model.TaskCategory;
import com.simpleagenda.app.data.model.TimeBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleAdapter extends ListAdapter<TimeBlock, ScheduleAdapter.ScheduleViewHolder> {
    
    private OnTimeBlockMoveListener onTimeBlockMoveListener;
    private OnTimeBlockClickListener onTimeBlockClickListener;
    private boolean dragAndDropEnabled = false;
    
    public interface OnTimeBlockMoveListener {
        void onTimeBlockMoved(TimeBlock fromBlock, int toHour);
    }
    
    public interface OnTimeBlockClickListener {
        void onTimeBlockClick(TimeBlock timeBlock);
    }
    
    public ScheduleAdapter() {
        super(DIFF_CALLBACK);
    }
    
    public void setOnTimeBlockMoveListener(OnTimeBlockMoveListener listener) {
        this.onTimeBlockMoveListener = listener;
    }
    
    public void setOnTimeBlockClickListener(OnTimeBlockClickListener listener) {
        this.onTimeBlockClickListener = listener;
    }
    
    public void enableDragAndDrop() {
        this.dragAndDropEnabled = true;
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
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_block, parent, false);
        return new ScheduleViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        TimeBlock timeBlock = getItem(position);
        holder.bind(timeBlock, dragAndDropEnabled);
    }
    
    @Override
    public int getItemViewType(int position) {
        // Retourner différents types pour les heures vides vs occupées
        TimeBlock timeBlock = getItem(position);
        return timeBlock != null && timeBlock.getTitle() != null ? 1 : 0;
    }
    
    public ItemTouchHelper.Callback createItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, 
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                @NonNull RecyclerView.ViewHolder viewHolder, 
                                @NonNull RecyclerView.ViewHolder target) {
                
                if (viewHolder.getItemViewType() != target.getItemViewType()) {
                    return false;
                }
                
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                if (fromPosition != toPosition) {
                    TimeBlock fromBlock = getItem(fromPosition);
                    notifyItemMoved(fromPosition, toPosition);
                    
                    if (onTimeBlockMoveListener != null) {
                        onTimeBlockMoveListener.onTimeBlockMoved(fromBlock, toPosition);
                    }
                }
                
                return true;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Ne pas permettre le swipe, seulement le drag & drop
            }
        };
    }
    
    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private TextView textHour;
        private TextView textTitle;
        private View categoryIndicator;
        private View background;
        
        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            textHour = itemView.findViewById(R.id.text_hour);
            textTitle = itemView.findViewById(R.id.text_title);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            background = itemView.findViewById(R.id.background);
        }
        
        public void bind(TimeBlock timeBlock, boolean enableDrag) {
            // Afficher l'heure
            textHour.setText(String.format("%02dh", timeBlock.getHour()));
            
            // Afficher le titre
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
            
            // Background pour le drag & drop
            if (enableDrag) {
                background.setBackgroundResource(R.drawable.draggable_background);
            }
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (onTimeBlockClickListener != null) {
                    onTimeBlockClickListener.onTimeBlockClick(timeBlock);
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
