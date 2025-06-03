package com.example.fyp_application.response;

import java.util.List;

public class ApiResponse<T> {
    private boolean isSuccess;
    private String message;
    private List<T> data;

    public ApiResponse(boolean isSuccess, String message, List<T> data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.data = data;
    }

    // Getters
    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public List<T> getData() {
        return data;
    }

    // Setters
    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}