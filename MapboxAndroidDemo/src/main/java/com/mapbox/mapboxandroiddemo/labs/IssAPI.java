package com.mapbox.mapboxandroiddemo.labs;

import com.mapbox.mapboxandroiddemo.model.IssModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IssAPI {
    @GET("iss-now")
    Call<IssModel> loadLocation();
}