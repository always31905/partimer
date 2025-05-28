package com.example.haedal_project;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {
    private static final String TAG = "ChatRoomActivity";
    
    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView titleText;
    
    private String chatRoomId;
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    
    private DatabaseReference chatRef;
    private DatabaseReference roomRef;
    private FirebaseFirestore db;
    private ConcurrentHashMap<String, String> nicknameCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Firebase 인증 확인
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Intent에서 데이터 가져오기
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");

        if (chatRoomId == null || otherUserId == null) {
            Toast.makeText(this, "채팅방 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase 초기화
        currentUserId = auth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        chatRef = database.getReference().child("chat_rooms").child(chatRoomId).child("messages");
        roomRef = database.getReference().child("chat_rooms");
        db = FirebaseFirestore.getInstance();
        nicknameCache = new ConcurrentHashMap<>();
        
        // UI 초기화
        setupUI();
        
        // 상대방 닉네임 가져오기
        loadUserNickname();
        
        // 메시지 어댑터 설정
        adapter = new ChatMessageAdapter(this, currentUserId);
        recyclerView.setAdapter(adapter);

        // 메시지 리스너 설정
        chatRef.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Chat chat = snapshot.getValue(Chat.class);
                if (chat != null) {
                    // 발신자 닉네임 설정
                    String senderId = chat.getSenderId();
                    String cachedNickname = nicknameCache.get(senderId);
                    if (cachedNickname != null) {
                        chat.setSenderName(cachedNickname);
                        adapter.addMessage(chat);
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    } else {
                        // Firestore에서 닉네임 가져오기
                        db.collection("users")
                            .document(senderId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String nickname = documentSnapshot.getString("nickname");
                                    if (nickname != null && !nickname.isEmpty()) {
                                        nicknameCache.put(senderId, nickname);
                                        chat.setSenderName(nickname);
                                    } else {
                                        chat.setSenderName("Unknown");
                                    }
                                    adapter.addMessage(chat);
                                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching nickname", e);
                                chat.setSenderName("Unknown");
                                adapter.addMessage(chat);
                                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                            });
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "메시지 로드 실패", error.toException());
                Toast.makeText(ChatRoomActivity.this, 
                    "메시지를 불러오는 중 오류가 발생했습니다: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // 메시지 전송 버튼 리스너
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadUserNickname() {
        // Firestore에서 상대방 닉네임 가져오기
        db.collection("users")
            .document(otherUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nickname = documentSnapshot.getString("nickname");
                    if (nickname != null && !nickname.isEmpty()) {
                        nicknameCache.put(otherUserId, nickname);
                        titleText.setText(nickname);
                    }
                }
            })
            .addOnFailureListener(e -> 
                Log.e(TAG, "Error fetching other user's nickname", e));
    }

    private void setupUI() {
        recyclerView = findViewById(R.id.recycler_chat);
        messageInput = findViewById(R.id.edit_message);
        sendButton = findViewById(R.id.button_send);
        titleText = findViewById(R.id.text_title);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set initial title
        titleText.setText(otherUserName);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference()
            .child("chat_rooms")
            .child(chatRoomId)
            .child("messages");

        String messageId = chatRef.push().getKey();
        if (messageId == null) {
            Toast.makeText(this, "메시지 ID 생성 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 사용자의 닉네임 가져오기
        String cachedNickname = nicknameCache.get(currentUserId);
        if (cachedNickname != null) {
            sendMessageWithNickname(messageId, messageText, cachedNickname);
        } else {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String nickname = "Unknown";
                    if (documentSnapshot.exists()) {
                        String tempNick = documentSnapshot.getString("nickname");
                        if (tempNick != null && !tempNick.isEmpty()) {
                            nickname = tempNick;
                            nicknameCache.put(currentUserId, nickname);
                        }
                    }
                    sendMessageWithNickname(messageId, messageText, nickname);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching current user's nickname", e);
                    sendMessageWithNickname(messageId, messageText, "Unknown");
                });
        }
    }

    private void sendMessageWithNickname(String messageId, String messageText, String nickname) {
        Chat chat = new Chat(
            messageId,
            currentUserId,
            nickname,
            messageText,
            System.currentTimeMillis()
        );

        chatRef.child(messageId).setValue(chat)
            .addOnSuccessListener(aVoid -> {
                messageInput.setText("");
                Log.d(TAG, "Message sent successfully");
                
                // Update chat room's last message and increment unread count for other user
                DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference()
                    .child("chat_rooms")
                    .child(chatRoomId);
                
                roomRef.get().addOnSuccessListener(snapshot -> {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                    if (chatRoom != null) {
                        // unreadCount 업데이트
                        Map<String, Integer> unreadCounts = chatRoom.getUnreadCount();
                        if (unreadCounts == null) {
                            unreadCounts = new HashMap<>();
                        }
                        // 상대방의 읽지 않은 메시지 수 증가
                        int otherUserUnreadCount = unreadCounts.getOrDefault(otherUserId, 0);
                        unreadCounts.put(otherUserId, otherUserUnreadCount + 1);
                        
                        // 업데이트할 데이터 준비
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastMessage", messageText);
                        updates.put("lastMessageTime", chat.getTimestamp());
                        updates.put("unreadCount", unreadCounts);
                        
                        // 데이터 업데이트
                        roomRef.updateChildren(updates)
                            .addOnFailureListener(e -> 
                                Log.e(TAG, "Error updating chat room data", e));
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error sending message", e);
                Toast.makeText(ChatRoomActivity.this, 
                    "메시지 전송 실패: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 채팅방에 들어왔을 때 읽음 처리
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference()
            .child("chat_rooms")
            .child(chatRoomId);
        
        roomRef.get().addOnSuccessListener(snapshot -> {
            ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
            if (chatRoom != null) {
                // unreadCount를 0으로 초기화
                Map<String, Integer> unreadCounts = chatRoom.getUnreadCount();
                if (unreadCounts == null) {
                    unreadCounts = new HashMap<>();
                }
                unreadCounts.put(currentUserId, 0);
                roomRef.child("unreadCount").setValue(unreadCounts);
            }
        });
    }
} 