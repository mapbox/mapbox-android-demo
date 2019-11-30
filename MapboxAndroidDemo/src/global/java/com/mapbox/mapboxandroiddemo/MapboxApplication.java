package com.mapbox.mapboxandroiddemo;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.mapbox.mapboxandroiddemo.utils.TileLoadingInterceptor;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import androidx.multidex.MultiDexApplication;
import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;

public class MapboxApplication extends MultiDexApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    setUpPicasso();
    if (!BuildConfig.DEBUG) {
      FirebaseApp.initializeApp(this);
      setUpCrashlytics();
    }
    Mapbox.getInstance(this, getString(R.string.access_token));
    if (BuildConfig.DEBUG) {
      if (Mapbox.getTelemetry() == null) {
        throw new RuntimeException("Mapbox.getTelemetry() == null in debug config");
      } else {
        Mapbox.getTelemetry().setDebugLoggingEnabled(true);
      }
    }
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
        .connectTimeout(30, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .addNetworkInterceptor(new TileLoadingInterceptor())
        .build();
    HttpRequestUtil.setOkHttpClient(okHttpClient);
  }

  private void setUpCrashlytics() {
    Fabric.with(this, new Crashlytics());
  }
}
