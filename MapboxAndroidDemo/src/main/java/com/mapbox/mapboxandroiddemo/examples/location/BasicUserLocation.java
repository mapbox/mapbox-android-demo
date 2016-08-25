package com.mapbox.mapboxandroiddemo.examples.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class BasicUserLocation extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private FloatingActionButton floatingActionButton;
  private LocationServices locationServices;


  private static final int PERMISSIONS_LOCATION = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location_basic);

    locationServices = LocationServices.getLocationServices(BasicUserLocation.this);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
      }
    });

    floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
    floatingActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (map != null) {
          toggleGps(!map.isMyLocationEnabled());
        }
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

  private void toggleGps(boolean enableGps) {
    if (enableGps) {
      // Check if user has granted location permission
      if (!locationServices.areLocationPermissionsGranted()) {
        ActivityCompat.requestPermissions(this, new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
      } else {
        enableLocation(true);
      }
    } else {
      enableLocation(false);
    }
  }

  private void enableLocation(boolean enabled) {
    if (enabled) {
      // If we have the last location of the user, we can move the camera to that position.
      Location lastLocation = locationServices.getLastLocation();
      if (lastLocation != null) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
      }

      locationServices.addLocationListener(new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
          if (location != null) {
            // Move the map camera to where the user location is and then remove the
            // listener so the camera isn't constantly updating when the user location
            // changes. When the user disables and then enables the location again, this
            // listener is registered again and will adjust the camera once again.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
            locationServices.removeLocationListener(this);
          }
        }
      });
      floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
    } else {
      floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
    }
    // Enable or disable the location layer on the map
    map.setMyLocationEnabled(enabled);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_LOCATION: {
        if (grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          enableLocation(true);
        }
      }
    }
  }
}
