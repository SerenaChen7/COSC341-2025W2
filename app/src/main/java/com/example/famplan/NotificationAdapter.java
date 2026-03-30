package com.example.famplan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onActionClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.tvMsg.setText(notification.getMessage());
        holder.tvTime.setText(notification.getTimeReceived());

        // 根据类型设置图标
        if ("reminder".equals(notification.getIconType())) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_popup_reminder);
            holder.btnAction.setText("Open");
        } else if ("assignment".equals(notification.getIconType())) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_my_calendar); // 模拟人像图标
            holder.btnAction.setText("View");
        } else if ("change".equals(notification.getIconType())) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_today);
            holder.btnAction.setText("View");
        }

        holder.btnAction.setOnClickListener(v -> listener.onActionClick(notification));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvMsg, tvTime;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notif_icon);
            tvMsg = itemView.findViewById(R.id.tv_notif_msg);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            btnAction = itemView.findViewById(R.id.btn_notif_action);
        }
    }
}
