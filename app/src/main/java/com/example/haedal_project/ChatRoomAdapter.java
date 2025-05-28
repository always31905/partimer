package com.example.haedal_project;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {
    private static final String TAG = "ChatRoomAdapter";
    
    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    private final Context context;
    private final List<ChatRoom> chatRooms;
    private final String currentUserId;
    private final OnChatRoomClickListener listener;
    private final SimpleDateFormat dateFormat;
    private final ConcurrentHashMap<String, String> nicknameCache;
    private final FirebaseFirestore db;

    public ChatRoomAdapter(Context context, List<ChatRoom> chatRooms, String currentUserId, OnChatRoomClickListener listener) {
        this.context = context;
        this.chatRooms = chatRooms;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
        this.nicknameCache = new ConcurrentHashMap<>();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        
        // 상대방 ID 가져오기
        String otherUserId = chatRoom.getOtherUserId(currentUserId);
        
        // 캐시된 닉네임이 있는지 확인
        String cachedNickname = nicknameCache.get(otherUserId);
        if (cachedNickname != null) {
            holder.nameText.setText(cachedNickname);
        } else {
            // 임시로 기본 이름 표시
            holder.nameText.setText(chatRoom.getOtherUserName(currentUserId));
            
            // Firestore에서 닉네임 가져오기
            db.collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nickname = documentSnapshot.getString("nickname");
                        if (nickname != null && !nickname.isEmpty()) {
                            nicknameCache.put(otherUserId, nickname);
                            holder.nameText.setText(nickname);
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error fetching nickname for user: " + otherUserId, e));
        }

        // 마지막 메시지 표시
        String lastMessage = chatRoom.getLastMessage();
        holder.lastMessageText.setText(lastMessage != null && !lastMessage.isEmpty() 
            ? lastMessage : "새로운 채팅방");

        // 마지막 메시지 시간 표시
        long lastMessageTime = chatRoom.getLastMessageTime();
        if (lastMessageTime > 0) {
            holder.timeText.setText(dateFormat.format(new Date(lastMessageTime)));
        } else {
            holder.timeText.setText("");
        }

        // 읽지 않은 메시지 처리
        Map<String, Integer> unreadCounts = chatRoom.getUnreadCount();
        int unreadCount = 0;
        if (unreadCounts != null) {
            unreadCount = unreadCounts.getOrDefault(currentUserId, 0);
        }

        Log.d(TAG, "Unread count for room " + chatRoom.getRoomId() + ": " + unreadCount);

        if (unreadCount > 0) {
            // 읽지 않은 메시지 수 표시
            holder.unreadCountText.setVisibility(View.VISIBLE);
            holder.unreadCountText.setText(String.valueOf(unreadCount));
            
            // 텍스트 스타일 강조
            holder.nameText.setTypeface(Typeface.DEFAULT_BOLD);
            holder.lastMessageText.setTypeface(Typeface.DEFAULT_BOLD);
            holder.timeText.setTypeface(Typeface.DEFAULT_BOLD);
            
            // 텍스트 색상 변경
            int blackColor = context.getResources().getColor(android.R.color.black);
            holder.nameText.setTextColor(blackColor);
            holder.lastMessageText.setTextColor(blackColor);
            holder.timeText.setTextColor(blackColor);
            
            // 배경 설정
            holder.itemView.setBackgroundResource(R.drawable.bg_chat_room_unread);
        } else {
            // 읽은 메시지 스타일로 초기화
            holder.unreadCountText.setVisibility(View.GONE);
            
            // 기본 텍스트 스타일
            holder.nameText.setTypeface(Typeface.DEFAULT);
            holder.lastMessageText.setTypeface(Typeface.DEFAULT);
            holder.timeText.setTypeface(Typeface.DEFAULT);
            
            // 기본 텍스트 색상
            holder.nameText.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.lastMessageText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.timeText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            
            // 기본 배경으로 초기화
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        // 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatRoomClick(chatRoom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView lastMessageText;
        TextView timeText;
        TextView unreadCountText;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_name);
            lastMessageText = itemView.findViewById(R.id.text_last_message);
            timeText = itemView.findViewById(R.id.text_time);
            unreadCountText = itemView.findViewById(R.id.text_unread_count);
        }
    }
} 