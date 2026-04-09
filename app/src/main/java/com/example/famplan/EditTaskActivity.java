package com.example.famplan;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class EditTaskActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDate, etTime, etLocation, etNotes;
    private AutoCompleteTextView tvType, tvAssignee, tvReminder, tvRepeat;
    private Button btnSave, btnViewTask, btnClose;
    private View layoutSuccessOverlay;

    private DataRepository repository;
    private Task currentTask;

    private final TimeZone vancouverTZ = TimeZone.getTimeZone("America/Vancouver");
    private final Calendar selectedCalendar = Calendar.getInstance(vancouverTZ);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        repository = DataRepository.getInstance();

        String taskId = getIntent().getStringExtra("TASK_ID");
        if (taskId != null) {
            currentTask = repository.getTaskById(taskId);
        }

        if (currentTask == null) {
            finish();
            return;
        }

        bindViews();
        setupDropdowns();
        setupDateTimePickers();
        populateForm();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> handleSaveAttempt());
        btnViewTask.setOnClickListener(v -> openTaskDetails());
        btnClose.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        etTitle = findViewById(R.id.et_title);
        tvType = findViewById(R.id.tv_type_dropdown);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etLocation = findViewById(R.id.et_location);
        tvAssignee = findViewById(R.id.tv_assignee_dropdown);
        tvReminder = findViewById(R.id.tv_reminder_dropdown);
        tvRepeat = findViewById(R.id.tv_repeat_dropdown);
        etNotes = findViewById(R.id.et_notes);

        btnSave = findViewById(R.id.btn_save_changes);
        btnViewTask = findViewById(R.id.btn_view_task);
        btnClose = findViewById(R.id.btn_close_success);
        layoutSuccessOverlay = findViewById(R.id.layout_success_overlay);
    }

    private void setupDropdowns() {
        String[] types = {"Task", "Event"};
        String[] familyMembers = {"Lily", "Alex"};
        String[] reminderOptions = {"None", "15 min before", "30 min before", "1 hour before"};
        String[] repeatOptions = {"None", "Daily", "Weekly", "Monthly"};

        tvType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types));
        tvAssignee.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, familyMembers));
        tvReminder.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, reminderOptions));
        tvRepeat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, repeatOptions));
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> {
            Calendar initialCal = Calendar.getInstance(vancouverTZ);

            if (currentTask != null) {
                Calendar parsed = parseTaskDateTime(currentTask.getDate(), currentTask.getStartTime());
                if (parsed != null) {
                    initialCal.setTimeInMillis(parsed.getTimeInMillis());
                }
            }

            DatePickerDialog dpd = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedCalendar.set(Calendar.YEAR, year);
                        selectedCalendar.set(Calendar.MONTH, month);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
                        sdf.setTimeZone(vancouverTZ);
                        etDate.setText(sdf.format(selectedCalendar.getTime()));
                    },
                    initialCal.get(Calendar.YEAR),
                    initialCal.get(Calendar.MONTH),
                    initialCal.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show();
        });

        etTime.setOnClickListener(v -> {
            Calendar initialCal = Calendar.getInstance(vancouverTZ);

            if (currentTask != null) {
                Calendar parsed = parseTaskDateTime(
                        etDate.getText() != null && !etDate.getText().toString().trim().isEmpty()
                                ? etDate.getText().toString().trim()
                                : currentTask.getDate(),
                        currentTask.getStartTime()
                );
                if (parsed != null) {
                    initialCal.setTimeInMillis(parsed.getTimeInMillis());
                }
            }

            new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedCalendar.set(Calendar.MINUTE, minute);
                        selectedCalendar.set(Calendar.SECOND, 0);
                        selectedCalendar.set(Calendar.MILLISECOND, 0);

                        String amPm = hourOfDay < 12 ? "AM" : "PM";
                        int displayHour = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                        etTime.setText(String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm));
                    },
                    initialCal.get(Calendar.HOUR_OF_DAY),
                    initialCal.get(Calendar.MINUTE),
                    false
            ).show();
        });
    }

    private void populateForm() {
        etTitle.setText(currentTask.getTitle());
        tvType.setText(capitalizeFirst(currentTask.getType()), false);
        etDate.setText(currentTask.getDate());
        etTime.setText(currentTask.getStartTime());
        etLocation.setText(currentTask.getLocation());
        tvAssignee.setText(getUserNameFromId(currentTask.getAssigneeId()), false);
        tvReminder.setText(currentTask.getReminder(), false);
        tvRepeat.setText(currentTask.getRepeat(), false);

        // Requires Task.getNotes()
        if (currentTask.getNotes() != null) {
            etNotes.setText(currentTask.getNotes());
        }

        Calendar parsed = parseTaskDateTime(currentTask.getDate(), currentTask.getStartTime());
        if (parsed != null) {
            selectedCalendar.setTimeInMillis(parsed.getTimeInMillis());
        }
    }

    private void handleSaveAttempt() {
        String title = getText(etTitle);
        String type = getText(tvType);
        String date = getText(etDate);
        String time = getText(etTime);
        String assigneeName = getText(tvAssignee);

        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.error_title_required));
            return;
        }

        if (type.isEmpty()) {
            tvType.setError(getString(R.string.error_type_required));
            return;
        }

        if (date.isEmpty()) {
            etDate.setError(getString(R.string.error_date_required));
            return;
        }

        if (time.isEmpty()) {
            etTime.setError(getString(R.string.error_time_required));
            return;
        }

        if (assigneeName.isEmpty()) {
            tvAssignee.setError(getString(R.string.error_assignee_required));
            return;
        }

        Task conflictingTask = repository.checkConflict(date, time, currentTask.getId());
        if (conflictingTask != null) {
            showConflictDialog(conflictingTask);
            return;
        }

        saveChanges();
    }

    private Task findConflictingTask(String date, String time) {
        return repository.checkConflict(
                getText(etDate),
                getText(etTime),
                currentTask.getId()
        );
    }

    private void showConflictDialog(Task conflictingTask) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_conflict, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        TextView tvMessage = dialogView.findViewById(R.id.tv_conflict_msg);
        tvMessage.setText("This overlaps with " + conflictingTask.getTitle() + " at " + conflictingTask.getStartTime() + ".");

        dialogView.findViewById(R.id.btn_reschedule).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_keep_anyway).setOnClickListener(v -> {
            dialog.dismiss();
            saveChanges();
        });
        dialogView.findViewById(R.id.btn_cancel_conflict).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void saveChanges() {
        String assigneeName = getText(tvAssignee);
        User selectedUser = repository.getUserByName(assigneeName);
        String assigneeId = selectedUser != null ? selectedUser.getId() : currentTask.getAssigneeId();

        String updatedStatus = assigneeId.equals(repository.getCurrentUser().getId())
                ? "Confirmed"
                : "Pending";

        long updatedTimestamp = buildTimestamp(getText(etDate), getText(etTime));

        currentTask.setTitle(getText(etTitle));
        currentTask.setType(getText(tvType).toLowerCase(Locale.US));
        currentTask.setDate(getText(etDate));
        currentTask.setStartTime(getText(etTime));
        currentTask.setLocation(getText(etLocation));
        currentTask.setAssigneeId(assigneeId);
        currentTask.setStatus(updatedStatus);
        currentTask.setReminder(getText(tvReminder));
        currentTask.setRepeat(getText(tvRepeat));
        currentTask.setNotes(getText(etNotes));
        currentTask.setTimestamp(updatedTimestamp);

        repository.updateTask(currentTask);

        layoutSuccessOverlay.setVisibility(View.VISIBLE);
    }

    private void openTaskDetails() {
        Intent intent = new Intent(this, TaskDetailsActivity.class);
        intent.putExtra("TASK_ID", currentTask.getId());
        startActivity(intent);
        finish();
    }

    private String getText(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    private String getUserNameFromId(String userId) {
        return "2".equals(userId) ? "Alex" : "Lily";
    }

    private String capitalizeFirst(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1).toLowerCase(Locale.US);
    }

    private Calendar parseTaskDateTime(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d yyyy h:mm a", Locale.US);
            sdf.setTimeZone(vancouverTZ);

            int currentYear = Calendar.getInstance(vancouverTZ).get(Calendar.YEAR);
            return toCalendar(sdf.parse(date + " " + currentYear + " " + time));
        } catch (ParseException e) {
            return null;
        }
    }

    private Calendar toCalendar(java.util.Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance(vancouverTZ);
        cal.setTime(date);
        return cal;
    }

    private long buildTimestamp(String date, String time) {
        Calendar cal = parseTaskDateTime(date, time);
        return cal != null ? cal.getTimeInMillis() : currentTask.getTimestamp();
    }
}