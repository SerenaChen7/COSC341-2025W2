package com.example.famplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Collections;
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
        if (rootView != null) {
            refreshUI();
        }
    }

    private void refreshUI() {
        // 动态更新欢迎语和今日日期
        TextView tvGreeting = rootView.findViewById(R.id.tv_greeting);
        TextView tvDate = rootView.findViewById(R.id.tv_date);
        
        User currentUser = repository.getCurrentUser();
        tvGreeting.setText("Good morning, " + currentUser.getName());
        tvDate.setText(repository.getTodayString());

        List<Task> allTasks = new ArrayList<>(repository.getAllTasks());
        
        // 1. 时间排序逻辑
        Collections.sort(allTasks, (t1, t2) -> t1.getStartTime().compareTo(t2.getStartTime()));

        setupSummary(rootView, allTasks);
        setupNextUp(rootView, allTasks);
        setupGroupedLists(rootView, allTasks);
    }

    private void setupSummary(View view, List<Task> tasks) {
        int todayCount = 0, upcomingCount = 0, pendingCount = 0;
        String todayStr = repository.getTodayString();

        for (Task t : tasks) {
            if ("Pending".equalsIgnoreCase(t.getStatus())) pendingCount++;
            if (t.getDate().equals(todayStr)) todayCount++;
            else upcomingCount++;
        }

        updateSummaryCount(view.findViewById(R.id.include_today), String.valueOf(todayCount), "Today", android.R.drawable.ic_menu_recent_history);
        updateSummaryCount(view.findViewById(R.id.include_upcoming), String.valueOf(upcomingCount), "Upcoming", android.R.drawable.ic_menu_my_calendar);
        updateSummaryCount(view.findViewById(R.id.include_pending), String.valueOf(pendingCount), "Pending", android.R.drawable.ic_dialog_alert);
    }

    private void updateSummaryCount(View item, String count, String label, int iconRes) {
        if (item == null) return;
        ((TextView) item.findViewById(R.id.tv_summary_count)).setText(count);
        ((TextView) item.findViewById(R.id.tv_summary_label)).setText(label);
        ((ImageView) item.findViewById(R.id.iv_summary_icon)).setImageResource(iconRes);
    }

    private void setupNextUp(View view, List<Task> tasks) {
        View card = view.findViewById(R.id.card_next_up);
        String todayStr = repository.getTodayString();
        
        // 找出今天最早的任务作为 Next Up
        Task next = null;
        for (Task t : tasks) {
            if (t.getDate().equals(todayStr) && !"Completed".equalsIgnoreCase(t.getStatus())) {
                next = t;
                break;
            }
        }

        if (next == null) {
            card.setVisibility(View.GONE);
            return;
        }
        
        card.setVisibility(View.VISIBLE);
        ((TextView) card.findViewById(R.id.tv_next_task_title)).setText(next.getTitle());
        ((TextView) card.findViewById(R.id.tv_next_task_status)).setText(next.getStatus());
        ((TextView) card.findViewById(R.id.tv_next_task_time)).setText(next.getStartTime());
        
        Task finalNext = next;
        card.setOnClickListener(v -> openDetails(finalNext.getId()));
    }

    private void setupGroupedLists(View view, List<Task> tasks) {
        LinearLayout toMeContainer = view.findViewById(R.id.layout_to_me_list);
        LinearLayout byMeContainer = view.findViewById(R.id.layout_by_me_list);
        toMeContainer.removeAllViews();
        byMeContainer.removeAllViews();

        String currentUserId = repository.getCurrentUser().getId();

        for (Task task : tasks) {
            View itemView = mInflater.inflate(R.layout.item_task_overview, null);
            fillTaskItem(itemView, task);

            if (task.getAssigneeId().equals(currentUserId)) {
                toMeContainer.addView(itemView);
            } else if (task.getCreatorId().equals(currentUserId)) {
                byMeContainer.addView(itemView);
            }
        }
    }

    private void fillTaskItem(View itemView, Task task) {
        ((TextView) itemView.findViewById(R.id.tv_item_title)).setText(task.getTitle());
        String name = task.getAssigneeId().equals("2") ? "Alex" : "Lily";
        ((TextView) itemView.findViewById(R.id.tv_item_details)).setText(task.getStartTime() + " • " + name);
        
        TextView status = itemView.findViewById(R.id.tv_item_status);
        status.setText(task.getStatus());
        
        if ("Confirmed".equalsIgnoreCase(task.getStatus())) {
            status.setBackgroundResource(R.color.status_confirmed_bg);
            status.setTextColor(getResources().getColor(R.color.status_confirmed_text));
        } else if ("Pending".equalsIgnoreCase(task.getStatus())) {
            status.setBackgroundResource(R.color.status_pending_bg);
            status.setTextColor(getResources().getColor(R.color.status_pending_text));
        } else if ("Completed".equalsIgnoreCase(task.getStatus())) {
            status.setBackgroundResource(R.color.status_completed_bg);
            status.setTextColor(getResources().getColor(R.color.status_completed_text));
        }

        itemView.setOnClickListener(v -> openDetails(task.getId()));
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        itemView.setLayoutParams(lp);
    }

    private void openDetails(String taskId) {
        Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
        intent.putExtra("TASK_ID", taskId);
        startActivity(intent);
    }
}
