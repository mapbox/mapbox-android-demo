package com.mapbox.mapboxandroiddemo.examples.camera;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class RestrictCameraActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final LatLngBounds ICELAND_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(66.852863, -25.985652))
    .include(new LatLng(62.985661, -12.626277))
    .build();

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_camera_restrict);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    // Set bounds to Iceland
    mapboxMap.setLatLngBoundsForCameraTarget(ICELAND_BOUNDS);
    mapboxMap.setMinZoomPreference(2);

    // Visualise bounds area
    showBoundsArea();
  }

  private void showBoundsArea() {
    PolygonOptions boundsArea = new PolygonOptions()
      .add(ICELAND_BOUNDS.getNorthWest())
      .add(ICELAND_BOUNDS.getNorthEast())
      .add(ICELAND_BOUNDS.getSouthEast())
      .add(ICELAND_BOUNDS.getSouthWest());
    boundsArea.alpha(0.25f);
    boundsArea.fillColor(Color.RED);
    mapboxMap.addPolygon(boundsArea);
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