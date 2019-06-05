package com.mapbox.mapboxandroiddemo;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.mapbox.mapboxandroiddemo.utils.TileLoadingInterceptor;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

public class MapboxApplication extends MultiDexApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeFirebaseApp();
    setUpPicasso();
    Mapbox.getInstance(this, getString(R.string.access_token));
    Mapbox.getTelemetry().setDebugLoggingEnabled(true);
    setUpTileLoadingMeasurement();
  }

  private void initializeFirebaseApp() {
    FirebaseApp.initializeApp(this, new FirebaseOptions.Builder()
      .setApiKey(getString(R.string.firebase_api_key))
      .setApplicationId(getString(R.string.firebase_app_id))
      .build()
    );
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
