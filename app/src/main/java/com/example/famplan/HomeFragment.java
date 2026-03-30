package com.example.famplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;

public class HomeFragment extends Fragment {

    private DataRepository repository;
    private View rootView;
    private LayoutInflater mInflater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mInflater = inflater;
        repository = DataRepository.getInstance();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 关键：每次回到主页时都重新刷新 UI
        if (rootView != null) {
            refreshUI();
        }
    }

    private void refreshUI() {
        setupSummary(rootView);
        setupNextUp(rootView);
        setupOverviewList(rootView, mInflater);
    }

    private void setupSummary(View view) {
        TextView tvToday = view.findViewById(R.id.tv_count_today);
        TextView tvUpcoming = view.findViewById(R.id.tv_count_upcoming);
        TextView tvPending = view.findViewById(R.id.tv_count_pending);

        List<Task> allTasks = repository.getAllTasks();
        int todayCount = 0;
        int upcomingCount = 0;
        int pendingCount = 0;

        for (Task t : allTasks) {
            if (t.getStatus().equalsIgnoreCase("Pending")) {
                pendingCount++;
            }
            if (t.getDate().contains("March 16")) { 
                todayCount++;
            } else {
                upcomingCount++;
            }
        }

        tvToday.setText(String.valueOf(todayCount));
        tvUpcoming.setText(String.valueOf(upcomingCount));
        tvPending.setText(String.valueOf(pendingCount));
    }

    private void setupNextUp(View view) {
        List<Task> tasks = repository.getAllTasks();
        View nextUpCard = view.findViewById(R.id.card_next_up);
        
        if (tasks.isEmpty()) {
            nextUpCard.setVisibility(View.GONE);
            return;
        }
        
        nextUpCard.setVisibility(View.VISIBLE);
        Task nextTask = tasks.get(0);
        
        nextUpCard.setOnClickListener(v -> openDetails(nextTask.getId()));

        TextView title = view.findViewById(R.id.tv_next_task_title);
        TextView status = view.findViewById(R.id.tv_next_task_status);
        TextView time = view.findViewById(R.id.tv_next_task_time);
        TextView assignee = view.findViewById(R.id.tv_next_task_assignee);
        TextView location = view.findViewById(R.id.tv_next_task_location);

        title.setText(nextTask.getTitle());
        status.setText(nextTask.getStatus());
        time.setText(nextTask.getStartTime());
        assignee.setText("Assigned to: " + (nextTask.getAssigneeId().equals("2") ? "Alex" : "Lily"));
        location.setText("Location: " + nextTask.getLocation());
    }

    private void setupOverviewList(View view, LayoutInflater inflater) {
        LinearLayout listContainer = view.findViewById(R.id.layout_overview_list);
        listContainer.removeAllViews();

        List<Task> tasks = repository.getAllTasks();
        for (int i = 0; i < Math.min(tasks.size(), 8); i++) {
            Task task = tasks.get(i);
            View itemView = inflater.inflate(R.layout.item_task_overview, listContainer, false);
            
            itemView.setOnClickListener(v -> openDetails(task.getId()));

            TextView title = itemView.findViewById(R.id.tv_item_title);
            TextView details = itemView.findViewById(R.id.tv_item_details);
            TextView status = itemView.findViewById(R.id.tv_item_status);

            title.setText(task.getTitle());
            String userName = task.getAssigneeId().equals("2") ? "Alex" : "Lily";
            details.setText(task.getStartTime() + " • " + userName);
            status.setText(task.getStatus());

            if (task.getStatus().equalsIgnoreCase("Confirmed")) {
                status.setBackgroundResource(R.color.status_confirmed_bg);
                status.setTextColor(getResources().getColor(R.color.status_confirmed_text));
            } else if (task.getStatus().equalsIgnoreCase("Pending")) {
                status.setBackgroundResource(R.color.status_pending_bg);
                status.setTextColor(getResources().getColor(R.color.status_pending_text));
            } else if (task.getStatus().equalsIgnoreCase("Completed")) {
                status.setBackgroundResource(R.color.status_completed_bg);
                status.setTextColor(getResources().getColor(R.color.status_completed_text));
            }

            listContainer.addView(itemView);
        }
    }

    private void openDetails(String taskId) {
        Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
        intent.putExtra("TASK_ID", taskId);
        startActivity(intent);
    }
}
