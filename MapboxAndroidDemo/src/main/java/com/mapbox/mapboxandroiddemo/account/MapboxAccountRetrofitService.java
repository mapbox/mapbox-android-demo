package com.mapbox.mapboxandroiddemo.account;

import com.mapbox.mapboxandroiddemo.model.usermodel.UserResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Service for making call to Mapbox API for account information
 */

interface MapboxAccountRetrofitService {

  @GET("User/{username}")
  Call<UserResponse> getUserAccount(@Path("username") String userName, @Query("access_token") String accessToken);

}