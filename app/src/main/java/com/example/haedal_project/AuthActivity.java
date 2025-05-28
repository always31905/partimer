//로그인 화면
package com.example.haedal_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.haedal_project.HomeActivity;
import com.example.haedal_project.R;
import com.example.haedal_project.SignupActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleClient;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();

        // 1) GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            startActivityForResult(googleClient.getSignInIntent(), RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(account -> {
                        AuthCredential cred = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        mAuth.signInWithCredential(cred).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) checkProfileExists();
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show());
        }
    }

    private void checkProfileExists() {
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        startActivity(new Intent(this, HomeActivity.class));
                    } else {
                        startActivity(new Intent(this, SignupActivity.class));
                    }
                    finish();
                });
    }
}
