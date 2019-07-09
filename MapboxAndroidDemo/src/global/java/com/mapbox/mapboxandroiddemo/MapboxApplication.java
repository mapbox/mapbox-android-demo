package com.mapbox.mapboxandroiddemo;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.mapbox.mapboxandroiddemo.utils.TileLoadingInterceptor;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import androidx.multidex.MultiDexApplication;
import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;

public class MapboxApplication extends MultiDexApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    setUpPicasso();
    Mapbox.getInstance(this, getString(R.string.access_token));
    Mapbox.getTelemetry().setDebugLoggingEnabled(true);
    setUpTileLoadingMeasurement();
    setUpCrashlytics();
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

  private void setUpCrashlytics() {
    Fabric.with(this, new Crashlytics.Builder()
        .core(new CrashlyticsCore.Builder()
            .disabled(BuildConfig.DEBUG)
            .build())
        .build());
  }
}
