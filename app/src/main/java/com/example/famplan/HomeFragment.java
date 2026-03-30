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
        setupOverviewList(rootView, mInflater);
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
        TextView tvToday = view.findViewById(R.id.tv_count_today);
        TextView tvUpcoming = view.findViewById(R.id.tv_count_upcoming);
        TextView tvPending = view.findViewById(R.id.tv_count_pending);

        Calendar today = startOfDay(Calendar.getInstance());
        int todayCount = 0, upcomingCount = 0, pendingCount = 0;

        for (Task t : repository.getAllTasks()) {
            Calendar taskDate = parseTaskDate(t.getDate());
            if (taskDate == null) continue;

            if (taskDate.equals(today)) {
                todayCount++;
            } else if (taskDate.after(today) && !t.getStatus().equalsIgnoreCase("Completed")) {
                upcomingCount++;
            }
            if (t.getStatus().equalsIgnoreCase("Pending")) {
                pendingCount++;
            }
        }

        tvToday.setText(String.valueOf(todayCount));
        tvUpcoming.setText(String.valueOf(upcomingCount));
        tvPending.setText(String.valueOf(pendingCount));
    }

    private void setupNextUp(View view) {
        View nextUpCard = view.findViewById(R.id.card_next_up);
        Calendar today = startOfDay(Calendar.getInstance());

        List<Task> candidates = new ArrayList<>();
        for (Task t : repository.getAllTasks()) {
            Calendar taskDate = parseTaskDate(t.getDate());
            if (taskDate == null) continue;
            if (!taskDate.before(today) && !t.getStatus().equalsIgnoreCase("Completed")) {
                candidates.add(t);
            }
        }

        Collections.sort(candidates, (a, b) -> {
            Calendar da = parseTaskDate(a.getDate());
            Calendar db = parseTaskDate(b.getDate());
            if (da == null || db == null) return 0;
            int cmp = da.compareTo(db);
            return cmp != 0 ? cmp : parseTime(a.getStartTime()) - parseTime(b.getStartTime());
        });

        if (candidates.isEmpty()) {
            nextUpCard.setVisibility(View.GONE);
            return;
        }

        Task nextTask = candidates.get(0);
        nextUpCard.setVisibility(View.VISIBLE);
        nextUpCard.setOnClickListener(v -> openDetails(nextTask.getId()));

        ((TextView) view.findViewById(R.id.tv_next_task_title)).setText(nextTask.getTitle());
        TextView statusView = view.findViewById(R.id.tv_next_task_status);
        statusView.setText(nextTask.getStatus());
        applyStatusColor(statusView, nextTask.getStatus());
        ((TextView) view.findViewById(R.id.tv_next_task_time)).setText(nextTask.getStartTime());
        String name = nextTask.getAssigneeId().equals("2") ? "Alex" : "Lily";
        ((TextView) view.findViewById(R.id.tv_next_task_assignee)).setText("Assigned to: " + name);
        ((TextView) view.findViewById(R.id.tv_next_task_location)).setText("Location: " + nextTask.getLocation());
    }

    private void setupOverviewList(View view, LayoutInflater inflater) {
        LinearLayout listContainer = view.findViewById(R.id.layout_overview_list);
        listContainer.removeAllViews();

        Calendar today = startOfDay(Calendar.getInstance());
        List<Task> upcoming = new ArrayList<>();

        for (Task t : repository.getAllTasks()) {
            Calendar taskDate = parseTaskDate(t.getDate());
            if (taskDate == null) continue;
            if (!taskDate.before(today) && !t.getStatus().equalsIgnoreCase("Completed")) {
                upcoming.add(t);
            }
        }

        Collections.sort(upcoming, (a, b) -> {
            Calendar da = parseTaskDate(a.getDate());
            Calendar db = parseTaskDate(b.getDate());
            if (da == null || db == null) return 0;
            int cmp = da.compareTo(db);
            return cmp != 0 ? cmp : parseTime(a.getStartTime()) - parseTime(b.getStartTime());
        });

        for (Task task : upcoming) {
            View itemView = inflater.inflate(R.layout.item_task_overview, listContainer, false);
            itemView.setOnClickListener(v -> openDetails(task.getId()));

            ((TextView) itemView.findViewById(R.id.tv_item_title)).setText(task.getTitle());
            String userName = task.getAssigneeId().equals("2") ? "Alex" : "Lily";
            ((TextView) itemView.findViewById(R.id.tv_item_details)).setText(task.getStartTime() + " • " + userName);
            TextView status = itemView.findViewById(R.id.tv_item_status);
            status.setText(task.getStatus());
            applyStatusColor(status, task.getStatus());

            listContainer.addView(itemView);
        }
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

    /** Parses "DayOfWeek, Month Day" (e.g. "Monday, March 16") into a Calendar at midnight. */
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

    /** Parses "h:mm AM/PM" into minutes since midnight for sorting. */
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
