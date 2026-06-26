package com.gvayt.smile.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gvayt.smile.R;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskResponse> tasks;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onDeleteClick(TaskResponse task);
    }

    public TaskAdapter(List<TaskResponse> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = tasks.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTaskText, tvTaskTime;
        private final ImageButton btnDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskText = itemView.findViewById(R.id.tv_task_text);
            tvTaskTime = itemView.findViewById(R.id.tv_task_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);
        }

        void bind(TaskResponse task, OnTaskClickListener listener) {
            tvTaskText.setText(task.getText());
            tvTaskTime.setText(task.getLocalTime());

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(task);
            });
        }
    }
}
