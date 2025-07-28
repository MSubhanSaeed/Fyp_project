package com.example.fyp_application.network;

import com.example.fyp_application.dto.AddFriendEntity;
import com.example.fyp_application.dto.IndividualLiveLocationEntity;
import com.example.fyp_application.dto.LoginUserEntity;
import com.example.fyp_application.dto.MemberEntity;
import com.example.fyp_application.dto.UserEntity;
import com.example.fyp_application.dto.UserGeofence;
import com.example.fyp_application.dto.UserLiveLocationEntity;
import com.example.fyp_application.model.Notification;
import com.example.fyp_application.request.GroupRequest;
import com.example.fyp_application.response.GroupResponse;
import com.example.fyp_application.response.SignupResponse;
import com.example.fyp_application.model.UserDto;
import com.example.fyp_application.response.TripResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApiService {

    @POST("User/Signup")
    Call<SignupResponse> signUp(@Body UserDto user);

    @POST("User/Login")
    Call<UserEntity> loginUser(@Body LoginUserEntity loginUser);

    @GET("GroupMember/GetByUserId/{id}")
    Call<List<GroupResponse>> getGroups(@Path("id") int userId);

    @GET("GroupMember/GetByGroupId/{id}")
    Call<List<UserEntity>> getUsersInGroup(@Path("id") int groupId);

    @DELETE("GeoFence/Delete/{id}")
    Call<Void> DeleteGeofence(@Path("id") int GeofenceId);

    @POST("Group/CreateUserGroup")
    Call<Void> createGroup(@Body GroupRequest request);

    @GET("User/GetById/{id}")
    Call<UserEntity> getUserById(@Path("id") int userId);

    @PUT("User/UpdateById/{id}")
    Call<Boolean> updateUserById(@Path("id") int userId, @Body UserDto user);

    @DELETE("User/DeleteById/{id}")
    Call<Boolean> deleteUserById(@Path("id") int userId);

    @GET("UserFriend/GetGroupFriendsLiveLocation/{id}")
    Call<List<UserLiveLocationEntity>> getGroupFriendsLiveLocation(@Path("id") int groupId);

    @GET("UserFriend/GetFriendsDetailByUserId/{id}")
    Call<List<IndividualLiveLocationEntity>> getFriendsdetails(@Path("id") int userId);

    @GET("UserFriend/GetUserFriendsLiveLocation/{id}")
    Call<List<IndividualLiveLocationEntity>> GetUserFriendsLiveLocation(@Path("id") int userId);

    @POST("GeoFence/AddData")
    Call<Void> addGeofence(@Body UserGeofence geofence);

    @GET("GeoFence/GetAllByUser/{userId}")
    Call<List<UserGeofence>> getGeofencesByUser(@Path("userId") int userId);


    @POST("UserFriend/SendRequestUsingEmail")
    Call<String> addFriend(@Body AddFriendEntity request);

    @GET("UserTripLog/GetByUserId/{userId}")
    Call<List<TripResponse>> getTripsByUserId(@Path("userId") int userId);

    @GET("Notification/GetByUserId/{userId}")
    Call<List<Notification>> getNotificationsByUserId(@Path("userId") int userId);

    @PUT("Notification/UpdateNotification")
    Call<Void> markNotificationAsRead(@Body Map<String, Object> request);

    @DELETE("Notification/DeleteNotification/{notificationId}")
    Call<Void> deleteNotification(@Path("notificationId") int notificationId);

    @POST("Notification/AddNotification/{userId")
    Call<Notification> AddNotification(@Path("userId") Notification notification);

    @POST("GroupMember/AddGroupMember")
    Call<Void> addGroupMember(@Body MemberEntity Groupmem);


}