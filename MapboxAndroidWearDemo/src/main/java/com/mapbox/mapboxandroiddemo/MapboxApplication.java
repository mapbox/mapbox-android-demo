package com.mapbox.mapboxandroiddemo;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.mapbox.mapboxsdk.Mapbox;

public class MapboxApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    if (!BuildConfig.DEBUG) {
      FirebaseApp.initializeApp(this);
    }
    Mapbox.getInstance(this, getString(R.string.access_token));
    if (BuildConfig.DEBUG) {
      if (Mapbox.getTelemetry() == null) {
        throw new RuntimeException("Mapbox.getTelemetry() == null in debug config");
      } else {
        Mapbox.getTelemetry().setDebugLoggingEnabled(true);
      }
    }
  }
}
