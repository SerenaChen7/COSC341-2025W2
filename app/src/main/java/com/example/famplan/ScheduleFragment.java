package com.example.famplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private DataRepository repository;
    private LinearLayout container;
    private TextView tvTitle, tvDate;
    private Button btnToggle;
    private boolean isWeekView = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        repository = DataRepository.getInstance();

        this.container = view.findViewById(R.id.layout_schedule_container);
        tvTitle = view.findViewById(R.id.tv_schedule_title);
        tvDate = view.findViewById(R.id.tv_schedule_date);
        btnToggle = view.findViewById(R.id.btn_toggle_view);

        btnToggle.setOnClickListener(v -> {
            isWeekView = !isWeekView;
            updateUI();
        });

        updateUI();
        return view;
    }

    private void updateUI() {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (isWeekView) {
            tvTitle.setText("This Week");
            tvDate.setVisibility(View.GONE);
            btnToggle.setText("← Today View");
            setupWeekView(inflater);
        } else {
            tvTitle.setText("Today");
            tvDate.setVisibility(View.VISIBLE);
            btnToggle.setText("Week View →");
            setupTodayView(inflater);
        }
    }

    private void setupTodayView(LayoutInflater inflater) {
        String todayStr = repository.getTodayString();
        tvDate.setText(todayStr);
        
        List<Task> allTasks = new ArrayList<>(repository.getAllTasks());
        // 按时间排序
        Collections.sort(allTasks, (t1, t2) -> t1.getStartTime().compareTo(t2.getStartTime()));

        for (Task task : allTasks) {
            if (task.getDate().equals(todayStr)) {
                View itemView = inflater.inflate(R.layout.item_schedule_today, container, false);
                fillTodayItem(itemView, task);
                container.addView(itemView);
            }
        }
    }

    private void fillTodayItem(View view, Task task) {
        ((TextView) view.findViewById(R.id.tv_time)).setText(task.getStartTime());
        ((TextView) view.findViewById(R.id.tv_title)).setText(task.getTitle());
        ((TextView) view.findViewById(R.id.tv_type_label)).setText(task.getType());
        String name = task.getAssigneeId().equals("2") ? "Alex" : "Lily";
        ((TextView) view.findViewById(R.id.tv_assignee)).setText("Assigned to: " + name);
        ((TextView) view.findViewById(R.id.tv_location)).setText(task.getLocation());
        
        TextView status = view.findViewById(R.id.tv_status);
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

        view.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
            intent.putExtra("TASK_ID", task.getId());
            startActivity(intent);
        });
    }

    private void setupWeekView(LayoutInflater inflater) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
        SimpleDateFormat matchFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.US);

        // 显示接下来 7 天
        for (int i = 0; i < 7; i++) {
            String dayLabel = dayFormat.format(cal.getTime());
            if (i == 0) dayLabel += "(Today)";
            String dateLabel = dateFormat.format(cal.getTime());
            String matchDate = matchFormat.format(cal.getTime());

            View dayBox = inflater.inflate(R.layout.item_schedule_week_box, container, false);
            TextView tvDay = dayBox.findViewById(R.id.tv_week_day);
            TextView tvDateLabel = dayBox.findViewById(R.id.tv_week_date);
            LinearLayout tasksLayout = dayBox.findViewById(R.id.layout_week_tasks);

            tvDay.setText(dayLabel);
            tvDateLabel.setText(dateLabel);

            if (i == 0) {
                dayBox.setBackgroundResource(R.drawable.bg_week_box_today);
                tvDay.setTextColor(getResources().getColor(R.color.primary_blue));
            }

            fillWeekTasks(tasksLayout, matchDate, inflater);
            container.addView(dayBox);
            
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void fillWeekTasks(LinearLayout tasksLayout, String matchDate, LayoutInflater inflater) {
        List<Task> allTasks = new ArrayList<>(repository.getAllTasks());
        Collections.sort(allTasks, (t1, t2) -> t1.getStartTime().compareTo(t2.getStartTime()));

        for (Task task : allTasks) {
            if (task.getDate().equals(matchDate)) {
                View itemView = inflater.inflate(R.layout.item_task_overview, tasksLayout, false);
                
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemView.getLayoutParams();
                lp.setMargins(0, 0, 0, 8);
                itemView.setLayoutParams(lp);

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
                
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
                    intent.putExtra("TASK_ID", task.getId());
                    startActivity(intent);
                });
                tasksLayout.addView(itemView);
            }
        }
    }
}
