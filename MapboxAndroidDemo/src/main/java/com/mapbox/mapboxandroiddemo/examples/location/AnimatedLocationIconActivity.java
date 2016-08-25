package com.mapbox.mapboxandroiddemo.examples.location;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.PulseMarkerView;
import com.mapbox.mapboxandroiddemo.model.PulseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * This example shows how to create your own marker and update its location so that we follow the
 * user's position. In addition the background of the marker uses a pulsing animation. There are a
 * few additional files that go along with this example.
 *
 * @see PulseMarkerViewOptions
 * @see PulseMarkerView
 * @see ~/drawable/ic_circle.png
 *
 * If you are just getting started with adding user location to your map, it's recommended to start
 * with this example instead:
 * @see BasicUserLocation
 */
public class AnimatedLocationIconActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private MarkerView userMarker;
  private LocationServices locationServices;
  private boolean initialLocationSet = false;

  private static final int PERMISSIONS_LOCATION = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location_animated_icon);

    locationServices = LocationServices.getLocationServices(AnimatedLocationIconActivity.this);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        mapboxMap.getMarkerViewManager().addMarkerViewAdapter(new PulseMarkerViewAdapter(AnimatedLocationIconActivity.this));

        userMarker = mapboxMap.addMarker(new PulseMarkerViewOptions().position(new LatLng(0, 0)));

        animateMarker(userMarker);

        if (locationServices.areLocationPermissionsGranted()) {
          setInitialMapPosition();
        }

        enableGPS();
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

  private void animateMarker(MarkerView marker) {

    View view = map.getMarkerViewManager().getView(marker);
    if (view != null) {
      View backgroundView = view.findViewById(R.id.background_imageview);

      ValueAnimator scaleCircleX = ObjectAnimator.ofFloat(backgroundView, "scaleX", 1.8f);
      ValueAnimator scaleCircleY = ObjectAnimator.ofFloat(backgroundView, "scaleY", 1.8f);
      ObjectAnimator fadeOut = ObjectAnimator.ofFloat(backgroundView, "alpha", 1f, 0f);

      scaleCircleX.setRepeatCount(ValueAnimator.INFINITE);
      scaleCircleY.setRepeatCount(ValueAnimator.INFINITE);
      fadeOut.setRepeatCount(ObjectAnimator.INFINITE);

      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.play(scaleCircleX).with(scaleCircleY).with(fadeOut);
      animatorSet.setDuration(1000);
      animatorSet.start();
    }
  }

  private void enableGPS() {
    // Check if user has granted location permission
    if (!locationServices.areLocationPermissionsGranted()) {
      ActivityCompat.requestPermissions(this, new String[]{
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
    } else {
      enableLocation();
    }
  }

  private void setInitialMapPosition() {
    Location lastLocation = LocationServices.getLocationServices(this).getLastLocation();
    if (lastLocation != null && userMarker != null) {
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
      userMarker.setPosition(new LatLng(lastLocation));
    }
  }

  private void enableLocation() {
    locationServices.addLocationListener(new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        if (location != null) {
          if (!initialLocationSet) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
            initialLocationSet = true;
          }
          userMarker.setPosition(new LatLng(location));
        }
      }
    });
    // Enable GPS location tracking.
    locationServices.toggleGPS(true);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_LOCATION: {
        if (grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          enableLocation();
        } else {
          // User denied location permission, user marker won't be shown.
          Toast.makeText(AnimatedLocationIconActivity.this,
              "Enable location for example to work properly", Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  // Custom marker view used for pulsing the background view of marker.
  private static class PulseMarkerViewAdapter extends MapboxMap.MarkerViewAdapter<PulseMarkerView> {

    private LayoutInflater inflater;

    public PulseMarkerViewAdapter(@NonNull Context context) {
      super(context);
      this.inflater = LayoutInflater.from(context);
    }

    @Nullable
    @Override
    public View getView(@NonNull PulseMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {
      ViewHolder viewHolder;
      if (convertView == null) {
        viewHolder = new ViewHolder();
        convertView = inflater.inflate(R.layout.view_pulse_marker, parent, false);
        viewHolder.foregroundImageView = (ImageView) convertView.findViewById(R.id.foreground_imageView);
        viewHolder.backgroundImageView = (ImageView) convertView.findViewById(R.id.background_imageview);
        convertView.setTag(viewHolder);
      }
      return convertView;
    }

    private static class ViewHolder {
      ImageView foregroundImageView;
      ImageView backgroundImageView;
    }
  }
}
