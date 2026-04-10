package com.example.famplan;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String title;
    private String type;
    private String date;
    private String startTime;
    private String endTime;
    private String location;
    private String assigneeId;
    private String creatorId;
    private String status;
    private String notes;
    private String reminder;
    private String repeat;
    private long timestamp;

    public Task(String id, String title, String type, String date, String startTime, String endTime,
                String location, String assigneeId, String creatorId, String status, String notes,
                String reminder, String repeat, long timestamp) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.assigneeId = assigneeId;
        this.creatorId = creatorId;
        this.status = status;
        this.notes = notes;
        this.reminder = reminder;
        this.repeat = repeat;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getAssigneeId() { return assigneeId; }
    public String getCreatorId() { return creatorId; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getReminder() { return reminder; }
    public String getRepeat() { return repeat; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setType(String type) { this.type = type; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setLocation(String location) { this.location = location; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setReminder(String reminder) { this.reminder = reminder; }
    public void setRepeat(String repeat) { this.repeat = repeat; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}