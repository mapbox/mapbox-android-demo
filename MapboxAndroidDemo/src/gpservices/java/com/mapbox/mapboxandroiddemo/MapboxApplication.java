package com.mapbox.mapboxandroiddemo;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class MapboxApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeFirebaseApp();
    setUpPicasso();
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
}
