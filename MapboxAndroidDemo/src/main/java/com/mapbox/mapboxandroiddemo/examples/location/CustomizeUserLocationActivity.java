package com.mapbox.mapboxandroiddemo.examples.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class CustomizeUserLocationActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private LocationServices locationServices;

  private static final int PERMISSIONS_LOCATION = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_location_customize_user);

    locationServices = LocationServices.getLocationServices(CustomizeUserLocationActivity.this);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        enableGps();

        // Customize the user location icon using the getMyLocationViewSettings object.
        map.getMyLocationViewSettings().setPadding(0, 500, 0, 0);
        map.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#56B881"));
        map.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FBB03B"));

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
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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

  private void enableGps() {
    // Check if user has granted location permission
    if (!locationServices.areLocationPermissionsGranted()) {
      ActivityCompat.requestPermissions(this, new String[] {
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
    } else {
      enableLocation();
    }
  }

  private void enableLocation() {
    // Enable user tracking to show the padding affect.
    map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
    map.getTrackingSettings().setDismissAllTrackingOnGesture(false);
  }

  @Override
  public void onRequestPermissionsResult(
    int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSIONS_LOCATION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        enableLocation();
      }
    }
  }
}