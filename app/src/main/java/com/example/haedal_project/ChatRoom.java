package com.example.haedal_project;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    private String roomId;
    private String user1Id;
    private String user2Id;
    private String user1Name;
    private String user2Name;
    private String lastMessage;
    private long lastMessageTime;
    private long createdAt;
    private Map<String, Integer> unreadCount;  // 각 사용자별 읽지 않은 메시지 수

    // Firebase를 위한 기본 생성자
    public ChatRoom() {
        unreadCount = new HashMap<>();
    }

    public ChatRoom(String user1Id, String user2Id, String user1Name, String user2Name) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.user1Name = user1Name;
        this.user2Name = user2Name;
        this.lastMessage = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount = new HashMap<>();
        this.unreadCount.put(user1Id, 0);
        this.unreadCount.put(user2Id, 0);
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getUser1Name() {
        return user1Name;
    }

    public void setUser1Name(String user1Name) {
        this.user1Name = user1Name;
    }

    public String getUser2Name() {
        return user2Name;
    }

    public void setUser2Name(String user2Name) {
        this.user2Name = user2Name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Helper method to get other user's name
    public String getOtherUserName(String currentUserId) {
        return currentUserId.equals(user1Id) ? user2Name : user1Name;
    }

    // Helper method to get other user's ID
    public String getOtherUserId(String currentUserId) {
        return currentUserId.equals(user1Id) ? user2Id : user1Id;
    }

    public void incrementUnreadCount(String userId) {
        int count = unreadCount.getOrDefault(userId, 0);
        unreadCount.put(userId, count + 1);
    }

    public void resetUnreadCount(String userId) {
        unreadCount.put(userId, 0);
    }

    public int getUnreadCount(String userId) {
        return unreadCount.getOrDefault(userId, 0);
    }

    public Map<String, Integer> getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Map<String, Integer> unreadCount) {
        this.unreadCount = unreadCount;
    }
} 