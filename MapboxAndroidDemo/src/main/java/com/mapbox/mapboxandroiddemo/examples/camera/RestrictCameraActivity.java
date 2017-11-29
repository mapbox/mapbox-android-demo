package com.mapbox.mapboxandroiddemo.examples.camera;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Restrict the map camera to certain bounds.
 */
public class RestrictCameraActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final LatLngBounds AUSTRALIA_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(-9.136343, 109.372126))
    .include(new LatLng(-44.640488, 158.590484))
    .build();

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Marker marker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_camera_restrict);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    // Set bounds to Australia
    mapboxMap.setLatLngBoundsForCameraTarget(AUSTRALIA_BOUNDS);
    mapboxMap.setMinZoomPreference(2);

    // Visualise bounds area
    showBoundsArea();

    showCrosshair();

  }

  private void showBoundsArea() {
    PolygonOptions boundsArea = new PolygonOptions()
      .add(AUSTRALIA_BOUNDS.getNorthWest())
      .add(AUSTRALIA_BOUNDS.getNorthEast())
      .add(AUSTRALIA_BOUNDS.getSouthEast())
      .add(AUSTRALIA_BOUNDS.getSouthWest());
    boundsArea.alpha(0.25f);
    boundsArea.fillColor(Color.RED);
    mapboxMap.addPolygon(boundsArea);
  }

  private void showCrosshair() {
    View crosshair = new View(this);
    crosshair.setLayoutParams(new FrameLayout.LayoutParams(15, 15, Gravity.CENTER));
    crosshair.setBackgroundColor(Color.GREEN);
    mapView.addView(crosshair);
  }


  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}