package com.example.fyp_application.model;

public class GroupRequest {
    private String groupName;
    private String groupCode;
    private boolean isPrivate;
    private int userId;
    private String createdAt;

    public GroupRequest(String groupName, String groupCode, boolean isPrivate, int userId, String createdAt) {
        this.groupName = groupName;
        this.groupCode = groupCode;
        this.isPrivate = isPrivate;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
