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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

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
        setupHeader(rootView);
        setupSummary(rootView);
        setupNextUp(rootView);
        setupGroupedLists(rootView);
    }

    private void setupHeader(View view) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        TextView tvDate = view.findViewById(R.id.tv_date);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String timeOfDay = hour < 12 ? "morning" : (hour < 17 ? "afternoon" : "evening");
        tvGreeting.setText("Good " + timeOfDay + ", " + repository.getCurrentUser().getName());

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
    }

    private void setupSummary(View view) {
        String todayStr = repository.getTodayString();
        int todayCount = 0, upcomingCount = 0, pendingCount = 0;

        for (Task t : repository.getAllTasks()) {
            if ("Pending".equalsIgnoreCase(t.getStatus())) pendingCount++;
            if (t.getDate().equals(todayStr)) {
                todayCount++;
            } else if (!t.getStatus().equalsIgnoreCase("Completed")) {
                Calendar taskDate = parseTaskDate(t.getDate());
                Calendar today = startOfDay(Calendar.getInstance());
                if (taskDate != null && taskDate.after(today)) {
                    upcomingCount++;
                }
            }
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

    private void setupNextUp(View view) {
        View card = view.findViewById(R.id.card_next_up);
        String todayStr = repository.getTodayString();

        List<Task> todayTasks = new ArrayList<>();
        for (Task t : repository.getAllTasks()) {
            if (t.getDate().equals(todayStr) && !"Completed".equalsIgnoreCase(t.getStatus())) {
                todayTasks.add(t);
            }
        }

        Collections.sort(todayTasks, (a, b) -> parseTime(a.getStartTime()) - parseTime(b.getStartTime()));

        if (todayTasks.isEmpty()) {
            card.setVisibility(View.GONE);
            return;
        }

        Task next = todayTasks.get(0);
        card.setVisibility(View.VISIBLE);
        card.setOnClickListener(v -> openDetails(next.getId()));

        ((TextView) card.findViewById(R.id.tv_next_task_title)).setText(next.getTitle());
        TextView statusView = card.findViewById(R.id.tv_next_task_status);
        statusView.setText(next.getStatus());
        applyStatusColor(statusView, next.getStatus());
        ((TextView) card.findViewById(R.id.tv_next_task_time)).setText(next.getStartTime());
        String name = next.getAssigneeId().equals("2") ? "Alex" : "Lily";
        ((TextView) card.findViewById(R.id.tv_next_task_assignee)).setText("Assigned to: " + name);
        ((TextView) card.findViewById(R.id.tv_next_task_location)).setText("Location: " + next.getLocation());
    }

    private void setupGroupedLists(View view) {
        LinearLayout toMeContainer = view.findViewById(R.id.layout_to_me_list);
        LinearLayout byMeContainer = view.findViewById(R.id.layout_by_me_list);
        toMeContainer.removeAllViews();
        byMeContainer.removeAllViews();

        String currentUserId = repository.getCurrentUser().getId();
        Calendar today = startOfDay(Calendar.getInstance());

        List<Task> tasks = new ArrayList<>(repository.getAllTasks());
        Collections.sort(tasks, (a, b) -> {
            Calendar da = parseTaskDate(a.getDate());
            Calendar db = parseTaskDate(b.getDate());
            if (da == null || db == null) return 0;
            int cmp = da.compareTo(db);
            return cmp != 0 ? cmp : parseTime(a.getStartTime()) - parseTime(b.getStartTime());
        });

        for (Task task : tasks) {
            if (task.getStatus().equalsIgnoreCase("Completed")) continue;
            Calendar taskDate = parseTaskDate(task.getDate());
            if (taskDate != null && taskDate.before(today)) continue;

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
        applyStatusColor(status, task.getStatus());

        itemView.setOnClickListener(v -> openDetails(task.getId()));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 4);
        itemView.setLayoutParams(lp);
    }

    private void applyStatusColor(TextView statusView, String status) {
        if (status.equalsIgnoreCase("Confirmed")) {
            statusView.setBackgroundResource(R.color.status_confirmed_bg);
            statusView.setTextColor(getResources().getColor(R.color.status_confirmed_text));
        } else if (status.equalsIgnoreCase("Pending")) {
            statusView.setBackgroundResource(R.color.status_pending_bg);
            statusView.setTextColor(getResources().getColor(R.color.status_pending_text));
        } else if (status.equalsIgnoreCase("Completed")) {
            statusView.setBackgroundResource(R.color.status_completed_bg);
            statusView.setTextColor(getResources().getColor(R.color.status_completed_text));
        }
    }

    private Calendar parseTaskDate(String dateStr) {
        try {
            String monthDay = dateStr.contains(", ") ? dateStr.split(", ", 2)[1] : dateStr;
            String[] parts = monthDay.trim().split(" ");
            int month = -1;
            for (int i = 0; i < MONTH_NAMES.length; i++) {
                if (MONTH_NAMES[i].equalsIgnoreCase(parts[0])) { month = i; break; }
            }
            if (month == -1) return null;
            int day = Integer.parseInt(parts[1]);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            return startOfDay(cal);
        } catch (Exception e) {
            return null;
        }
    }

    private int parseTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            String[] minParts = parts[1].split(" ");
            int min = Integer.parseInt(minParts[0]);
            String amPm = minParts[1];
            if (amPm.equalsIgnoreCase("PM") && hour != 12) hour += 12;
            if (amPm.equalsIgnoreCase("AM") && hour == 12) hour = 0;
            return hour * 60 + min;
        } catch (Exception e) {
            return 0;
        }
    }

    private Calendar startOfDay(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private void openDetails(String taskId) {
        Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
        intent.putExtra("TASK_ID", taskId);
        startActivity(intent);
    }
}
