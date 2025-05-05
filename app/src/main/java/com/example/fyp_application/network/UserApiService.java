package com.example.fyp_application.network;

import com.example.fyp_application.model.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApiService {
    @POST("User/signup")
    Call<Integer> signUp(@Body UserDto user);
}
