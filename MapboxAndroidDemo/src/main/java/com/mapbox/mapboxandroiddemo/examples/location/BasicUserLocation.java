package com.mapbox.mapboxandroiddemo.examples.location;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.util.List;

/**
 * Toggle the user location on the map.
 */
public class BasicUserLocation extends AppCompatActivity implements PermissionsListener {

  private MapView mapView;
  private MapboxMap map;
  private FloatingActionButton floatingActionButton;
  private LocationEngine locationEngine;
  private LocationEngineListener locationEngineListener;
  private PermissionsManager permissionsManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_basic);

    // Get the location engine object for later use.
    locationEngine = new LocationSource(this);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        map.setStyleUrl("mapbox://styles/mapbox/streets-v10");
      }
    });

    floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
    floatingActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
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
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
    // Ensure no memory leak occurs if we register the location listener but the call hasn't
    // been made yet.
    if (locationEngineListener != null) {
      locationEngine.removeLocationEngineListener(locationEngineListener);
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  private void toggleGps(boolean enableGps) {
    if (enableGps) {
      // Check if user has granted location permission
      permissionsManager = new PermissionsManager(this);
      if (!PermissionsManager.areLocationPermissionsGranted(this)) {
        permissionsManager.requestLocationPermissions(this);
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
      Location lastLocation = locationEngine.getLastLocation();
      if (lastLocation != null) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
      }

      locationEngineListener = new LocationEngineListener() {
        @Override
        public void onConnected() {
          // No action needed here.
        }

        @Override
        public void onLocationChanged(Location location) {
          if (location != null) {
            // Move the map camera to where the user location is and then remove the
            // listener so the camera isn't constantly updating when the user location
            // changes. When the user disables and then enables the location again, this
            // listener is registered again and will adjust the camera once again.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
            locationEngine.removeLocationEngineListener(this);
          }
        }
      };
      locationEngine.addLocationEngineListener(locationEngineListener);
      floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
    } else {
      floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
    }
    // Enable or disable the location layer on the map
    map.setMyLocationEnabled(enabled);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, R.string.user_location_permission_explanation,
      Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      enableLocation(true);
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted,
        Toast.LENGTH_LONG).show();
      finish();
    }
  }
}
