package com.simpleagenda.app.ui.tasks;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.ui.common.TaskPalette;

import java.util.ArrayList;
import java.util.List;

public class PendingTasksAdapter extends RecyclerView.Adapter<PendingTasksAdapter.Holder> {

    private final List<Task> items = new ArrayList<>();

    public void submit(List<Task> tasks) {
        items.clear();
        if (tasks != null) {
            items.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_backlog, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Task t = items.get(position);
        holder.title.setText(t.getTitle());
        holder.duration.setText(holder.itemView.getContext().getString(
                R.string.duration_hours_short,
                t.getDurationHours()
        ));
        int accent = TaskPalette.accent(holder.itemView.getContext(), t.getColorIndex());
        GradientDrawable d = new GradientDrawable();
        d.setColor(accent);
        d.setCornerRadius(8f * holder.itemView.getResources().getDisplayMetrics().density);
        holder.accentBar.setBackground(d);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView duration;
        final View accentBar;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            duration = itemView.findViewById(R.id.task_duration);
            accentBar = itemView.findViewById(R.id.accent_bar);
        }
    }
}
