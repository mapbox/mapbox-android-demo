package com.mapbox.mapboxandroiddemo.examples.camera;
// #-code-snippet: bounding-box-camera-activity full-java
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Fit a map to a bounding box
 */
public class BoundingBoxCameraActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;

  private LatLng locationOne;
  private LatLng locationTwo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_camera_bounding_box);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    BoundingBoxCameraActivity.this.mapboxMap = mapboxMap;

    // Declare two locations on map
    locationOne = new LatLng(36.532128, -93.489121);
    locationTwo = new LatLng(25.837058, -106.646234);

    // Add markers to map
    mapboxMap.addMarker(new MarkerOptions()
      .position(locationOne));

    mapboxMap.addMarker(new MarkerOptions()
      .position(locationTwo));

    // Toast instructing user to tap on the map to start animation and set bounds
    Toast.makeText(
      BoundingBoxCameraActivity.this,
      getString(R.string.tap_on_map_instruction),
      Toast.LENGTH_LONG
    ).show();

    mapboxMap.addOnMapClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    LatLngBounds latLngBounds = new LatLngBounds.Builder()
      .include(locationOne) // Northeast
      .include(locationTwo) // Southwest
      .build();

    mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 5000);
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
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }
}
// #-end-code-snippet: bounding-box-camera-activity full-java