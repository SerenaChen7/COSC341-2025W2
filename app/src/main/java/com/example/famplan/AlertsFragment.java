package com.example.famplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlertsFragment extends Fragment {

    private DataRepository repository;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);
        repository = DataRepository.getInstance();

        RecyclerView recyclerView = view.findViewById(R.id.rv_alerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Notification> notifications = repository.getNotifications();
        
        adapter = new NotificationAdapter(notifications, notification -> {
            // 点击通知的 Action 按钮逻辑
            if (notification.getTaskId() != null) {
                Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
                intent.putExtra("TASK_ID", notification.getTaskId());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
}
