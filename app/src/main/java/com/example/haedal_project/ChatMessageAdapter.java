package com.example.haedal_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final String currentUserId;
    private final List<Chat> messages;
    private final SimpleDateFormat timeFormat;

    public ChatMessageAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Chat chat = messages.get(position);
        holder.messageText.setText(chat.getMessage());
        
        // 시간 표시
        String time = timeFormat.format(new Date(chat.getTimestamp()));
        holder.timeText.setText(time);
        
        // 받은 메시지인 경우 발신자 이름 표시
        if (getItemViewType(position) == VIEW_TYPE_RECEIVED && holder.senderName != null) {
            String name = chat.getSenderName();
            if (name == null || name.isEmpty()) {
                name = "Unknown";
            }
            holder.senderName.setText(name);
            holder.senderName.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = messages.get(position);
        if (chat.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public void addMessage(Chat chat) {
        // 시간순으로 메시지 삽입 위치 찾기
        int insertPosition = 0;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getTimestamp() <= chat.getTimestamp()) {
                insertPosition = i + 1;
                break;
            }
        }
        
        messages.add(insertPosition, chat);
        notifyItemInserted(insertPosition);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        TextView senderName;  // 받은 메시지에만 존재

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
            timeText = itemView.findViewById(R.id.text_time);
            senderName = itemView.findViewById(R.id.text_sender_name);  // 받은 메시지 레이아웃에만 존재
        }
    }
} 