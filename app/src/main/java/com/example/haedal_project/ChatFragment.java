package com.example.haedal_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatRoomAdapter.OnChatRoomClickListener {
    private static final String TAG = "ChatFragment";
    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> chatRooms;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        
        // 현재 로그인한 사용자 확인
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return view;
        }
        currentUserId = auth.getCurrentUser().getUid();

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.recycler_chat_rooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRooms = new ArrayList<>();
        adapter = new ChatRoomAdapter(getContext(), chatRooms, currentUserId, this);
        recyclerView.setAdapter(adapter);

        // 채팅방 목록 로드
        loadChatRooms();

        return view;
    }

    private void loadChatRooms() {
        DatabaseReference chatRoomsRef = FirebaseDatabase.getInstance().getReference().child("chat_rooms");
        
        // lastMessageTime을 기준으로 내림차순 정렬
        chatRoomsRef.orderByChild("lastMessageTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRooms.clear();
                List<ChatRoom> tempRooms = new ArrayList<>();
                
                // 데이터를 임시 리스트에 저장
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                    if (chatRoom != null) {
                        // 현재 사용자가 참여한 채팅방만 표시
                        if (currentUserId.equals(chatRoom.getUser1Id()) || 
                            currentUserId.equals(chatRoom.getUser2Id())) {
                            chatRoom.setRoomId(snapshot.getKey());
                            tempRooms.add(chatRoom);
                        }
                    }
                }
                
                // 최신 메시지 순으로 정렬 (lastMessageTime 기준 내림차순)
                tempRooms.sort((room1, room2) -> 
                    Long.compare(room2.getLastMessageTime(), room1.getLastMessageTime()));
                
                // 정렬된 리스트를 chatRooms에 추가
                chatRooms.addAll(tempRooms);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "채팅방 로드 실패", error.toException());
                Toast.makeText(getContext(), "채팅방 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChatRoomClick(ChatRoom chatRoom) {
        Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
        intent.putExtra("chatRoomId", chatRoom.getRoomId());
        intent.putExtra("otherUserId", chatRoom.getOtherUserId(currentUserId));
        intent.putExtra("otherUserName", chatRoom.getOtherUserName(currentUserId));
        startActivity(intent);
    }
}