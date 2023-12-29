package com.example.petcommunity;

public class Message {
    private String content;
    private long timestamp;
    private String senderId;
    private String recipientId;

    // Required default constructor for Firebase
    public Message() {
    }

    public Message(String content, long timestamp, String senderId, String recipientId) {
        this.content = content;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }
}

