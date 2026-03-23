package com.simpleagenda.app.ui.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.simpleagenda.app.R;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.ui.common.TaskPalette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskSelectAdapter extends RecyclerView.Adapter<TaskSelectAdapter.Holder> {

    public interface Listener {
        void onSelectionChanged();
    }

    private final List<Task> items = new ArrayList<>();
    private final Set<Long> selected = new HashSet<>();
    private Listener listener;
    private RecyclerView recyclerView;
    private ItemTouchHelper itemTouchHelper;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Task> tasks) {
        items.clear();
        if (tasks != null) {
            items.addAll(tasks);
        }
        selected.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    @NonNull
    public Set<Long> getSelectedIds() {
        return new HashSet<>(selected);
    }

    public int selectedCount() {
        return selected.size();
    }

    public int selectedDurationHours() {
        int sum = 0;
        for (Task t : items) {
            if (selected.contains(t.getId())) {
                sum += t.getDurationHours();
            }
        }
        return sum;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_select, parent, false);
        return new Holder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        
        ItemTouchHelper.Callback callback = new DragCallback(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Task t = items.get(position);
        holder.title.setText(t.getTitle());
        holder.duration.setText(holder.itemView.getContext().getString(R.string.duration_hours_short, t.getDurationHours()));

        int light = TaskPalette.lightBackground(holder.itemView.getContext(), t.getColorIndex());
        int accent = TaskPalette.accent(holder.itemView.getContext(), t.getColorIndex());
        holder.card.setCardBackgroundColor(light);

        boolean isSel = selected.contains(t.getId());
        float d = holder.itemView.getResources().getDisplayMetrics().density;
        holder.card.setStrokeWidth(isSel ? (int) (3 * d) : 0);
        holder.card.setStrokeColor(isSel ? accent : Color.TRANSPARENT);

        holder.state.setImageResource(
                isSel ? android.R.drawable.checkbox_on_background : android.R.drawable.ic_input_add
        );

        holder.itemView.setOnClickListener(v -> {
            if (selected.contains(t.getId())) {
                selected.remove(t.getId());
            } else {
                selected.add(t.getId());
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onSelectionChanged();
            }
        });

        holder.taskId = t.getId();
        holder.card.setLongClickable(true);
        holder.card.setOnLongClickListener(v -> {
            if (!selected.contains(t.getId())) {
                Toast.makeText(v.getContext(), R.string.select_task_first, Toast.LENGTH_SHORT).show();
                return true;
            }

            // ← CLÉ : bloquer les parents scrollables
        ViewParent parent = v.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).requestDisallowInterceptTouchEvent(true);
            }
            
            // Démarre le drag via ItemTouchHelper
            itemTouchHelper.startDrag(holder);
            return true;
        });
    }

    // Méthode pour réordonner (à appeler depuis onMove)
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView state;
        final TextView duration;
        final TextView title;
        long taskId;

        Holder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            state = itemView.findViewById(R.id.select_state);
            duration = itemView.findViewById(R.id.select_duration);
            title = itemView.findViewById(R.id.select_title);
        }
    }

    private class DragCallback extends ItemTouchHelper.Callback {

        private final TaskSelectAdapter adapter;

        DragCallback(TaskSelectAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // Enable drag in all directions for external drop
            int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(0, dragFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
           int from = viewHolder.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            adapter.moveItem(from, to); // ← Réordonne dans la colonne
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // No swipe functionality
        }

        @Override
        public void onSelectedChanged(@NonNull RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            // Remet le scroll normal à la fin du drag
            if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                ViewParent parent = recyclerView.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).requestDisallowInterceptTouchEvent(false);
                }
            }
        }
    }
}

