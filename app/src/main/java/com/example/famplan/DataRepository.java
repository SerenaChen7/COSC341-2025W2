package com.example.famplan;

import java.util.ArrayList;
import java.util.List;

public class DataRepository {
    private static DataRepository instance;
    private List<User> users;
    private List<Task> tasks;
    private List<Notification> notifications;
    private User currentUser;

    private DataRepository() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        notifications = new ArrayList<>();

        // 初始化用户
        User lily = new User("1", "Lily");
        User alex = new User("2", "Alex");
        users.add(lily);
        users.add(alex);

        // 默认登录用户为 Lily
        currentUser = lily;

        // 初始化任务 (根据原型图数据)
        tasks.add(new Task("t1", "Soccer Practice", "event", "Monday, March 16", "4:30 PM", "5:30 PM", "Community Field", "2", "1", "Confirmed", "Weekly practice", "1 hour before", "Weekly"));
        tasks.add(new Task("t2", "Grocery Pickup", "task", "Thursday, March 19", "5:00 PM", "5:30 PM", "Save-On-Foods", "2", "1", "Pending", "", "30 min before", "None"));
        tasks.add(new Task("t3", "Doctor Appointment", "event", "Tuesday, March 17", "2:00 PM", "3:00 PM", "Health Center", "1", "1", "Confirmed", "", "1 hour before", "None"));
        tasks.add(new Task("t4", "Dinner Prep", "task", "Monday, March 16", "6:00 PM", "7:00 PM", "Home", "2", "1", "Confirmed", "", "None", "Daily"));
        tasks.add(new Task("t5", "Trash Duty", "task", "Tuesday, March 17", "7:00 AM", "7:30 AM", "Home", "2", "1", "Pending", "", "None", "Weekly"));
        tasks.add(new Task("t6", "Team Meeting", "event", "Monday, March 16", "10:00 AM", "11:00 AM", "Office", "1", "1", "Confirmed", "", "15 min before", "None"));
        tasks.add(new Task("t7", "Lunch Prep", "task", "Monday, March 16", "12:00 PM", "12:30 PM", "Home", "2", "1", "Confirmed", "", "None", "Daily"));
        tasks.add(new Task("t8", "Pick Up Kids", "task", "Monday, March 16", "3:15 PM", "3:45 PM", "Elementary School", "2", "1", "Pending", "", "None", "Daily"));
        tasks.add(new Task("t9", "Morning Walk", "event", "Monday, March 16", "7:30 AM", "8:30 AM", "Park", "1", "1", "Completed", "", "None", "Daily"));
        tasks.add(new Task("t10", "Piano Lesson", "event", "Tuesday, March 17", "4:00 PM", "5:00 PM", "Music School", "1", "1", "Confirmed", "", "1 hour before", "Weekly"));

        // 初始化通知
        notifications.add(new Notification("n1", "Reminder: Grocery Pickup at 5:00 PM", "30 min ago", "reminder", "t2"));
        notifications.add(new Notification("n2", "Lily assigned you Trash Duty", "2 hours ago", "assignment", "t5"));
        notifications.add(new Notification("n3", "Schedule changed: Doctor Appointment moved to 6:30 PM", "5 hours ago", "change", "t3"));
        notifications.add(new Notification("n4", "Reminder: Soccer Practice at 4:30 PM", "6 hours ago", "reminder", "t1"));
        notifications.add(new Notification("n5", "Alex accepted Dinner Prep assignment", "8 hours ago", "assignment", "t4"));
        notifications.add(new Notification("n6", "New task created: Pick Up Kids", "1 day ago", "change", "t8"));
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    // --- 数据操作方法 ---

    public List<Task> getAllTasks() { return tasks; }
    public List<Notification> getNotifications() { return notifications; }
    public List<User> getUsers() { return users; }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public void addTask(Task task) { tasks.add(task); }
    public void deleteTask(String id) {
        tasks.removeIf(t -> t.getId().equals(id));
    }
    
    public Task getTaskById(String id) {
        for (Task t : tasks) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    // 用于冲突检测：检查在同一日期，新任务的时间是否与已有任务重合
    // 这里是一个简化的演示逻辑
    public Task checkConflict(String date, String startTime) {
        for (Task t : tasks) {
            if (t.getDate().equals(date) && t.getStartTime().equals(startTime)) {
                return t;
            }
        }
        return null;
    }
}
