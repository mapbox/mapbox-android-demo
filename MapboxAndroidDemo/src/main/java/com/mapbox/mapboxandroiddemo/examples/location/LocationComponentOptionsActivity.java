package com.mapbox.mapboxandroiddemo.examples.location;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Use the LocationLayerOptions class to customize the LocationComponent's device location icon.
 */
public class LocationComponentOptionsActivity extends AppCompatActivity implements
  OnMapReadyCallback, OnLocationClickListener, PermissionsListener, OnCameraTrackingChangedListener {

  private PermissionsManager permissionsManager;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private LocationComponent locationComponent;
  private boolean isInTrackingMode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_component_options);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    enableLocationComponent();
  }

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationComponent() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Create and customize the LocationComponent's options
      LocationComponentOptions options = LocationComponentOptions.builder(this)
        .elevation(5)
        .accuracyAlpha(.6f)
        .accuracyColor(Color.RED)
        .foregroundDrawable(R.drawable.android_custom_location_icon)
        .build();

      // Get an instance of the component
      locationComponent = mapboxMap.getLocationComponent();

      // Activate with options
      locationComponent.activateLocationComponent(this, options);

      // Enable to make component visible
      locationComponent.setLocationComponentEnabled(true);

      // Set the component's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);

      // Set the component's render mode
      locationComponent.setRenderMode(RenderMode.COMPASS);

      // Add the location icon click listener
      locationComponent.addOnLocationClickListener(this);

      // Add the camera tracking listener. Fires if the map camera is manually moved.
      locationComponent.addOnCameraTrackingChangedListener(this);

      setUpClickMeSymbolLayer();

      findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(view -> {
        if (!isInTrackingMode) {
          isInTrackingMode = true;
        } else {
          isInTrackingMode = false;
        }
      });

    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @SuppressWarnings( {"MissingPermission"})
  @Override
  public void onLocationComponentClick() {
    if (locationComponent.getLastKnownLocation() != null) {
      Toast.makeText(this, String.format(getString(R.string.current_location),
        locationComponent.getLastKnownLocation().getLatitude(),
        locationComponent.getLastKnownLocation().getLongitude()), Toast.LENGTH_LONG).show();
      
    }
  }

  @Override
  public void onCameraTrackingDismissed() {
    Log.d("LocOptionsActivity", "onCameraTrackingDismissed: ");
    isInTrackingMode = false;
  }

  @Override
  public void onCameraTrackingChanged(int currentMode) {
    Log.d("LocOptionsActivity", "onCameraTrackingChanged: ");
  }

  @SuppressWarnings( {"MissingPermission"})
  private void setUpClickMeSymbolLayer() {
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource geoJsonSource = new GeoJsonSource("source-id", featureCollection);
    mapboxMap.addSource(geoJsonSource);
    SymbolLayer symbolLayer = new SymbolLayer("layer-id", "source-id");
    symbolLayer.setProperties(
      textSize(17f),
      textColor(Color.BLUE),
      textField(getString(R.string.tap_the_icon)),
      textOffset(new Float[] {0f, -3f}),
      textIgnorePlacement(true),
      textAllowOverlap(true)
    );
    mapboxMap.addLayer(symbolLayer);
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
      enableLocationComponent();
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  @SuppressWarnings( {"MissingPermission"})
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