package com.example.haedal_project;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class DetailFragment extends Fragment {
    private TextView tvTitle, tvDate, tvTime, tvLocation, tvPay, tvContent, tvKeywords;
    private ProgressBar progressBar;
    private EditText etComment;
    private Button btnPostComment;
    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private String postId;

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
        // progressBar = v.findViewById(R.id.progressBar);->로딩중바
        
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
            } else {
                showError("게시글 ID가 유효하지 않습니다.");
            }
        } else {
            showError("게시글 정보를 찾을 수 없습니다.");
        }

        return v;
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


    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }
    /**
     * Firestore에서 단일 문서를 조회하여 UI에 표시
     */
    private void fetchPostDetail(String postId) {
        FirebaseFirestore.getInstance()
                .collection("job_posts")
                .document(postId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        JobPost post = doc.toObject(JobPost.class);
                        if (post != null) {
                            tvTitle.setText(post.getTitle());
                            tvDate.setText(post.getDate());
                            tvTime.setText(post.getStartTime() + " ~ " + post.getEndTime());
                            tvLocation.setText(post.getLocation());
                            tvPay.setText(post.getPay() + "원/시간");
                            tvContent.setText(post.getContent());
                            if (post.getKeywords() != null) {
                                tvKeywords.setText(TextUtils.join(", ", post.getKeywords()));
                            } else {
                                tvKeywords.setText("(키워드 없음)");
                            }
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