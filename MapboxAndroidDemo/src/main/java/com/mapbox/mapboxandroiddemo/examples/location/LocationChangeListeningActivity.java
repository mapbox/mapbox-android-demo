package com.mapbox.mapboxandroiddemo.examples.location;

import com.mapbox.mapboxandroiddemo.R;
import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
// Classes needed to initialize the map
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
// Classes needed to handle location permissions
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import java.util.List;
// Classes needed to add the location engine
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import java.lang.ref.WeakReference;
// Classes needed to add the location component
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;

/**
 * Use the Mapbox Core Library to listen to device location updates
 */
public class LocationChangeListeningActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {
  // Variables needed to initialize a map
  private MapboxMap mapboxMap;
  private MapView mapView;
  // Variables needed to handle location permissions
  private PermissionsManager permissionsManager;
  // Variables needed to add the location engine
  private LocationEngine locationEngine;
  private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
  private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
  // Variables needed to listen to location updates
  private LocationChangeListeningActivityLocationCallback callback = new LocationChangeListeningActivityLocationCallback(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_change_listening);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    mapboxMap.setStyle(Style.TRAFFIC_NIGHT,
            new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
              }
            });
  }

  /**
   * Initialize the Maps SDK's LocationComponent
   */
  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationComponent(@NonNull Style loadedMapStyle) {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Get an instance of the component
      LocationComponent locationComponent = mapboxMap.getLocationComponent();

      // Set the LocationComponent activation options
      LocationComponentActivationOptions locationComponentActivationOptions =
              LocationComponentActivationOptions.builder(this, loadedMapStyle)
                      .useDefaultLocationEngine(false)
                      .build();

      // Activate with the LocationComponentActivationOptions object
      locationComponent.activateLocationComponent(locationComponentActivationOptions);

      // Enable to make component visible
      locationComponent.setLocationComponentEnabled(true);

      // Set the component's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING);

      // Set the component's render mode
      locationComponent.setRenderMode(RenderMode.COMPASS);

      initLocationEngine();
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  /**
   * Set up the LocationEngine and the parameters for querying the device's location
   */
  @SuppressLint("MissingPermission")
  private void initLocationEngine() {
    locationEngine = LocationEngineProvider.getBestLocationEngine(this);

    LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

    locationEngine.requestLocationUpdates(request, callback, getMainLooper());
    locationEngine.getLastLocation(callback);
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
      if (mapboxMap.getStyle() != null) {
        enableLocationComponent(mapboxMap.getStyle());
      }
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private static class LocationChangeListeningActivityLocationCallback
          implements LocationEngineCallback<LocationEngineResult> {

    private final WeakReference<LocationChangeListeningActivity> activityWeakReference;

    LocationChangeListeningActivityLocationCallback(LocationChangeListeningActivity activity) {
      this.activityWeakReference = new WeakReference<>(activity);
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location has changed.
     *
     * @param result the LocationEngineResult object which has the last known location within it.
     */
    @Override
    public void onSuccess(LocationEngineResult result) {
      LocationChangeListeningActivity activity = activityWeakReference.get();

      if (activity != null) {
        Location location = result.getLastLocation();

        if (location == null) {
          return;
        }

        // Create a Toast which displays the new location's coordinates
        Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
                String.valueOf(result.getLastLocation().getLatitude()), String.valueOf(result.getLastLocation().getLongitude())),
                Toast.LENGTH_SHORT).show();

        // Pass the new location to the Maps SDK's LocationComponent
        if (activity.mapboxMap != null && result.getLastLocation() != null) {
          activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
        }
      }
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location can not be captured
     *
     * @param exception the exception message
     */
    @Override
    public void onFailure(@NonNull Exception exception) {
      Log.d("LocationChangeActivity", exception.getLocalizedMessage());
      LocationChangeListeningActivity activity = activityWeakReference.get();
      if (activity != null) {
        Toast.makeText(activity, exception.getLocalizedMessage(),
                Toast.LENGTH_SHORT).show();
      }
    }
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
    // Prevent leaks
    if (locationEngine != null) {
      locationEngine.removeLocationUpdates(callback);
    }
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}