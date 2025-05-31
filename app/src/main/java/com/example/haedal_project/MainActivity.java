package com.example.haedal_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private SignInButton googleLoginButton;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_main);

            // Firebase 초기화 확인
            if (!FirebaseApp.getApps(this).isEmpty()) {
                initializeFirebase();
            } else {
                Log.e(TAG, "Firebase is not initialized");
                Toast.makeText(this, "Firebase 초기화 오류", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in onCreate", e);
            Toast.makeText(this, "앱 초기화 중 치명적인 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeFirebase() {
        try {
            // FirebaseAuth 초기화
            mAuth = FirebaseAuth.getInstance();
            Log.d(TAG, "Firebase Auth initialized");

            // 현재 로그인된 사용자 확인
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "User already signed in: " + currentUser.getUid());
                checkNicknameAndProceed();
                return;
            }

            // Google 로그인 초기화
            initializeGoogleSignIn();

            // UI 초기화
            initializeUI();

            isInitialized = true;
            Log.d(TAG, "MainActivity initialization completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeFirebase", e);
            Toast.makeText(this, "Firebase 초기화 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeGoogleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "Google Sign In Client initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Sign In", e);
            Toast.makeText(this, "Google 로그인 초기화 실패", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeUI() {
        try {
            googleLoginButton = findViewById(R.id.google_login_button);

            if (googleLoginButton == null) {
                throw new IllegalStateException("Google 로그인 버튼을 찾을 수 없습니다");
            }

            // 구글 로그인 버튼 설정
            googleLoginButton.setSize(SignInButton.SIZE_WIDE);

            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeUI", e);
            Toast.makeText(this, "UI 초기화 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupClickListeners() {
        // 구글 로그인 버튼 클릭 리스너
        googleLoginButton.setOnClickListener(v -> {
            if (!isInitialized) {
                Log.e(TAG, "Trying to login before initialization");
                Toast.makeText(this, "앱이 아직 초기화되지 않았습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                Log.d(TAG, "Starting Google sign in");
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            } catch (Exception e) {
                Log.e(TAG, "Error starting Google sign in", e);
                Toast.makeText(this, "구글 로그인을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == RC_SIGN_IN) {
                Log.d(TAG, "Google sign in result received");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "Google sign in successful, proceeding with Firebase auth");
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Log.e(TAG, "Google sign in failed", e);
                    Toast.makeText(this, "구글 로그인 실패: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult", e);
            Toast.makeText(this, "구글 로그인 처리 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        checkNicknameAndProceed();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "인증 실패",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkNicknameAndProceed() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Log.e(TAG, "Current user is null in checkNicknameAndProceed");
                Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            String uid = user.getUid();
            Log.d(TAG, "Checking nickname for user: " + uid);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Log.d(TAG, "User has nickname, proceeding to HomeActivity");
                            startActivity(new Intent(this, HomeActivity.class));
                        } else {
                            Log.d(TAG, "User needs to set nickname, proceeding to SetNicknameActivity");
                            startActivity(new Intent(this, SetNicknameActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking nickname", e);
                        Toast.makeText(this, "사용자 정보 확인 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in checkNicknameAndProceed", e);
            Toast.makeText(this, "사용자 정보 확인 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }
}
