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
    private long timestamp; // 新增：用于精准排序

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
    public String getAssigneeId() { return assigneeId; }
    public String getCreatorId() { return creatorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public String getReminder() { return reminder; }
    public String getRepeat() { return repeat; }
    public long getTimestamp() { return timestamp; }
}
