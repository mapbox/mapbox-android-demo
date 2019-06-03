package com.mapbox.mapboxandroiddemo.examples.camera;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

/**
 * Animate the map's camera position, tilt, bearing, and zoom.
 */
public class AnimateMapCameraActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener  {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_camera_animate);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    AnimateMapCameraActivity.this.mapboxMap = mapboxMap;

    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Toast instructing user to tap on the map
        Toast.makeText(
          AnimateMapCameraActivity.this,
          getString(R.string.tap_on_map_instruction),
          Toast.LENGTH_LONG
        ).show();

        mapboxMap.addOnMapClickListener(AnimateMapCameraActivity.this);
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {

    // Toast instructing user to tap on the map
    Toast.makeText(
      AnimateMapCameraActivity.this,
      getString(R.string.tap_on_map_instruction),
      Toast.LENGTH_LONG
    ).show();

    CameraPosition position = new CameraPosition.Builder()
      .target(new LatLng(51.50550, -0.07520)) // Sets the new camera position
      .zoom(17) // Sets the zoom
      .bearing(180) // Rotate the camera
      .tilt(30) // Set the camera tilt
      .build(); // Creates a CameraPosition from the builder

    mapboxMap.animateCamera(CameraUpdateFactory
      .newCameraPosition(position), 7000);

    return true;
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
