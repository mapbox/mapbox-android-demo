package com.mapbox.mapboxandroidweardemo.examples;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import com.mapbox.mapboxandroidweardemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * The watch will need to have "Always-on screen" enabled in the settings for this example to work
 * properly. see https://developer.android.com/training/wearables/apps/always-on.html to learn more
 * about ambient mode.
 */
public class AlwaysOnMapActivity extends WearableActivity {

  private static final String LOG_TAG = "AlwaysOnMapActivity";

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    MapboxAccountManager.start(this, getString(R.string.access_token));

    // Enable ambient mode
    Log.i(LOG_TAG, "setAmbientEnabled");
    setAmbientEnabled();

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_always_on_map);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        map = mapboxMap;

      }
    });

  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onEnterAmbient(Bundle ambientDetails) {
    Log.i(LOG_TAG, "onEnterAmbient");
    super.onEnterAmbient(ambientDetails);
    updateDisplay();
  }

  @Override
  public void onUpdateAmbient() {
    Log.i(LOG_TAG, "onUpdateAmbient");
    super.onUpdateAmbient();
    updateDisplay();
  }

  @Override
  public void onExitAmbient() {
    Log.i(LOG_TAG, "onExitAmbient");
    super.onExitAmbient();
    updateDisplay();
  }

  private void updateDisplay() {
    System.out.println(isAmbient());
    if (isAmbient()) {
      Log.i(LOG_TAG, "isAmbient true");
      map.setStyleUrl("mapbox://styles/mapbox/dark-v9");
    } else {
      Log.i(LOG_TAG, "isAmbient false");
      map.setStyleUrl("mapbox://styles/mapbox/light-v9");
    }
  }
}
