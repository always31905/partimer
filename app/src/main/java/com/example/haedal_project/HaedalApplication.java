package com.example.haedal_project;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class HaedalApplication extends Application {
    private static final String TAG = "HaedalApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }
    }
} 