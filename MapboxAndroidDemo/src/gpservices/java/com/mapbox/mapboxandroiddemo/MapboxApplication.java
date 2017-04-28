package com.mapbox.mapboxandroiddemo;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.mapbox.mapboxandroiddemo.analytics.AnalyticsTracker;

public class MapboxApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    FirebaseApp.initializeApp(this, new FirebaseOptions.Builder()
      .setApiKey(getString(R.string.firebase_api_key))
      .setApplicationId(getString(R.string.firebase_app_id))
      .build()
    );

    AnalyticsTracker.getInstance().trackEvent("Application onCreate()");

  }
}
