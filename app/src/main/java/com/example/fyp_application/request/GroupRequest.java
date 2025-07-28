package com.example.fyp_application.request;

public class GroupRequest {
    private String GroupName;           // Changed from groupName to name
    private String Group_Code;
    private boolean isPrivate;
    private int Group_Id;

    public GroupRequest(String name, String groupCode, boolean isPrivate, int userId) {
        this.GroupName = name;
        this.Group_Code = groupCode;
        this.isPrivate = isPrivate;
        this.Group_Id = userId;
    }

    // Getters and setters
    public String getName() {
        return GroupName;
    }

    public void setName(String name) {
        this.GroupName = name;
    }

    public String getGroupCode() {
        return Group_Code;
    }

    public void setGroupCode(String groupCode) {
        this.Group_Code = groupCode;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public int getUserId() {
        return Group_Id;
    }

    public void setUserId(int userId) {
        this.Group_Id = userId;
    }

}
