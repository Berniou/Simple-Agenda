package com.simpleagenda.app.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpleagenda.app.R;
import com.simpleagenda.app.data.Task;

import java.util.ArrayList;
import java.util.List;

public class UnscheduledRowAdapter extends RecyclerView.Adapter<UnscheduledRowAdapter.Holder> {

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unscheduled_row, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Task t = items.get(position);
        holder.title.setText(t.getTitle());
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
