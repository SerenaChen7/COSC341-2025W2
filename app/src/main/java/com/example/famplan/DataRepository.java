package com.example.famplan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DataRepository {
    private static DataRepository instance;
    private List<User> users;
    private List<Task> tasks;
    private List<Notification> notifications;
    private User currentUser;
    private TimeZone vancouverTZ = TimeZone.getTimeZone("America/Vancouver");
    private SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.US);

    private DataRepository() {
        sdf.setTimeZone(vancouverTZ);
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        notifications = new ArrayList<>();

        User lily = new User("1", "Lily");
        User alex = new User("2", "Alex");
        users.add(lily);
        users.add(alex);
        currentUser = lily;

        Calendar cal = Calendar.getInstance(vancouverTZ);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayTs = cal.getTimeInMillis();
        String today = sdf.format(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, 1);
        long tomorrowTs = cal.getTimeInMillis();
        String tomorrow = sdf.format(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, 1);
        long dayAfterTs = cal.getTimeInMillis();
        String dayAfter = sdf.format(cal.getTime());

        tasks.add(new Task("t1", "Soccer Practice", "event", today, "4:30 PM", "5:30 PM", "Community Field", "2", "1", "Confirmed", "Weekly practice", "1 hour before", "Weekly", todayTs + (16 * 3600 + 30 * 60) * 1000));
        tasks.add(new Task("t4", "Dinner Prep", "task", today, "6:00 PM", "7:00 PM", "Home", "2", "1", "Confirmed", "", "None", "Daily", todayTs + (18 * 3600) * 1000));
        tasks.add(new Task("t6", "Team Meeting", "event", today, "10:00 AM", "11:00 AM", "Office", "1", "1", "Confirmed", "", "15 min before", "None", todayTs + (10 * 3600) * 1000));
        tasks.add(new Task("t9", "Morning Walk", "event", today, "7:30 AM", "8:30 AM", "Park", "1", "1", "Completed", "", "None", "Daily", todayTs + (7 * 3600 + 30 * 60) * 1000));

        tasks.add(new Task("t2", "Grocery Pickup", "task", tomorrow, "5:00 PM", "5:30 PM", "Save-On-Foods", "2", "1", "Pending", "", "30 min before", "None", tomorrowTs + (17 * 3600) * 1000));
        tasks.add(new Task("t3", "Doctor Appointment", "event", tomorrow, "2:00 PM", "3:00 PM", "Health Center", "1", "1", "Confirmed", "", "1 hour before", "None", tomorrowTs + (14 * 3600) * 1000));

        tasks.add(new Task("t5", "Trash Duty", "task", dayAfter, "7:00 AM", "7:30 AM", "Home", "2", "1", "Pending", "", "None", "Weekly", dayAfterTs + (7 * 3600) * 1000));
        tasks.add(new Task("t10", "Piano Lesson", "event", dayAfter, "4:00 PM", "5:00 PM", "Music School", "1", "1", "Confirmed", "", "1 hour before", "Weekly", dayAfterTs + (16 * 3600) * 1000));

        notifications.add(new Notification("n1", "Reminder: Grocery Pickup at 5:00 PM", "30 min ago", "reminder", "t2"));
        notifications.add(new Notification("n2", "Lily assigned you Trash Duty", "2 hours ago", "assignment", "t5"));
        notifications.add(new Notification("n3", "Schedule changed: Doctor Appointment moved to 6:30 PM", "5 hours ago", "change", "t3"));
        notifications.add(new Notification("n4", "Reminder: Soccer Practice at 4:30 PM", "6 hours ago", "reminder", "t1"));
        notifications.add(new Notification("n5", "Alex accepted Dinner Prep assignment", "8 hours ago", "assignment", "t4"));
        notifications.add(new Notification("n6", "New task created: Pick Up Kids", "1 day ago", "change", "t8"));
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) instance = new DataRepository();
        return instance;
    }

    public List<Task> getAllTasks() { return tasks; }
    public List<Notification> getNotifications() { return notifications; }
    public User getCurrentUser() { return currentUser; }
    public List<User> getUsers() { return users; }

    public String getTodayString() {
        Calendar cal = Calendar.getInstance(vancouverTZ);
        return sdf.format(cal.getTime());
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void deleteTask(String id) {
        tasks.removeIf(t -> t.getId().equals(id));
    }

    public Task getTaskById(String id) {
        for (Task t : tasks) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public User getUserById(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) return user;
        }
        return null;
    }

    public User getUserByName(String name) {
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name)) return user;
        }
        return null;
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                return;
            }
        }
    }

    public Task checkConflict(String date, String startTime) {
        for (Task t : tasks) {
            if (t.getDate().equals(date) && t.getStartTime().equals(startTime)) {
                return t;
            }
        }
        return null;
    }

    public Task checkConflict(String date, String startTime, String excludedTaskId) {
        for (Task t : tasks) {
            if (excludedTaskId != null && excludedTaskId.equals(t.getId())) {
                continue;
            }
            if (t.getDate().equals(date) && t.getStartTime().equals(startTime)) {
                return t;
            }
        }
        return null;
    }
}