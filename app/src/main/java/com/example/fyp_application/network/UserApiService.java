package com.example.fyp_application.network;

import com.example.fyp_application.dto.LoginUserEntity;
import com.example.fyp_application.dto.UserEntity;
import com.example.fyp_application.model.GroupRequest;
import com.example.fyp_application.model.GroupResponse;
import com.example.fyp_application.model.SignupResponse;
import com.example.fyp_application.model.UserDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserApiService {

@POST("User/Signup")
Call<SignupResponse> signUp(@Body UserDto user);


    @POST("User/Login")
    Call<UserEntity> loginUser(@Body LoginUserEntity loginUser);

    @GET("GroupMember/GetByUserId/{id}")
    Call<List<GroupResponse>> getGroups(@Path("id") int userId);

    @GET("GroupMember/GetByGroupId/{id}")
    Call<List<UserEntity>> getUserInGroup(@Path("id") int userId);

    @POST("Group/CreateUserGroup") // Replace with actual endpoint if needed
    Call<Void> createGroup(@Body GroupRequest request);

}
