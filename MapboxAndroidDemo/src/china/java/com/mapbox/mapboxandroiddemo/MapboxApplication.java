package com.mapbox.mapboxandroiddemo;

import android.app.Application;

import com.mapbox.mapboxsdk.Mapbox;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class MapboxApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    setUpPicasso();
    Mapbox.getInstance(this, getString(R.string.access_token));
  }

  private void setUpPicasso() {
    Picasso.Builder builder = new Picasso.Builder(this);
    builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
    Picasso built = builder.build();
    built.setLoggingEnabled(true);
    Picasso.setSingletonInstance(built);
  }
}
