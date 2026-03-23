package com.simpleagenda.app.ui.tasks;

import android.content.ClipData;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Iterator;
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

    public void moveItem(int from, int to) {
        Collections.swap(items, from, to);
        notifyItemMoved(from, to);
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
        
        ItemTouchHelper.Callback callback = new DragCallback();
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
        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            moveItem(from, to);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // No swipe functionality
        }
    }
}

