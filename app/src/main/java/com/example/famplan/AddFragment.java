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
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class AddFragment extends Fragment {

    private EditText etTitle, etDate, etTime, etLocation;
    private AutoCompleteTextView tvType, tvAssignee;
    private Button btnCreate, btnGoHome, btnUndo;
    private ConstraintLayout layoutSuccess;
    private DataRepository repository;
    private Task lastAddedTask;
    
    // 强制锁定温哥华时区
    private TimeZone vancouverTZ = TimeZone.getTimeZone("America/Vancouver");
    private Calendar selectedCalendar = Calendar.getInstance(vancouverTZ);

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

        setupDropdowns();
        setupDateTimePickers();

        btnCreate.setOnClickListener(v -> handleCreateAttempt());
        btnGoHome.setOnClickListener(v -> goHome());
        btnUndo.setOnClickListener(v -> undoLastTask());
        view.findViewById(R.id.btn_back).setOnClickListener(v -> goHome());

        return view;
    }

    private void setupDropdowns() {
        String[] types = {"Task", "Event"};
        tvType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types));
        String[] familyMembers = {"Lily", "Alex"};
        tvAssignee.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, familyMembers));
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> {
            // 获取温哥华当前时间作为弹窗初始值
            Calendar nowInVancouver = Calendar.getInstance(vancouverTZ);
            DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, day);
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
                sdf.setTimeZone(vancouverTZ);
                etDate.setText(sdf.format(selectedCalendar.getTime()));
            }, nowInVancouver.get(Calendar.YEAR), nowInVancouver.get(Calendar.MONTH), nowInVancouver.get(Calendar.DAY_OF_MONTH));
            
            // 设置最小日期为温哥华当前的零点
            nowInVancouver.set(Calendar.HOUR_OF_DAY, 0);
            nowInVancouver.set(Calendar.MINUTE, 0);
            dpd.getDatePicker().setMinDate(nowInVancouver.getTimeInMillis());
            dpd.show();
        });

        etTime.setOnClickListener(v -> {
            Calendar nowInVancouver = Calendar.getInstance(vancouverTZ);
            new TimePickerDialog(requireContext(), (view1, hour, min) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
                selectedCalendar.set(Calendar.MINUTE, min);
                selectedCalendar.set(Calendar.SECOND, 0);
                
                String amPm = hour < 12 ? "AM" : "PM";
                int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
                etTime.setText(String.format(Locale.US, "%d:%02d %s", displayHour, min, amPm));
            }, nowInVancouver.get(Calendar.HOUR_OF_DAY), nowInVancouver.get(Calendar.MINUTE), false).show();
        });
    }

    private void handleCreateAttempt() {
        if (etTitle.getText().toString().isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        // 核心防错：对比温哥华当前时间
        Calendar realNow = Calendar.getInstance(vancouverTZ);
        if (selectedCalendar.before(realNow)) {
            Toast.makeText(getContext(), "Error: Selected time is in the past (Vancouver Time)!", Toast.LENGTH_LONG).show();
            return; 
        }

        Task conflict = repository.checkConflict(etDate.getText().toString(), etTime.getText().toString());
        if (conflict != null) {
            showConflictDialog(conflict);
        } else {
            saveTask();
        }
    }

    private void showConflictDialog(Task conflictingTask) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_conflict, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        ((TextView) dialogView.findViewById(R.id.tv_conflict_msg)).setText("This overlaps with " + conflictingTask.getTitle() + " at " + conflictingTask.getStartTime() + ".");
        dialogView.findViewById(R.id.btn_reschedule).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_keep_anyway).setOnClickListener(v -> { dialog.dismiss(); saveTask(); });
        dialogView.findViewById(R.id.btn_cancel_conflict).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void saveTask() {
        String assignee = tvAssignee.getText().toString();
        String assigneeId = assignee.equalsIgnoreCase("Alex") ? "2" : "1";
        String status = assigneeId.equals(repository.getCurrentUser().getId()) ? "Confirmed" : "Pending";

        lastAddedTask = new Task(UUID.randomUUID().toString(), etTitle.getText().toString(),
                tvType.getText().toString(), etDate.getText().toString(), etTime.getText().toString(),
                "", etLocation.getText().toString(), assigneeId, repository.getCurrentUser().getId(),
                status, "", "30 min before", "None", selectedCalendar.getTimeInMillis());

        repository.addTask(lastAddedTask);
        layoutSuccess.setVisibility(View.VISIBLE);
    }

    private void undoLastTask() {
        if (lastAddedTask != null) { repository.deleteTask(lastAddedTask.getId()); lastAddedTask = null; }
        layoutSuccess.setVisibility(View.GONE);
    }

    private void goHome() {
        if (getActivity() != null) getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }
}
