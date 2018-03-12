package com.mapbox.mapboxandroiddemo;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.mapbox.mapboxsdk.Mapbox;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class MapboxApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeFirebaseApp();
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } else {
      Timber.plant(new CrashReportingTree());
    }
    setUpPicasso();
    Mapbox.getInstance(this, getString(R.string.access_token));
  }

  /**
   * A tree which logs important information for crash reporting.
   */
  private static class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG) {
        return;
      }
      if (!TextUtils.isEmpty(message)) {
        Crashlytics.log(priority, tag, message);
      }
      if (throwable != null && priority == Log.ERROR || priority == Log.WARN) {
        Crashlytics.logException(throwable);
      }
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
