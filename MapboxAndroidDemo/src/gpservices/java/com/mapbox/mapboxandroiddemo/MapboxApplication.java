package com.mapbox.mapboxandroiddemo;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.mapbox.mapboxsdk.Mapbox;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class MapboxApplication extends MultiDexApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeFirebaseApp();
    setUpPicasso();
    if (BuildConfig.FLAVOR.matches("gpsservices")) {
      Mapbox.getInstance(this, getString(R.string.access_token));
    } else if (BuildConfig.FLAVOR.matches("nogpsservices")) {
      //You will need a special Mapbox China access token if you want to view any of our China
      // examples and use China map styles. Please fill out the form at https://www.mapbox.cn/contact
      // to start the process of receiving a special China access token. Thank you!
      Mapbox.getInstance(this, getString(R.string.china_access_token));
    }
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
