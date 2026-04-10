package com.example.famplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TaskDetailsActivity extends AppCompatActivity {

    private DataRepository repository;
    private Task currentTask;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        repository = DataRepository.getInstance();
        currentUser = repository.getCurrentUser();

        String taskId = getIntent().getStringExtra("TASK_ID");
        if (taskId != null) {
            currentTask = repository.getTaskById(taskId);
        }

        if (currentTask == null) {
            finish();
            return;
        }

        setupViews();
        setupButtons();

        View backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentTask != null) {
            currentTask = repository.getTaskById(currentTask.getId());
        }

        if (currentTask == null) {
            finish();
            return;
        }

        setupViews();
        setupButtons();
    }

    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvType = findViewById(R.id.tv_detail_type);
        if (tvTitle != null) tvTitle.setText(currentTask.getTitle());
        if (tvType != null) tvType.setText(currentTask.getType());

        setupRow(R.id.row_datetime, "Date & Time", currentTask.getDate() + "\n" + currentTask.getStartTime());
        setupRow(R.id.row_location, "Location", currentTask.getLocation());
        setupRow(R.id.row_assignee, "Assigned To", currentTask.getAssigneeId().equals("2") ? "Alex" : "Lily");
        setupRow(R.id.row_creator, "Created By", currentTask.getCreatorId().equals("1") ? "Lily" : "Alex");
        setupRow(R.id.row_reminder, "Reminder", currentTask.getReminder());
        setupRow(R.id.row_recurring, "Recurring", currentTask.getRepeat());

        updateStatusUI();
    }

    private void updateStatusUI() {
        TextView tvBadge = findViewById(R.id.tv_status_badge);
        if (tvBadge != null) {
            tvBadge.setText(currentTask.getStatus());
            if ("Confirmed".equalsIgnoreCase(currentTask.getStatus())) {
                tvBadge.setBackgroundResource(R.color.status_confirmed_bg);
                tvBadge.setTextColor(getResources().getColor(R.color.status_confirmed_text));
            } else if ("Pending".equalsIgnoreCase(currentTask.getStatus())) {
                tvBadge.setBackgroundResource(R.color.status_pending_bg);
                tvBadge.setTextColor(getResources().getColor(R.color.status_pending_text));
            } else if ("Completed".equalsIgnoreCase(currentTask.getStatus())) {
                tvBadge.setBackgroundResource(R.color.status_completed_bg);
                tvBadge.setTextColor(getResources().getColor(R.color.status_completed_text));
            }
        }
    }

    private void setupRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        if (row != null) {
            TextView tvLabel = row.findViewById(R.id.tv_row_label);
            TextView tvValue = row.findViewById(R.id.tv_row_value);
            if (tvLabel != null) tvLabel.setText(label);
            if (tvValue != null) tvValue.setText(value);
        }
    }

    private void setupButtons() {
        boolean isCreator = currentUser.getId().equals(currentTask.getCreatorId());
        boolean isAssignee = currentUser.getId().equals(currentTask.getAssigneeId());

        Button btnComplete = findViewById(R.id.btn_mark_complete);
        Button btnDelete = findViewById(R.id.btn_delete);
        Button btnEdit = findViewById(R.id.btn_edit);
        View layoutResponse = findViewById(R.id.layout_assignee_response);
        Button btnConfirm = findViewById(R.id.btn_confirm);
        Button btnDecline = findViewById(R.id.btn_decline);

        if (btnComplete != null) btnComplete.setVisibility(View.GONE);
        if (btnDelete != null) btnDelete.setVisibility(View.GONE);
        if (btnEdit != null) btnEdit.setVisibility(View.GONE);
        if (layoutResponse != null) layoutResponse.setVisibility(View.GONE);

        // DEGRADED: Show all buttons to all users regardless of role
        if (btnComplete != null) btnComplete.setVisibility(View.VISIBLE);
        if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
        if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
        if (layoutResponse != null) layoutResponse.setVisibility(View.VISIBLE);

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(TaskDetailsActivity.this, EditTaskActivity.class);
                intent.putExtra("TASK_ID", currentTask.getId());
                startActivity(intent);
            });
        }

        // --- 核心交互逻辑实现 ---

        // 1. Confirm 逻辑
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                currentTask.setStatus("Confirmed");
                updateStatusUI();
                setupButtons(); // 刷新按钮显示（隐藏 Confirm，显示 Mark Complete）
                // DEGRADED: Toast removed
            });
        }

        // 2. Mark Complete 逻辑
        if (btnComplete != null) {
            btnComplete.setOnClickListener(v -> {
                currentTask.setStatus("Completed");
                updateStatusUI();
                setupButtons(); // 隐藏 Mark Complete
                // DEGRADED: Toast removed
            });
        }

        // 3. Delete 与 Undo (保持原有逻辑)
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                View overlay = findViewById(R.id.layout_delete_overlay);
                if (overlay != null) {
                    overlay.setVisibility(View.VISIBLE);
                    View homeBtn = overlay.findViewById(R.id.btn_overlay_home);
                    View undoBtn = overlay.findViewById(R.id.btn_overlay_undo);
                    if (homeBtn != null) homeBtn.setOnClickListener(v2 -> {
                        repository.deleteTask(currentTask.getId());
                        finish();
                    });
                    if (undoBtn != null) undoBtn.setOnClickListener(v2 -> overlay.setVisibility(View.GONE));
                }
            });
        }
    }
}
