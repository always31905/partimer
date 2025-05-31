package com.example.haedal_project;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class JobPost {
    @DocumentId
    private String id;

    private String writerUid;
    private String writerName;
    private String title;
    private String content;
    private String location;
    private String date;       // YYYY-MM-DD
    private String startTime;  // HH:MM
    private String endTime;    // HH:MM
    private int pay;
    private String status;
    private List<String> keywords;

    @ServerTimestamp
    private Date createdAt;

    public JobPost() {}

    // === Getter ===
    public String getId() { return id; }
    public String getWriterUid() { return writerUid; }
    public String getWriterName() { return writerName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getPay() { return pay; }
    public String getStatus() { return status; }
    public List<String> getKeywords() { return keywords; }
    public Date getCreatedAt() { return createdAt; }

    // === Setter ===
    public void setWriterUid(String writerUid) { this.writerUid = writerUid; }
    public void setWriterName(String writerName) { this.writerName = writerName; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setPay(int pay) { this.pay = pay; }
    public void setStatus(String status) { this.status = status; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public void setId(String id) { this.id = id; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
