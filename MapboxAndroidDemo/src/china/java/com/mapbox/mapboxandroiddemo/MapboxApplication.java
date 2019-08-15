package com.mapbox.mapboxandroiddemo;

import android.app.Application;

import com.mapbox.mapboxandroiddemo.utils.TileLoadingInterceptor;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

public class MapboxApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    setUpPicasso();
    Mapbox.getInstance(this, getString(R.string.access_token));
//    Mapbox.getTelemetry().setDebugLoggingEnabled(true);
    setUpTileLoadingMeasurement();
  }

  private void setUpPicasso() {
    Picasso.Builder builder = new Picasso.Builder(this);
    builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
    Picasso built = builder.build();
    built.setLoggingEnabled(true);
    Picasso.setSingletonInstance(built);
  }

  private void setUpTileLoadingMeasurement() {
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new TileLoadingInterceptor())
            .build();
    HttpRequestUtil.setOkHttpClient(okHttpClient);
  }
}
