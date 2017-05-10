package com.mapbox.mapboxandroiddemo;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Building Retrofit client for eventually making calls to Mapbox API for account information
 */

public class MapboxAccountClient {

  public static final String BASE_URL = "https://api.mapbox.com/api/";
  private static Retrofit retrofit = null;

  public static Retrofit getClient() {
    if (retrofit == null) {
      retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    }
    return retrofit;
  }
}
