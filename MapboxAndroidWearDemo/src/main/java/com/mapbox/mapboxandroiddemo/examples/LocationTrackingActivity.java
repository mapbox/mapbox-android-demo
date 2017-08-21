package com.mapbox.mapboxandroiddemo.examples;


import android.location.Location;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

/**
 * Lock the camera centered above the user location.
 */
public class LocationTrackingActivity extends WearableActivity {

  private MapView mapView;
  private MapboxMap map;
  private LocationEngine locationEngine;
  private LocationEngineListener locationEngineListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_simple_mapview);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    locationEngine = GoogleLocationEngine.getLocationEngine(this);
    locationEngineListener = new LocationEngineListener() {
      @Override
      public void onConnected() {
        locationEngine.requestLocationUpdates();
      }

      @Override
      public void onLocationChanged(Location location) {
        if (map != null) {
          map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
        }
      }
    };
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        // Customize map with markers, polylines, etc.
        map = mapboxMap;
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
          map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
        }
      }
    });
    locationEngine.addLocationEngineListener(locationEngineListener);
    setAmbientEnabled();
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
  protected void onStart() {
    super.onStart();
    mapView.onStart();
    locationEngine.activate();
  }

  @Override
  protected void onStop() {
    locationEngine.removeLocationUpdates();
    locationEngine.deactivate();
    super.onStop();
    mapView.onStop();
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
}