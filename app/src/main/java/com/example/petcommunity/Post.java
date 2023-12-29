package com.example.petcommunity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Post {
    private String postId;
    private String status;
    private String imageUrl;
    private String savedUsername;
    private long commentsCount; // Số lượt bình luận
    private List<String> comments; // Danh sách bình luận
    private Map<String, Boolean> likesMap;


    public Post() {
        // Hàm khởi tạo mặc định, cần cho Firebase
    }

    public Post(String postId, String status, String imageUrl, String savedUsername) {
        this.postId = postId;
        this.status = status;
        this.imageUrl = imageUrl;
        this.savedUsername = savedUsername;
        this.commentsCount = 0;
        this.comments = new ArrayList<>();
    }

    public String getSavedUsername() {
        return savedUsername;
    }

    public void setSavedUsername(String savedUsername) {
        this.savedUsername = savedUsername;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    public long getCommentCount() {
        return commentsCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentsCount = commentCount;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
    public Map<String, Boolean> getLikesMap() {
        return likesMap;
    }


    public void setLikesMap(Map<String, Boolean> likesMap) {
        this.likesMap = likesMap;
    }
}
