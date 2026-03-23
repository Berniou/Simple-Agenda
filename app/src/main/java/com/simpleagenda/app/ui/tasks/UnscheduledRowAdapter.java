package com.simpleagenda.app.ui.tasks;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.Task;

import java.util.ArrayList;
import java.util.List;

public class UnscheduledRowAdapter extends RecyclerView.Adapter<UnscheduledRowAdapter.Holder> {

    public interface DragPermission {
        boolean mayDrag(long taskId);
    }

    private final List<Task> items = new ArrayList<>();
    private @Nullable DragPermission dragPermission;

    public void setDragPermission(@Nullable DragPermission dragPermission) {
        this.dragPermission = dragPermission;
    }

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unscheduled_row, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Task t = items.get(position);
        holder.title.setText(t.getTitle());

        holder.itemView.setOnLongClickListener(v -> {
            if (dragPermission == null || !dragPermission.mayDrag(t.getId())) {
                Toast.makeText(v.getContext(), R.string.select_task_first, Toast.LENGTH_SHORT).show();
                return true;
            }
            ClipData clip = ClipData.newPlainText("task_id", String.valueOf(t.getId()));
            return v.startDragAndDrop(clip, new View.DragShadowBuilder(holder.itemView), null, 0);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.unscheduled_title);
        }
    }
}
