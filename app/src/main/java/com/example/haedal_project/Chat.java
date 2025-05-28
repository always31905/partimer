package com.example.haedal_project;

public class Chat {
    private String messageId;
    private String senderId;
    private String senderName;
    private String message;
    private long timestamp;

    // Firebase를 위한 기본 생성자
    public Chat() {
    }

    // 이전 버전과의 호환성을 위한 생성자
    public Chat(String senderId, String receiverId, String message) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // 새로운 생성자
    public Chat(String messageId, String senderId, String senderName, String message, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 