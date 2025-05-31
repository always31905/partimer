package com.example.haedal_project;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Comment {
    @DocumentId
    private String id;
    private String postId;
    private String writerUid;
    private String content;
    @ServerTimestamp
    private Date createdAt;

    public Comment() {} // Firestore를 위한 빈 생성자

    public Comment(String postId, String writerUid, String content) {
        this.postId = postId;
        this.writerUid = writerUid;
        this.content = content;
    }

    // Getters
    public String getId() { return id; }
    public String getPostId() { return postId; }
    public String getWriterUid() { return writerUid; }
    public String getContent() { return content; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setWriterUid(String writerUid) { this.writerUid = writerUid; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
} 