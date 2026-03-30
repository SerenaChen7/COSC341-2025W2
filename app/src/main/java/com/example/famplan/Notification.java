package com.example.famplan;

import java.io.Serializable;

public class Notification implements Serializable {
    private String id;
    private String message;
    private String timeReceived; // e.g., "30 min ago"
    private String iconType; // "reminder", "assignment", "change"
    private String taskId; // Associated task if any
    private boolean isRead;

    public Notification(String id, String message, String timeReceived, String iconType, String taskId) {
        this.id = id;
        this.message = message;
        this.timeReceived = timeReceived;
        this.iconType = iconType;
        this.taskId = taskId;
        this.isRead = false;
    }

    // Getters
    public String getMessage() { return message; }
    public String getTimeReceived() { return timeReceived; }
    public String getIconType() { return iconType; }
    public String getTaskId() { return taskId; }
}
