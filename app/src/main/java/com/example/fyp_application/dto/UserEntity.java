package com.example.fyp_application.dto;
import com.google.gson.annotations.SerializedName;

public class UserEntity {


    public static int uId;
    @SerializedName("User_Id")
    public  int userId;

    @SerializedName("Name")
    private String name;

    @SerializedName("Email")
    private String email;

    @SerializedName("Password")
    private String password;


    public UserEntity(int userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        uId=userId;
    }

    // Getters
    public  int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
