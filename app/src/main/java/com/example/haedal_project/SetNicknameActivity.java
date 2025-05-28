package com.example.haedal_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetNicknameActivity extends AppCompatActivity {
    private static final String TAG = "SetNicknameActivity";
    private EditText etNickname;
    private Button btnConfirm;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_nickname);

        etNickname = findViewById(R.id.etNickname);
        btnConfirm = findViewById(R.id.btnConfirm);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnConfirm.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            if (nickname.isEmpty()) {
                etNickname.setError("닉네임을 입력해주세요");
                return;
            }
            checkNicknameAndUpdate(nickname);
        });
    }

    private void checkNicknameAndUpdate(String nickname) {
        btnConfirm.setEnabled(false); // 중복 클릭 방지
        String currentUid = auth.getCurrentUser().getUid();
        Log.d(TAG, "Checking nickname: " + nickname + " for user: " + currentUid);

        // 먼저 현재 사용자의 닉네임인지 확인
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentNickname = documentSnapshot.getString("nickname");
                        Log.d(TAG, "Current user's nickname: " + currentNickname);
                        if (nickname.equals(currentNickname)) {
                            Log.d(TAG, "Attempting to set the same nickname");
                            Toast.makeText(this, "현재 사용 중인 닉네임입니다", Toast.LENGTH_SHORT).show();
                            btnConfirm.setEnabled(true);
                            return;
                        }
                    }

                    // 다른 사용자의 닉네임과 중복 확인
                    db.collection("users")
                            .whereEqualTo("nickname", nickname)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    Log.d(TAG, "Nickname is already in use by another user");
                                    etNickname.setError("이미 사용 중인 닉네임입니다");
                                    Toast.makeText(this, "다른 닉네임을 선택해주세요", Toast.LENGTH_SHORT).show();
                                    btnConfirm.setEnabled(true);
                                } else {
                                    Log.d(TAG, "Nickname is available");
                                    updateNickname(nickname);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error checking nickname: " + e.getMessage(), e);
                                String errorMessage = e.getMessage();
                                if (errorMessage != null && (
                                        errorMessage.contains("failed-precondition") ||
                                        errorMessage.contains("FAILED_PRECONDITION") ||
                                        errorMessage.contains("Missing or insufficient permissions"))) {
                                    Log.d(TAG, "Proceeding with nickname update despite index/permission error");
                                    updateNickname(nickname);
                                } else {
                                    Toast.makeText(this, "닉네임 확인 중 오류가 발생했습니다. 다시 시도해주세요.",
                                            Toast.LENGTH_SHORT).show();
                                    btnConfirm.setEnabled(true);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking current user's nickname: " + e.getMessage(), e);
                    Toast.makeText(this, "사용자 정보 확인 실패. 다시 시도해주세요.",
                            Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                });
    }

    private void updateNickname(String nickname) {
        String uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "Updating nickname for user: " + uid + " to: " + nickname);
        
        User user = new User(uid, nickname);

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Nickname updated successfully");
                    Toast.makeText(this, "닉네임이 설정되었습니다", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating nickname: " + e.getMessage(), e);
                    Toast.makeText(this, "닉네임 설정 실패. 다시 시도해주세요.",
                            Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                });
    }
} 