package com.mapbox.mapboxandroiddemo.examples.location;

import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class LocationComponentFragmentActivity extends AppCompatActivity implements PermissionsListener {

  private MapboxMap mapboxMap;
  private PermissionsManager permissionsManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_location_map_frag);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // Create supportMapFragment
    SupportMapFragment mapFragment;
    if (savedInstanceState == null) {

      // Create fragment
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

      // Build a Mapbox map
      MapboxMapOptions options = MapboxMapOptions.createFromAttributes(this, null);
      options.camera(new CameraPosition.Builder()
        .target(new LatLng(38.899895, -77.03401))
        .zoom(9)
        .build());

      // Create map fragment
      mapFragment = SupportMapFragment.newInstance(options);

      // Add map fragment to parent container
      transaction.add(R.id.location_frag_container, mapFragment, "com.mapbox.map");
      transaction.commit();
    } else {
      mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
    }

    if (mapFragment != null) {
      mapFragment.getMapAsync(new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull MapboxMap mapboxMap) {
          LocationComponentFragmentActivity.this.mapboxMap = mapboxMap;
          mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              enableLocationComponent(style);
            }
          });
        }
      });
    }
  }

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationComponent(@NonNull Style loadedMapStyle) {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Get an instance of the LocationComponent.
      LocationComponent locationComponent = mapboxMap.getLocationComponent();

      // Activate the LocationComponent
      locationComponent.activateLocationComponent(
        LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

      // Enable the LocationComponent so that it's actually visible on the map
      locationComponent.setLocationComponentEnabled(true);

      // Set the LocationComponent's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING);

      // Set the LocationComponent's render mode
      locationComponent.setRenderMode(RenderMode.NORMAL);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      mapboxMap.getStyle(new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          enableLocationComponent(style);
        }
      });
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
    }
  }
}