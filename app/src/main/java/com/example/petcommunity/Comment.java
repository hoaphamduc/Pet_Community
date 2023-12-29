package com.example.petcommunity;

public class Comment {
    private String userId; // Để lưu trữ username hoặc ID của người comment
    private String content; // Nội dung của comment

    public Comment() {
        // Empty constructor required by Firebase
    }

    public Comment(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
