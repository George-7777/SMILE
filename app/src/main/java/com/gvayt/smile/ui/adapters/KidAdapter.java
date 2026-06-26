package com.gvayt.smile.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.gvayt.smile.R;
import com.gvayt.smile.model.network.dto.kid.KidResponse;

import java.util.ArrayList;
import java.util.List;

public class KidAdapter extends RecyclerView.Adapter<KidAdapter.ChildViewHolder> {
    private List<KidResponse> children;
    private OnChildClickListener listener;

    public interface OnChildClickListener {
        void onChildClick(KidResponse child);
        void onDeleteClick(KidResponse child);
    }

    public KidAdapter(List<KidResponse> children, OnChildClickListener listener) {
        this.children = children != null ? children : new ArrayList<>();
        this.listener = listener;
    }

    public void setListener(OnChildClickListener listener) {
        this.listener = listener;
    }

    // ---- Добавление элементов ----
    public void addItem(KidResponse child) {
        children.add(child);
        notifyItemInserted(children.size() - 1);
    }

    public void addItemAtPosition(KidResponse child, int position) {
        children.add(position, child);
        notifyItemInserted(position);
    }

    public void addAll(List<KidResponse> newChildren) {
        int startPos = children.size();
        children.addAll(newChildren);
        notifyItemRangeInserted(startPos, newChildren.size());
    }

    // ---- Удаление элементов ----
    public void removeItem(int position) {
        if (position >= 0 && position < children.size()) {
            children.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeItemById(int childId) {
        int position = findPositionById(childId);
        if (position != -1) {
            removeItem(position);
        }
    }

    public void clear() {
        int size = children.size();
        children.clear();
        notifyItemRangeRemoved(0, size);
    }

    // ---- Обновление элементов ----
    public void updateItem(int position, KidResponse newChild) {
        if (position >= 0 && position < children.size()) {
            children.set(position, newChild);
            notifyItemChanged(position);
        }
    }

    // ---- Поиск ----
    private int findPositionById(int childId) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getId() == childId) {
                return i;
            }
        }
        return -1;
    }

    // ---- Стандартные методы RecyclerView ----
    @Override
    public int getItemCount() {
        return children.size();
    }

    @Override
    public ChildViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChildViewHolder holder, int position) {
        KidResponse child = children.get(position);
        holder.bind(child, listener);
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvChildName, tvChildUsername, tvTasksCount;
        //private final ImageButton btnDelete;

        ChildViewHolder(View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tv_child_name);
            tvChildUsername = itemView.findViewById(R.id.tv_child_username);
            tvTasksCount = itemView.findViewById(R.id.tv_tasks_count);
            //btnDelete = itemView.findViewById(R.id.btn_delete_child);
        }

        void bind(KidResponse child, OnChildClickListener listener) {
            tvChildName.setText(child.getFio());
            tvChildUsername.setText(itemView.getContext().getString(R.string.template_username_kid, child.getLogin()));
            tvTasksCount.setText(itemView.getContext().getString(R.string.tasks_count, child.getTasks().size()));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onChildClick(child);
            });

            //btnDelete.setOnClickListener(v -> {
            //    if (listener != null) listener.onDeleteClick(child);
            //});
        }
    }
}