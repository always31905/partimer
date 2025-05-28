package com.example.haedal_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailFragment extends Fragment {
    private TextView tvTitle, tvDate, tvTime, tvLocation, tvPay, tvContent, tvKeywords;
    private ProgressBar progressBar;
    private EditText etComment;
    private Button btnPostComment, btnChat;
    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private String postId;
    private JobPost currentPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        // View 초기화
        tvTitle    = v.findViewById(R.id.tvTitle);
        tvDate     = v.findViewById(R.id.tvDate);
        tvTime     = v.findViewById(R.id.tvTime);
        tvLocation = v.findViewById(R.id.tvLocation);
        tvPay      = v.findViewById(R.id.tvPay);
        tvContent  = v.findViewById(R.id.tvContent);
        tvKeywords = v.findViewById(R.id.tvKeywords);
        btnChat    = v.findViewById(R.id.btnChat);
        
        // 댓글 관련 View 초기화
        etComment = v.findViewById(R.id.etComment);
        btnPostComment = v.findViewById(R.id.btnPostComment);
        rvComments = v.findViewById(R.id.rvComments);
        
        // 댓글 RecyclerView 설정
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new CommentAdapter();
        rvComments.setAdapter(commentAdapter);

        // 인자로 전달된 게시글 ID 가져오기
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            if (postId != null && !postId.isEmpty()) {
                fetchPostDetail(postId);
                loadComments();
                
                // 댓글 등록 버튼 클릭 리스너
                btnPostComment.setOnClickListener(v1 -> {
                    String commentText = etComment.getText().toString().trim();
                    if (!commentText.isEmpty()) {
                        postComment(commentText);
                    }
                });

                // 채팅 버튼 클릭 리스너
                btnChat.setOnClickListener(v1 -> startChat());
            } else {
                showError("게시글 ID가 유효하지 않습니다.");
            }
        } else {
            showError("게시글 정보를 찾을 수 없습니다.");
        }

        return v;
    }

    private void startChat() {
        if (currentPost == null) {
            showError("게시글 정보를 불러올 수 없습니다.");
            return;
        }

        // Firebase 인증 확인
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showError("로그인이 필요합니다.");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        String authorId = currentPost.getWriterUid();
        String authorName = currentPost.getWriterName();
        
        Log.d("DetailFragment", "Starting chat - Current User: " + currentUserId);
        Log.d("DetailFragment", "Starting chat - Author ID: " + authorId);
        Log.d("DetailFragment", "Starting chat - Author Name: " + authorName);
        
        // 작성자 ID가 없는 경우
        if (authorId == null || authorId.isEmpty()) {
            showError("채팅을 시작할 수 없습니다: 작성자 정보가 없습니다.");
            return;
        }

        // 작성자 이름이 없는 경우 "Unknown"으로 표시
        if (authorName == null || authorName.isEmpty()) {
            authorName = "Unknown";
        }

        // 자신과는 채팅할 수 없음
        if (currentUserId.equals(authorId)) {
            showError("자신과는 채팅할 수 없습니다.");
            return;
        }

        // 채팅방 ID 생성 (작은 ID가 앞으로 오도록 하여 일관성 유지)
        String chatRoomId = currentUserId.compareTo(authorId) < 0 
            ? currentUserId + "_" + authorId 
            : authorId + "_" + currentUserId;

        Log.d("DetailFragment", "Created chat room ID: " + chatRoomId);

        final String finalAuthorName = authorName;

        // Firebase 데이터베이스 초기화 및 연결
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = database.getReference()
            .child("chat_rooms").child(chatRoomId);

        // 현재 사용자의 이름 가져오기
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String currentUserName = "Unknown";
                if (documentSnapshot.exists()) {
                    currentUserName = documentSnapshot.getString("nickname");
                    if (currentUserName == null || currentUserName.isEmpty()) {
                        currentUserName = documentSnapshot.getString("name");
                    }
                }
                
                if (currentUserName == null || currentUserName.isEmpty()) {
                    currentUserName = auth.getCurrentUser().getDisplayName();
                    if (currentUserName == null || currentUserName.isEmpty()) {
                        currentUserName = "Unknown";
                    }
                }

                final String finalCurrentUserName = currentUserName;

                // 채팅방 존재 여부 확인
                roomRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("DetailFragment", "Successfully checked chat room existence");
                        DataSnapshot snapshot = task.getResult();
                        if (!snapshot.exists()) {
                            Log.d("DetailFragment", "Creating new chat room");
                            
                            // ChatRoom 객체 생성
                            Map<String, Object> chatRoomData = new HashMap<>();
                            chatRoomData.put("roomId", chatRoomId);
                            chatRoomData.put("user1Id", currentUserId);
                            chatRoomData.put("user2Id", authorId);
                            chatRoomData.put("user1Name", finalCurrentUserName);
                            chatRoomData.put("user2Name", finalAuthorName);
                            chatRoomData.put("lastMessage", "");
                            chatRoomData.put("lastMessageTime", System.currentTimeMillis());
                            chatRoomData.put("createdAt", System.currentTimeMillis());

                            // unreadCount 초기화
                            Map<String, Integer> unreadCount = new HashMap<>();
                            unreadCount.put(currentUserId, 0);
                            unreadCount.put(authorId, 0);
                            chatRoomData.put("unreadCount", unreadCount);

                            // 채팅방 데이터 저장
                            roomRef.setValue(chatRoomData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("DetailFragment", "Chat room created successfully");
                                    startChatActivity(chatRoomId, authorId, finalAuthorName);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DetailFragment", "Error creating chat room", e);
                                    showError("채팅방을 생성하는 중 오류가 발생했습니다: " + e.getMessage());
                                });
                        } else {
                            Log.d("DetailFragment", "Chat room already exists");
                            startChatActivity(chatRoomId, authorId, finalAuthorName);
                        }
                    } else {
                        Log.e("DetailFragment", "Error checking chat room", task.getException());
                        showError("채팅방 확인 중 오류가 발생했습니다: " + task.getException().getMessage());
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e("DetailFragment", "Error getting current user info", e);
                showError("사용자 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            });
    }

    private void startChatActivity(String chatRoomId, String otherUserId, String otherUserName) {
        try {
            Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
            intent.putExtra("chatRoomId", chatRoomId);
            intent.putExtra("otherUserId", otherUserId);
            intent.putExtra("otherUserName", otherUserName);
            Log.d("DetailFragment", "Starting ChatRoomActivity with intent extras");
            startActivity(intent);
        } catch (Exception e) {
            Log.e("DetailFragment", "Error starting ChatRoomActivity", e);
            showError("채팅방을 열 수 없습니다: " + e.getMessage());
        }
    }

    private void loadComments() {
        FirebaseFirestore.getInstance()
                .collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)  // 오래된 순으로 정렬
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showError("댓글을 불러오는데 실패했습니다: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        ArrayList<Comment> comments = new ArrayList<>();
                        value.forEach(doc -> {
                            Comment comment = doc.toObject(Comment.class);
                            comment.setId(doc.getId());
                            comments.add(comment);
                        });
                        commentAdapter.setComments(comments);
                    }
                });
    }

    private void postComment(String content) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Comment comment = new Comment(postId, currentUid, content);

        FirebaseFirestore.getInstance()
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    etComment.setText("");
                    Toast.makeText(getContext(), "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "댓글 등록 실패: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show());
    }

    private void showPostDetails() {
        if (currentPost != null) {
            tvTitle.setText(currentPost.getTitle());
            tvDate.setText(currentPost.getDate());
            tvTime.setText(currentPost.getStartTime() + " ~ " + currentPost.getEndTime());
            tvLocation.setText(currentPost.getLocation());
            tvPay.setText(currentPost.getPay() + "원/시간");
            tvContent.setText(currentPost.getContent());
            if (currentPost.getKeywords() != null) {
                tvKeywords.setText(TextUtils.join(", ", currentPost.getKeywords()));
            } else {
                tvKeywords.setText("(키워드 없음)");
            }
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    private void fetchPostDetail(String postId) {
        FirebaseFirestore.getInstance()
                .collection("job_posts")
                .document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentPost = doc.toObject(JobPost.class);
                        if (currentPost != null) {
                            currentPost.setId(doc.getId());
                            showPostDetails();
                            
                            // 채팅 버튼 표시 여부 설정
                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String postWriterId = currentPost.getWriterUid();
                            boolean isAuthor = postWriterId != null && postWriterId.equals(currentUserId);
                            btnChat.setVisibility(isAuthor ? View.GONE : View.VISIBLE);
                        } else {
                            showError("게시글 데이터를 불러올 수 없습니다.");
                        }
                    } else {
                        showError("해당 게시글이 존재하지 않습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("게시글을 불러오는데 실패했습니다: " + e.getMessage());
                });
    }
}