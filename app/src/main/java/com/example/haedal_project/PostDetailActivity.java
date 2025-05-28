package com.example.haedal_project;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";
    private TextView tvTitle, tvContent, tvLocation, tvDate, tvTime, tvPay, tvKeywords;
    private EditText etTitle, etContent, etStartTime, etEndTime, etPay, etKeywords;
    private Spinner spLocation;
    private Button btnDate, btnEdit, btnDelete, btnSave, btnCancel;
    private View viewNormal, viewEdit;
    private String postId;
    private JobPost currentPost;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("게시글 상세");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 뷰 초기화
        viewNormal = findViewById(R.id.layoutNormal);
        viewEdit = findViewById(R.id.layoutEdit);
        
        // 일반 보기 모드 뷰
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        tvLocation = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvPay = findViewById(R.id.tvPay);
        tvKeywords = findViewById(R.id.tvKeywords);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        // 수정 모드 뷰
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        spLocation = findViewById(R.id.spLocation);
        btnDate = findViewById(R.id.btnDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etPay = findViewById(R.id.etPay);
        etKeywords = findViewById(R.id.etKeywords);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // 스피너 설정 - arrays.xml의 daegu_gu 배열 사용
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.daegu_gu, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocation.setAdapter(adapter);

        // 버튼 클릭 리스너 설정
        btnEdit.setOnClickListener(v -> toggleEditMode(true));
        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> toggleEditMode(false));
        btnDate.setOnClickListener(v -> showDatePicker());

        // 게시글 정보 가져오기
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "게시글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadPost();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadPost() {
        FirebaseFirestore.getInstance()
                .collection("job_posts")
                .document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    currentPost = doc.toObject(JobPost.class);
                    if (currentPost != null) {
                        currentPost.setId(doc.getId());  // ID 명시적 설정
                        showPostDetails();
                    } else {
                        Toast.makeText(this, "게시글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading post", e);
                    Toast.makeText(this, "게시글을 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showPostDetails() {
        try {
            // 일반 보기 모드에 데이터 표시
            tvTitle.setText(currentPost.getTitle());
            tvContent.setText(currentPost.getContent());
            tvLocation.setText(currentPost.getLocation());
            tvDate.setText(currentPost.getDate());
            tvTime.setText(currentPost.getStartTime() + " ~ " + currentPost.getEndTime());
            tvPay.setText(currentPost.getPay() + "원/시간");
            tvKeywords.setText(currentPost.getKeywords() != null ? 
                    String.join(", ", currentPost.getKeywords()) : "");

            // 수정 모드 필드에도 데이터 설정
            etTitle.setText(currentPost.getTitle());
            etContent.setText(currentPost.getContent());
            
            // 스피너 위치 설정
            ArrayAdapter adapter = (ArrayAdapter) spLocation.getAdapter();
            int locationPosition = adapter.getPosition(currentPost.getLocation());
            if (locationPosition >= 0) {
                spLocation.setSelection(locationPosition);
            }
            
            btnDate.setText(currentPost.getDate());
            etStartTime.setText(currentPost.getStartTime());
            etEndTime.setText(currentPost.getEndTime());
            etPay.setText(String.valueOf(currentPost.getPay()));
            etKeywords.setText(currentPost.getKeywords() != null ? 
                    String.join(", ", currentPost.getKeywords()) : "");
        } catch (Exception e) {
            Log.e(TAG, "Error showing post details", e);
            Toast.makeText(this, "게시글 표시 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void toggleEditMode(boolean editing) {
        isEditMode = editing;
        viewNormal.setVisibility(editing ? View.GONE : View.VISIBLE);
        viewEdit.setVisibility(editing ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            btnDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveChanges() {
        try {
            // 입력값 검증
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String location = spLocation.getSelectedItem().toString();
            String date = btnDate.getText().toString();
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();
            String payStr = etPay.getText().toString().trim();
            String keywordsStr = etKeywords.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty() || date.isEmpty() 
                    || startTime.isEmpty() || endTime.isEmpty() || payStr.isEmpty()) {
                Toast.makeText(this, "모든 필수 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            int pay;
            try {
                pay = Integer.parseInt(payStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "시급은 숫자로 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> keywords = Arrays.asList(keywordsStr.split("\\s*,\\s*"));

            // Firestore 업데이트
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", title);
            updates.put("content", content);
            updates.put("location", location);
            updates.put("date", date);
            updates.put("startTime", startTime);
            updates.put("endTime", endTime);
            updates.put("pay", pay);
            updates.put("keywords", keywords);

            FirebaseFirestore.getInstance()
                    .collection("job_posts")
                    .document(postId)
                    .update(updates)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "수정이 완료되었습니다", Toast.LENGTH_SHORT).show();
                        loadPost();  // 화면 새로고침
                        toggleEditMode(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating post", e);
                        Toast.makeText(this, "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveChanges", e);
            Toast.makeText(this, "수정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("게시글 삭제")
                .setMessage("정말 이 게시글을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deletePost())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deletePost() {
        FirebaseFirestore.getInstance()
                .collection("job_posts")
                .document(postId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "게시글이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting post", e);
                    Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
} 