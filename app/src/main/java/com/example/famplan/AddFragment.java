package com.example.famplan;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // 修正为 androidx 库
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class AddFragment extends Fragment {

    private EditText etTitle, etDate, etTime, etLocation;
    private AutoCompleteTextView tvType, tvAssignee;
    private Button btnCreate, btnGoHome, btnUndo;
    private ConstraintLayout layoutSuccess;
    private ImageView btnBack;
    private DataRepository repository;
    private Task lastAddedTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        repository = DataRepository.getInstance();

        etTitle = view.findViewById(R.id.et_title);
        tvType = view.findViewById(R.id.tv_type_dropdown);
        etDate = view.findViewById(R.id.et_date);
        etTime = view.findViewById(R.id.et_time);
        etLocation = view.findViewById(R.id.et_location);
        tvAssignee = view.findViewById(R.id.tv_assignee_dropdown);
        btnCreate = view.findViewById(R.id.btn_create);
        btnGoHome = view.findViewById(R.id.btn_go_home);
        btnUndo = view.findViewById(R.id.btn_undo);
        layoutSuccess = view.findViewById(R.id.layout_success_overlay);
        btnBack = view.findViewById(R.id.btn_back);

        setupDropdowns();
        setupDateTimePickers();

        btnCreate.setOnClickListener(v -> handleCreateAttempt());
        btnGoHome.setOnClickListener(v -> goHome());
        btnUndo.setOnClickListener(v -> undoLastTask());
        btnBack.setOnClickListener(v -> goHome());

        return view;
    }

    private void setupDropdowns() {
        String[] types = {"Task", "Event"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        tvType.setAdapter(typeAdapter);

        String[] familyMembers = {"Lily", "Alex"};
        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, familyMembers);
        tvAssignee.setAdapter(assigneeAdapter);
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
                etDate.setText(sdf.format(selected.getTime()));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                String amPm = hourOfDay < 12 ? "AM" : "PM";
                int displayHour = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                etTime.setText(String.format("%02d:%02d %s", displayHour, minute, amPm));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });
    }

    private void handleCreateAttempt() {
        String title = etTitle.getText().toString();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        // 核心冲突检测
        Task conflict = repository.checkConflict(date, time);
        if (conflict != null) {
            showConflictDialog(conflict);
        } else {
            saveTask();
        }
    }

    private void showConflictDialog(Task conflictingTask) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_conflict, null);
        // 使用默认主题，通过 Window 设置透明背景
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        
        TextView msg = dialogView.findViewById(R.id.tv_conflict_msg);
        msg.setText("This overlaps with " + conflictingTask.getTitle() + " at " + conflictingTask.getStartTime() + ".");

        dialogView.findViewById(R.id.btn_reschedule).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_keep_anyway).setOnClickListener(v -> {
            dialog.dismiss();
            saveTask();
        });
        dialogView.findViewById(R.id.btn_cancel_conflict).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void saveTask() {
        String assignee = tvAssignee.getText().toString();
        String status = "Pending";
        String assigneeId = assignee.equalsIgnoreCase("Alex") ? "2" : "1";

        if (assigneeId.equals(repository.getCurrentUser().getId())) {
            status = "Confirmed";
        }

        lastAddedTask = new Task(
                UUID.randomUUID().toString(),
                etTitle.getText().toString(),
                tvType.getText().toString(),
                etDate.getText().toString(),
                etTime.getText().toString(),
                "",
                etLocation.getText().toString(),
                assigneeId,
                repository.getCurrentUser().getId(),
                status,
                "",
                "30 min before",
                "None"
        );

        repository.addTask(lastAddedTask);
        layoutSuccess.setVisibility(View.VISIBLE);
    }

    private void undoLastTask() {
        if (lastAddedTask != null) {
            repository.deleteTask(lastAddedTask.getId());
            lastAddedTask = null;
        }
        layoutSuccess.setVisibility(View.GONE);
    }

    private void goHome() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
}
