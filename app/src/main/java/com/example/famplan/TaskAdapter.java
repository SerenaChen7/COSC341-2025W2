package com.example.famplan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_overview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.title.setText(task.getTitle());
        String userName = task.getAssigneeId().equals("2") ? "Alex" : "Lily";
        holder.details.setText(task.getStartTime() + " • " + userName);
        holder.status.setText(task.getStatus());

        if (task.getStatus().equalsIgnoreCase("Confirmed")) {
            holder.status.setBackgroundResource(R.color.status_confirmed_bg);
            holder.status.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_confirmed_text));
        } else if (task.getStatus().equalsIgnoreCase("Pending")) {
            holder.status.setBackgroundResource(R.color.status_pending_bg);
            holder.status.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_pending_text));
        } else if (task.getStatus().equalsIgnoreCase("Completed")) {
            holder.status.setBackgroundResource(R.color.status_completed_bg);
            holder.status.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_completed_text));
        }

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, details, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_item_title);
            details = itemView.findViewById(R.id.tv_item_details);
            status = itemView.findViewById(R.id.tv_item_status);
        }
    }
}
