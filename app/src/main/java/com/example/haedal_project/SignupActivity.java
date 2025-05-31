//회원가입 화면
package com.example.haedal_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.haedal_project.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private EditText etNick;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override 
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_signup);
        etNick = findViewById(R.id.etNickname);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            String nick = etNick.getText().toString().trim();
            if (nick.isEmpty()) { 
                etNick.setError("닉네임을 입력해주세요"); 
                return; 
            }
            checkNicknameAndSignup(nick);
        });
    }

    private void checkNicknameAndSignup(String nickname) {
        // 먼저 현재 사용자의 UID로 기존 문서가 있는지 확인
        String uid = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Checking nickname: " + nickname + " for user: " + uid);

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "User document already exists, proceeding with signup");
                        completeSignup(nickname);
                    } else {
                        Log.d(TAG, "User document does not exist, checking nickname availability");
                        // 닉네임 중복 확인
                        db.collection("users")
                                .whereEqualTo("nickname", nickname)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        Log.d(TAG, "Nickname is already in use");
                                        etNick.setError("이미 사용 중인 닉네임입니다");
                                        Toast.makeText(this, "다른 닉네임을 선택해주세요", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "Nickname is available");
                                        completeSignup(nickname);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking nickname: " + e.getMessage(), e);
                                    String errorMessage = e.getMessage();
                                    if (errorMessage != null && (
                                            errorMessage.contains("failed-precondition") ||
                                            errorMessage.contains("FAILED_PRECONDITION") ||
                                            errorMessage.contains("Missing or insufficient permissions"))) {
                                        Log.d(TAG, "Proceeding with signup despite index/permission error");
                                        completeSignup(nickname);
                                    } else {
                                        Toast.makeText(this, "닉네임 확인 중 오류가 발생했습니다. 다시 시도해주세요.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user document: " + e.getMessage(), e);
                    Toast.makeText(this, "사용자 정보 확인 실패. 다시 시도해주세요.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void completeSignup(String nickname) {
        String uid = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Completing signup for user: " + uid + " with nickname: " + nickname);

        Map<String,Object> user = new HashMap<>();
        user.put("nickname", nickname);
        user.put("email", mAuth.getCurrentUser().getEmail());
        user.put("createdAt", FieldValue.serverTimestamp());
        
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(x -> {
                    Log.d(TAG, "Signup completed successfully");
                    Toast.makeText(this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error completing signup: " + e.getMessage(), e);
                    Toast.makeText(this, "회원가입 실패. 다시 시도해주세요.", 
                            Toast.LENGTH_SHORT).show();
                });
    }
}
