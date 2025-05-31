package com.example.haedal_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private RecyclerView rvMyPosts;
    private JobPostAdapter adapter;
    private List<JobPost> myPosts;
    private TextView tvMyPostsCount;
    private TextView tvNickname;
    private FirebaseAuth mAuth;
    private ListenerRegistration postsListener;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Firebase Auth 초기화
        mAuth = FirebaseAuth.getInstance();

        // 뷰 초기화
        initializeViews(view);
        setupRecyclerView();

        // 사용자가 로그인되어 있는지 확인
        if (isUserLoggedIn()) {
            // 사용자 정보 로드
            loadUserInfo();
            // 내가 쓴 글 로드
            loadMyPosts();
        } else {
            // 로그인되어 있지 않은 경우 처리
            handleNotLoggedIn();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 리스너 해제
        if (postsListener != null) {
            postsListener.remove();
            postsListener = null;
        }
    }

    private void initializeViews(View view) {
        rvMyPosts = view.findViewById(R.id.rvMyPosts);
        tvMyPostsCount = view.findViewById(R.id.tvMyPostsCount);
        tvNickname = view.findViewById(R.id.tvNickname);
    }

    private void setupRecyclerView() {
        myPosts = new ArrayList<>();
        adapter = new JobPostAdapter();
        adapter.setItems(myPosts);
        adapter.setOnItemClickListener(post -> {
            if (post != null && post.getId() != null && getActivity() != null) {
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                intent.putExtra("postId", post.getId());
                startActivity(intent);
            }
        });
        rvMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyPosts.setAdapter(adapter);
    }

    private boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private void handleNotLoggedIn() {
        if (getContext() != null) {
            tvNickname.setText("로그인이 필요합니다");
            tvMyPostsCount.setText("내가 쓴 글 0개");
            myPosts.clear();
            adapter.setItems(myPosts);
        }
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            handleNotLoggedIn();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (getContext() == null) return;

                    User user = doc.toObject(User.class);
                    if (user != null && user.getNickname() != null) {
                        tvNickname.setText(user.getNickname());
                    } else {
                        tvNickname.setText("닉네임 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    tvNickname.setText("사용자 정보 로드 실패");
                });
    }

    private void loadMyPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            handleNotLoggedIn();
            return;
        }

        // 기존 리스너가 있다면 제거
        if (postsListener != null) {
            postsListener.remove();
        }

        // 새로운 리스너 등록
        Query query = FirebaseFirestore.getInstance()
                .collection("job_posts")
                .whereEqualTo("writerUid", currentUser.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING);

        postsListener = query.addSnapshotListener((value, error) -> {
            if (getContext() == null) return;

            if (error != null) {
                return;
            }

            if (value != null) {
                myPosts.clear();
                boolean hasValidPosts = false;

                for (DocumentSnapshot doc : value.getDocuments()) {
                    JobPost post = doc.toObject(JobPost.class);
                    if (post != null) {
                        post.setId(doc.getId());
                        myPosts.add(post);
                        hasValidPosts = true;
                    }
                }

                // UI 업데이트
                adapter.setItems(myPosts);
                tvMyPostsCount.setText("내가 쓴 글 " + myPosts.size() + "개");

                // 게시물이 없는 경우에만 메시지 표시
                if (!hasValidPosts && myPosts.isEmpty()) {
                    tvMyPostsCount.setText("작성한 글이 없습니다");
                }
            }
        });
    }
}
