package com.mapbox.mapboxandroiddemo.examples.location;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationCameraTransitionListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Use LocationComponent camera options to customize the map camera's behavior. More information
 * can be found at https://docs.mapbox.com/android/maps/overview/location-component and in
 * {@link CameraMode}.
 */
public class LocationComponentCameraOptionsActivity extends AppCompatActivity implements OnMapReadyCallback,
  OnLocationClickListener, OnCameraTrackingChangedListener {

  private static final String SAVED_STATE_CAMERA = "saved_state_camera";
  private static final String SAVED_STATE_RENDER = "saved_state_render";
  private static final String SAVED_STATE_LOCATION = "saved_state_location";
  private MapView mapView;
  private Button locationModeBtn;
  private Button locationTrackingBtn;
  private PermissionsManager permissionsManager;
  private LocationComponent locationComponent;
  private MapboxMap mapboxMap;
  private Location lastLocation;

  @CameraMode.Mode
  private int cameraMode = CameraMode.TRACKING;

  @RenderMode.Mode
  private int renderMode = RenderMode.NORMAL;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_component_camera_options);

    mapView = findViewById(R.id.mapView);

    // Check and use saved instance state in case of device rotation
    if (savedInstanceState != null) {
      cameraMode = savedInstanceState.getInt(SAVED_STATE_CAMERA);
      renderMode = savedInstanceState.getInt(SAVED_STATE_RENDER);
      lastLocation = savedInstanceState.getParcelable(SAVED_STATE_LOCATION);
    }

    mapView.onCreate(savedInstanceState);

    // Check for (and request) the device location permission
    if (PermissionsManager.areLocationPermissionsGranted(this)) {
      mapView.getMapAsync(this);
    } else {
      permissionsManager = new PermissionsManager(new PermissionsListener() {
        @Override
        public void onExplanationNeeded(List<String> permissionsToExplain) {
          Toast.makeText(LocationComponentCameraOptionsActivity.this,
            R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPermissionResult(boolean granted) {
          if (granted) {
            mapView.getMapAsync(LocationComponentCameraOptionsActivity.this);
          } else {
            finish();
          }
        }
      });
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

      setModeButtonListeners();

      // Retrieve and customize the Maps SDK's LocationComponent
      locationComponent = mapboxMap.getLocationComponent();
      locationComponent.activateLocationComponent(
        LocationComponentActivationOptions
          .builder(this, style)
          .useDefaultLocationEngine(true)
          .locationEngineRequest(new LocationEngineRequest.Builder(750)
            .setFastestInterval(750)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .build())
          .build());

      locationComponent.setLocationComponentEnabled(true);
      locationComponent.addOnLocationClickListener(this);
      locationComponent.addOnCameraTrackingChangedListener(this);
      locationComponent.setCameraMode(cameraMode);
      setRendererMode(renderMode);
      locationComponent.forceLocationUpdate(lastLocation);
    });
  }

  @SuppressLint("MissingPermission")
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);

    // Save LocationComponent-related settings to use once device rotation is finished
    outState.putInt(SAVED_STATE_CAMERA, cameraMode);
    outState.putInt(SAVED_STATE_RENDER, renderMode);
    if (locationComponent != null) {
      outState.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
    }
  }

  /**
   * Listen to and use a tap on the LocationComponent
   */
  @Override
  public void onLocationComponentClick() {
    Toast.makeText(this, getString(R.string.clicked_on_location_component), Toast.LENGTH_LONG).show();
  }

  /**
   * Adjust the LocationComponent's image to one of the preset options.
   *
   * @param mode desired normal (small blue circle laid on top of larger white dot),
   *             compass (arrow point representing the phone's bearing), or
   *             GPS (blue arrow within a white circle).
   */
  private void setRendererMode(@RenderMode.Mode int mode) {
    renderMode = mode;
    locationComponent.setRenderMode(mode);
    if (mode == RenderMode.NORMAL) {
      locationModeBtn.setText(getString(R.string.normal));
    } else if (mode == RenderMode.COMPASS) {
      locationModeBtn.setText(getString(R.string.compass));
    } else if (mode == RenderMode.GPS) {
      locationModeBtn.setText(getString(R.string.gps));
    }
  }

  private void showModeListDialog() {
    List<String> modes = new ArrayList<>();
    modes.add(getString(R.string.normal));
    modes.add(getString(R.string.compass));
    modes.add(getString(R.string.gps));
    ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, modes);
    ListPopupWindow listPopup = new ListPopupWindow(this);
    listPopup.setAdapter(profileAdapter);
    listPopup.setAnchorView(locationModeBtn);
    listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
      String selectedMode = modes.get(position);
      locationModeBtn.setText(selectedMode);
      if (selectedMode.contentEquals(getString(R.string.normal))) {
        setRendererMode(RenderMode.NORMAL);
      } else if (selectedMode.contentEquals(getString(R.string.compass))) {
        setRendererMode(RenderMode.COMPASS);
      } else if (selectedMode.contentEquals(getString(R.string.gps))) {
        setRendererMode(RenderMode.GPS);
      }
      listPopup.dismiss();
    });
    listPopup.show();
  }

  /**
   * Instruct the map camera to disregard the LocationComponent or to
   * follow the device location in a certain way.
   * <p>
   * NONE = No camera tracking.
   * NONE_COMPASS = Camera does not track location, but does track compass bearing.
   * NONE_GPS = Camera does not track location, but does track GPS {@link Location} bearing.
   * TRACKING = Camera tracks the user location.
   * TRACKING_COMPASS = Camera tracks the user location, with bearing provided by a compass.
   * TRACKING_GPS = Camera tracks the user location, with bearing provided by a
   * normalized {@link Location#getBearing()}.
   * TRACKING_GPS_NORTH = Camera tracks the user location, with bearing always set to north (0).
   */
  private void showTrackingListDialog() {
    List<String> trackingTypes = new ArrayList<>();
    trackingTypes.add(getString(R.string.none));
    trackingTypes.add(getString(R.string.none_compass));
    trackingTypes.add(getString(R.string.none_gps));
    trackingTypes.add(getString(R.string.tracking));
    trackingTypes.add(getString(R.string.tracking_compass));
    trackingTypes.add(getString(R.string.tracking_gps));
    trackingTypes.add(getString(R.string.tracking_gps_north));
    ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, trackingTypes);
    ListPopupWindow listPopup = new ListPopupWindow(this);
    listPopup.setAdapter(profileAdapter);
    listPopup.setAnchorView(locationTrackingBtn);
    listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
      String selectedTrackingType = trackingTypes.get(position);
      locationTrackingBtn.setText(selectedTrackingType);
      if (selectedTrackingType.contentEquals(getString(R.string.none))) {
        setCameraTrackingMode(CameraMode.NONE);
      } else if (selectedTrackingType.contentEquals(getString(R.string.none_compass))) {
        setCameraTrackingMode(CameraMode.NONE_COMPASS);
      } else if (selectedTrackingType.contentEquals(getString(R.string.none_gps))) {
        setCameraTrackingMode(CameraMode.NONE_GPS);
      } else if (selectedTrackingType.contentEquals(getString(R.string.tracking))) {
        setCameraTrackingMode(CameraMode.TRACKING);
      } else if (selectedTrackingType.contentEquals(getString(R.string.tracking_compass))) {
        setCameraTrackingMode(CameraMode.TRACKING_COMPASS);
      } else if (selectedTrackingType.contentEquals(getString(R.string.tracking_gps))) {
        setCameraTrackingMode(CameraMode.TRACKING_GPS);
      } else if (selectedTrackingType.contentEquals(getString(R.string.tracking_gps_north))) {
        setCameraTrackingMode(CameraMode.TRACKING_GPS_NORTH);
      }
      listPopup.dismiss();
    });
    listPopup.show();
  }

  private void setCameraTrackingMode(@CameraMode.Mode int mode) {
    locationComponent.setCameraMode(mode, new OnLocationCameraTransitionListener() {
      @Override
      public void onLocationCameraTransitionFinished(@CameraMode.Mode int cameraMode) {
        if (mode != CameraMode.NONE) {
          locationComponent.zoomWhileTracking(15, 750, new MapboxMap.CancelableCallback() {
            @Override
            public void onCancel() {
              // No impl
            }

            @Override
            public void onFinish() {
              locationComponent.tiltWhileTracking(45);
            }
          });
        } else {
          mapboxMap.easeCamera(CameraUpdateFactory.tiltTo(0));
        }
      }

      @Override
      public void onLocationCameraTransitionCanceled(@CameraMode.Mode int cameraMode) {
        // No impl
      }
    });
  }

  @Override
  public void onCameraTrackingDismissed() {
    locationTrackingBtn.setText(getString(R.string.none));
  }

  @Override
  public void onCameraTrackingChanged(int currentMode) {
    this.cameraMode = currentMode;
    if (currentMode == CameraMode.NONE) {
      locationTrackingBtn.setText(getString(R.string.none));
    } else if (currentMode == CameraMode.NONE_COMPASS) {
      locationTrackingBtn.setText(getString(R.string.none_compass));
    } else if (currentMode == CameraMode.NONE_GPS) {
      locationTrackingBtn.setText(getString(R.string.none_gps));
    } else if (currentMode == CameraMode.TRACKING) {
      locationTrackingBtn.setText(getString(R.string.tracking));
    } else if (currentMode == CameraMode.TRACKING_COMPASS) {
      locationTrackingBtn.setText(getString(R.string.tracking_compass));
    } else if (currentMode == CameraMode.TRACKING_GPS) {
      locationTrackingBtn.setText(getString(R.string.tracking_gps));
    } else if (currentMode == CameraMode.TRACKING_GPS_NORTH) {
      locationTrackingBtn.setText(getString(R.string.tracking_gps_north));
    }
  }

  private void setModeButtonListeners() {
    locationModeBtn = findViewById(R.id.button_location_mode);
    locationModeBtn.setOnClickListener(v -> {
      if (locationComponent == null) {
        return;
      }
      showModeListDialog();
    });

    locationTrackingBtn = findViewById(R.id.button_location_tracking);
    locationTrackingBtn.setOnClickListener(v -> {
      if (locationComponent == null) {
        return;
      }
      showTrackingListDialog();
    });
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