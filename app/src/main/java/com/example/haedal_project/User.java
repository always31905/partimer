package com.example.haedal_project;

import com.google.firebase.firestore.DocumentId;

public class User {
    @DocumentId
    private String uid;
    private String nickname;

    public User() {} // Firestore 필수 생성자

    public User(String uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
    }

    public String getUid() { return uid; }
    public String getNickname() { return nickname; }

    public void setUid(String uid) { this.uid = uid; }
    public void setNickname(String nickname) { this.nickname = nickname; }
} 