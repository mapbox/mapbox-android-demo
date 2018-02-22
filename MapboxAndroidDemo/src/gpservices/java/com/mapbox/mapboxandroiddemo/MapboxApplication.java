package com.mapbox.mapboxandroiddemo;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.mapbox.android.auth.UrlRoutingManager;
import com.squareup.picasso.Picasso;

public class MapboxApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeFirebaseApp();
    setUpPicasso();
    setUpUrlRoutingManager();
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
    builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
    Picasso built = builder.build();
    built.setLoggingEnabled(true);
    Picasso.setSingletonInstance(built);
  }

  private void setUpUrlRoutingManager() {
    UrlRoutingManager.getInstance(
      "mapbox-android-dev-preview",
      "7bb34a0cf68455d33ec0d994af2330a3f60ee636");
  }
}
