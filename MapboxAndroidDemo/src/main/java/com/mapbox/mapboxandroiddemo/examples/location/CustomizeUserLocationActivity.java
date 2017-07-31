package com.mapbox.mapboxandroiddemo.examples.location;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
 * Adjust the user location icon's colors and padding so it appears lower on the screen.
 */
public class CustomizeUserLocationActivity extends AppCompatActivity implements PermissionsListener {

  private PermissionsManager permissionsManager;
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

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_customize_user);

    // Get the location engine object for later use.
    locationEngine = new LocationSource(this);
    locationEngine.activate();

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        permissionsManager = new PermissionsManager(CustomizeUserLocationActivity.this);
        if (!PermissionsManager.areLocationPermissionsGranted(CustomizeUserLocationActivity.this)) {
          permissionsManager.requestLocationPermissions(CustomizeUserLocationActivity.this);
        } else {
          enableLocation();
        }

        // Customize the user location icon using the getMyLocationViewSettings object.
        map.getMyLocationViewSettings().setPadding(0, 200, 0, 0);
        map.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#56B881"));
        map.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FBB03B"));
      }
    });
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

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, R.string.user_location_permission_explanation,
      Toast.LENGTH_LONG).show();
  }


  private void enableLocation() {

    // If we have the last location of the user, we can move the camera to that position.
    Location lastLocation = locationEngine.getLastLocation();
    if (lastLocation != null) {
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 4));
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
          map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 4));
          locationEngine.removeLocationEngineListener(this);
        }
      }
    };

    locationEngine.addLocationEngineListener(locationEngineListener);

    //Enable or disable the location layer on the map
    map.setMyLocationEnabled(true);
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      enableLocation();
    } else {
      finish();
    }
  }
}