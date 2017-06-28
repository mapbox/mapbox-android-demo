package com.mapbox.mapboxandroiddemo.examples.offline;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class DownloadOfflineMapService extends IntentService {

  public DownloadOfflineMapService() {
    super("DownloadOfflineMapService");
  }

  public DownloadOfflineMapService(String name) {
    super(name);
  }

  @Override
  public void setIntentRedelivery(boolean enabled) {
    super.setIntentRedelivery(enabled);
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onStart(@Nullable Intent intent, int startId) {
    super.onStart(intent, startId);
  }

  @Override
  public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return super.onBind(intent);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {


  }
}
