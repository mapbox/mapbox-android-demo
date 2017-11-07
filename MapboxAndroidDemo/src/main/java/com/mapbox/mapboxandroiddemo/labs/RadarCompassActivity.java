package com.mapbox.mapboxandroiddemo.labs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;

import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;

/**
 * Place a rotating mini-window compass map on top of a camera preview.
 */
public class RadarCompassActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private LocationLayerPlugin locationLayerPlugin;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_radar_compass);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    LocationEngine locationEngine = new LostLocationEngine(this);
    locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
    locationLayerPlugin.setLocationLayerEnabled(LocationLayerMode.COMPASS);
    locationLayerPlugin.addCompass(new CompassListener() {
      @Override
      public void onCompassChanged(float userHeading) {
        CameraPosition cameraPosition = new CameraPosition.Builder().bearing(userHeading).build();
        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
      }

      @Override
      public void onCompassAccuracyChange(int compassStatus) {
        System.out.println(compassStatus);
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
    if (locationLayerPlugin != null) {
      locationLayerPlugin.onStart();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
    locationLayerPlugin.onStop();
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
    locationLayerPlugin.removeCompassListener(null);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}