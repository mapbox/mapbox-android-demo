package com.mapbox.mapboxandroiddemo.examples.camera;

import android.animation.ValueAnimator;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;
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
 * Animate the map's camera to slowly spin around a single point ont the map.
 */
public class SlowlyRotatingCameraActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private static final int DESIRED_NUM_OF_SPINS = 5;
  private static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN = 40;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private ValueAnimator animator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_camera_slow_spin);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        SlowlyRotatingCameraActivity.this.mapboxMap = mapboxMap;

        mapboxMap.addOnMapClickListener(SlowlyRotatingCameraActivity.this);

        // Toast instructing user to tap on the map
        Toast.makeText(
          SlowlyRotatingCameraActivity.this, getString(R.string.rotating_camera_toast_instruction),
          Toast.LENGTH_LONG).show();

        startMapCameraSpinningAnimation(mapboxMap.getCameraPosition().target);
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    startMapCameraSpinningAnimation(point);
    return true;
  }

  /**
   * Set up and start the spin animation. The Android system ValueAnimator emits a new value and that value is
   * used as the map camera's new bearing rotation amount. A smooth "new helicopter" type of effect is created
   * by using a LinearInterpolator.
   *
   * @param mapCameraTargetLocation the map location that the map camera should spin around
   */
  private void startMapCameraSpinningAnimation(@NonNull final LatLng mapCameraTargetLocation) {
    if (animator != null) {
      animator.cancel();
    }
    animator = ValueAnimator.ofFloat(0, DESIRED_NUM_OF_SPINS * 360);
    animator.setDuration(
      // Multiplying by 1000 to convert to milliseconds
      DESIRED_NUM_OF_SPINS * DESIRED_SECONDS_PER_ONE_FULL_360_SPIN * 1000);
    animator.setInterpolator(new LinearInterpolator());
    animator.setStartDelay(1000);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        // Retrieve the new animation number to use as the map camera bearing value
        Float newBearingValue = (Float) valueAnimator.getAnimatedValue();

        // Use the animation number in a new camera position and then direct the map camera to move to the new position
        mapboxMap.moveCamera(CameraUpdateFactory
          .newCameraPosition(new CameraPosition.Builder()
            .target(new LatLng(mapCameraTargetLocation.getLatitude(), mapCameraTargetLocation.getLongitude()))
            .bearing(newBearingValue)
            .build()));
      }
    });
    animator.start();
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
    if (animator != null) {
      animator.end();
    }
    mapView.onDestroy();
  }
}
