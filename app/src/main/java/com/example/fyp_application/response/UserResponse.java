package com.example.fyp_application.response;

public class UserResponse {
    private int User_Id;
    private String Name;

    public UserResponse(int user_Id, String name) {
        this.User_Id = user_Id;
        this.Name = name;
    }

    public int getUser_Id() {
        return User_Id;
    }

    public void setUser_Id(int user_Id) {
        this.User_Id = user_Id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }
}
