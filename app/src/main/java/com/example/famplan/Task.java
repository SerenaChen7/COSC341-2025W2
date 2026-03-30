package com.example.famplan;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String title;
    private String type; // "task" or "event"
    private String date; // Format: "Monday, March 16"
    private String startTime; // Format: "4:30 PM"
    private String endTime;
    private String location;
    private String assigneeId; // User ID
    private String creatorId;  // User ID
    private String status; // "Pending", "Confirmed", "Declined", "Completed"
    private String notes;
    private String reminder; // e.g., "1 hour before"
    private String repeat; // e.g., "Weekly"

    public Task(String id, String title, String type, String date, String startTime, String endTime, 
                String location, String assigneeId, String creatorId, String status, String notes, 
                String reminder, String repeat) {
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
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    public String getCreatorId() { return creatorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }
    public String getRepeat() { return repeat; }
    public void setRepeat(String repeat) { this.repeat = repeat; }
}
