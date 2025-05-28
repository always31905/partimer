package com.example.haedal_project;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final ConcurrentHashMap<String, String> nicknameCache = new ConcurrentHashMap<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvContent;
        private final TextView tvWriterAndDate;
        private final ImageButton btnDelete;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvWriterAndDate = itemView.findViewById(R.id.tvCommentWriterAndDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }

        void bind(Comment comment) {
            tvContent.setText(comment.getContent());
            String dateStr = comment.getCreatedAt() != null ? 
                    dateFormat.format(comment.getCreatedAt()) : "";
            
            // 현재 로그인한 사용자가 댓글 작성자인지 확인
            if (auth.getCurrentUser() != null && 
                auth.getCurrentUser().getUid().equals(comment.getWriterUid())) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> showDeleteDialog(comment));
            } else {
                btnDelete.setVisibility(View.GONE);
            }
            
            // 캐시된 닉네임이 있는지 확인
            String cachedNickname = nicknameCache.get(comment.getWriterUid());
            if (cachedNickname != null) {
                tvWriterAndDate.setText(cachedNickname + " • " + dateStr);
            } else {
                // 닉네임을 가져오는 동안 임시로 UID 표시
                tvWriterAndDate.setText(comment.getWriterUid() + " • " + dateStr);
                
                // Firestore에서 닉네임 가져오기
                db.collection("users")
                        .document(comment.getWriterUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String nickname = documentSnapshot.getString("nickname");
                                if (nickname != null && !nickname.isEmpty()) {
                                    // 캐시에 저장
                                    nicknameCache.put(comment.getWriterUid(), nickname);
                                    // UI 업데이트
                                    tvWriterAndDate.setText(nickname + " • " + dateStr);
                                }
                            }
                        });
            }
        }

        private void showDeleteDialog(Comment comment) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("댓글 삭제")
                    .setMessage("이 댓글을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> deleteComment(comment))
                    .setNegativeButton("취소", null)
                    .show();
        }

        private void deleteComment(Comment comment) {
            db.collection("comments")
                    .document(comment.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // 삭제 성공 시 목록에서도 제거
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            comments.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(itemView.getContext(), 
                                    "댓글이 삭제되었습니다", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(itemView.getContext(),
                                "댓글 삭제 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
        }
    }
} 