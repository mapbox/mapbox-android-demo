package com.mapbox.mapboxandroiddemo.examples.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;

/**
 * Use the {@link LocationComponentOptions} builder's
 * various pulsing circle methods to customize the
 * LocationComponent's pulsing circle.
 */
public class LocationComponentCustomPulsingActivity extends AppCompatActivity implements
    OnMapReadyCallback, PermissionsListener {

  // Adjust these variables to customize the example's pulsing circle UI
  private static final float DEFAULT_LOCATION_CIRCLE_PULSE_DURATION_MS = 2300;
  private static final float SECOND_LOCATION_CIRCLE_PULSE_DURATION_MS = 800;
  private static final float THIRD_LOCATION_CIRCLE_PULSE_DURATION_MS = 8000;
  private static final float DEFAULT_LOCATION_CIRCLE_PULSE_RADIUS = 35;
  private static final float DEFAULT_LOCATION_CIRCLE_PULSE_ALPHA = .55f;
  private static final Interpolator DEFAULT_LOCATION_CIRCLE_INTERPOLATOR_PULSE_MODE
      = new DecelerateInterpolator();
  private static final boolean DEFAULT_LOCATION_CIRCLE_PULSE_FADE_MODE = true;

  private static int LOCATION_CIRCLE_PULSE_COLOR;
  private static float LOCATION_CIRCLE_PULSE_DURATION = DEFAULT_LOCATION_CIRCLE_PULSE_DURATION_MS;
  private static final String SAVED_STATE_LOCATION = "saved_state_location";
  private static final String SAVED_STATE_LOCATION_CIRCLE_PULSE_COLOR = "saved_state_color";
  private static final String SAVED_STATE_LOCATION_CIRCLE_PULSE_DURATION = "saved_state_duration";
  private static final String LAYER_BELOW_ID = "waterway-label";

  private Location lastLocation;
  private MapView mapView;
  private Button pulsingCircleDurationButton;
  private Button pulsingCircleColorButton;
  private PermissionsManager permissionsManager;
  private LocationComponent locationComponent;
  private MapboxMap mapboxMap;
  private float currentPulseDuration;
  private List<String> colorOptionList;
  private HashMap<String, Integer> colorHashMap;
  private static final String[] STYLES_TO_CYCLE_THROUGH = new String[] {
    Style.LIGHT,
    Style.OUTDOORS,
    Style.SATELLITE_STREETS,
    Style.DARK,
    Style.MAPBOX_STREETS,
  };
  private static int styleCycleIndex = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_component_custom_pulsing);

    LOCATION_CIRCLE_PULSE_COLOR = Color.BLUE;

    mapView = findViewById(R.id.mapView);
    pulsingCircleColorButton = findViewById(R.id.button_location_circle_color);
    pulsingCircleDurationButton = findViewById(R.id.button_location_circle_duration);

    colorOptionList = new ArrayList<>();
    colorHashMap = new HashMap<>();
    colorHashMap.put("Blue", Color.BLUE);
    colorHashMap.put("Red", Color.RED);
    colorHashMap.put("Green", Color.GREEN);
    colorHashMap.put("Gray", Color.parseColor("#4a4a4a"));
    for (Map.Entry<String, Integer> entry : colorHashMap.entrySet()) {
      String colorKey = entry.getKey();
      colorOptionList.add(colorKey);
    }

    if (savedInstanceState != null) {
      lastLocation = savedInstanceState.getParcelable(SAVED_STATE_LOCATION);
      LOCATION_CIRCLE_PULSE_COLOR = savedInstanceState.getInt(SAVED_STATE_LOCATION_CIRCLE_PULSE_COLOR);
      LOCATION_CIRCLE_PULSE_DURATION = savedInstanceState.getFloat(SAVED_STATE_LOCATION_CIRCLE_PULSE_DURATION);
    }
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(new Style.Builder().fromUri(STYLES_TO_CYCLE_THROUGH[styleCycleIndex]),
        this::enableLocationComponent);
  }

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationComponent(@NonNull Style loadedMapStyle) {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Build a `LocationComponentOptions` object to use when building
      // a `LocationComponentActivationOptions` object.
      LocationComponentOptions customLocationComponentOptions =
          buildLocationComponentOptions(
              LOCATION_CIRCLE_PULSE_COLOR,
              LOCATION_CIRCLE_PULSE_DURATION)
              .pulseEnabled(true)
              .build();

      // Get an instance of the component
      locationComponent = mapboxMap.getLocationComponent();

      LocationComponentActivationOptions locationComponentActivationOptions =
          buildLocationComponentActivationOptions(loadedMapStyle, customLocationComponentOptions);

      // Activate with options
      locationComponent.activateLocationComponent(locationComponentActivationOptions);

      // Enable to make component visible
      locationComponent.setLocationComponentEnabled(true);

      // Set the component's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING);

      // Set the component's render mode
      locationComponent.setRenderMode(RenderMode.NORMAL);

      locationComponent.forceLocationUpdate(lastLocation);

      initCustomizationButtonClickListeners();

      findViewById(R.id.next_style_fab).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          styleCycleIndex++;
          if (styleCycleIndex == STYLES_TO_CYCLE_THROUGH.length) {
            styleCycleIndex = 0;
          }
          mapboxMap.setStyle(STYLES_TO_CYCLE_THROUGH[styleCycleIndex]);
        }
      });
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  /**
   * Build a {@link LocationComponentOptions#builder(Context)} that includes various pulsing
   * LocationComponent customization.
   *
   * @param pulsingCircleColor The color of the pulsing circle UI
   * @param pulsingCircleDuration The duration of a single pulse
   * @return {@link LocationComponentOptions#builder(Context)}
   */
  private LocationComponentOptions.Builder buildLocationComponentOptions(int pulsingCircleColor,
                                                                         float pulsingCircleDuration
  ) {
    currentPulseDuration = pulsingCircleDuration;
    return LocationComponentOptions.builder(this)
        .layerBelow(LAYER_BELOW_ID)
        .pulseFadeEnabled(DEFAULT_LOCATION_CIRCLE_PULSE_FADE_MODE)
        .pulseInterpolator(DEFAULT_LOCATION_CIRCLE_INTERPOLATOR_PULSE_MODE)
        .pulseColor(pulsingCircleColor)
        .pulseAlpha(DEFAULT_LOCATION_CIRCLE_PULSE_ALPHA)
        .pulseSingleDuration(pulsingCircleDuration)
        .pulseMaxRadius(DEFAULT_LOCATION_CIRCLE_PULSE_RADIUS);
  }

  /**
   * Apply new styling settings to the {@link LocationComponent}
   *
   * @param newPulsingDuration The new duration of a single pulse
   * @param newPulsingColor The new color of the pulsing circle UI
   */
  @SuppressLint("MissingPermission")
  private void setNewLocationComponentOptions(float newPulsingDuration,
                                              int newPulsingColor) {
    mapboxMap.getStyle(style -> locationComponent.applyStyle(
        buildLocationComponentOptions(
            newPulsingColor,
            newPulsingDuration)
            .pulseEnabled(true)
            .build()));
  }

  /**
   * Build a {@link LocationComponentActivationOptions} object to activate the
   * {@link LocationComponent}.
   *
   * @param style a Maps SDK map {@link Style}
   * @param locationComponentOptions a fully built {@link LocationComponentOptions} object
   * @return a {@link LocationComponentActivationOptions} object
   */
  private LocationComponentActivationOptions buildLocationComponentActivationOptions(
      @NonNull Style style,
      @NonNull LocationComponentOptions locationComponentOptions) {
    return LocationComponentActivationOptions
        .builder(this, style)
        .locationComponentOptions(locationComponentOptions)
        .useDefaultLocationEngine(true)
        .locationEngineRequest(new LocationEngineRequest.Builder(750)
            .setFastestInterval(750)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .build())
        .build();
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
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
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

  private void showDurationListDialog() {
    List<String> modes = new ArrayList<>();
    modes.add(String.format("%sms", String.valueOf(DEFAULT_LOCATION_CIRCLE_PULSE_DURATION_MS)));
    modes.add(String.format("%sms", String.valueOf(SECOND_LOCATION_CIRCLE_PULSE_DURATION_MS)));
    modes.add(String.format("%sms", String.valueOf(THIRD_LOCATION_CIRCLE_PULSE_DURATION_MS)));
    ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, modes);
    ListPopupWindow listPopup = new ListPopupWindow(this);
    listPopup.setAdapter(profileAdapter);
    listPopup.setAnchorView(pulsingCircleDurationButton);
    listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
      String selectedMode = modes.get(position);
      pulsingCircleDurationButton.setText(selectedMode);
      if (selectedMode.contentEquals(String.format("%sms",
          String.valueOf(DEFAULT_LOCATION_CIRCLE_PULSE_DURATION_MS)))) {
        LOCATION_CIRCLE_PULSE_DURATION = DEFAULT_LOCATION_CIRCLE_PULSE_DURATION_MS;
        setNewLocationComponentOptions(DEFAULT_LOCATION_CIRCLE_PULSE_DURATION_MS, LOCATION_CIRCLE_PULSE_COLOR);
      } else if (selectedMode.contentEquals(String.format("%sms",
          String.valueOf(SECOND_LOCATION_CIRCLE_PULSE_DURATION_MS)))) {
        LOCATION_CIRCLE_PULSE_DURATION = SECOND_LOCATION_CIRCLE_PULSE_DURATION_MS;
        setNewLocationComponentOptions(SECOND_LOCATION_CIRCLE_PULSE_DURATION_MS, LOCATION_CIRCLE_PULSE_COLOR);
      } else if (selectedMode.contentEquals(String.format("%sms",
          String.valueOf(THIRD_LOCATION_CIRCLE_PULSE_DURATION_MS)))) {
        LOCATION_CIRCLE_PULSE_DURATION = THIRD_LOCATION_CIRCLE_PULSE_DURATION_MS;
        setNewLocationComponentOptions(THIRD_LOCATION_CIRCLE_PULSE_DURATION_MS, LOCATION_CIRCLE_PULSE_COLOR);
      }
      listPopup.dismiss();
    });
    listPopup.show();
  }

  private void showColorListDialog() {
    ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, colorOptionList);
    ListPopupWindow listPopup = new ListPopupWindow(this);
    listPopup.setAdapter(profileAdapter);
    listPopup.setAnchorView(pulsingCircleColorButton);
    listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
      String selectedTrackingType = colorOptionList.get(position);
      pulsingCircleColorButton.setText(selectedTrackingType);
      if (colorHashMap.get(selectedTrackingType) != null) {
        LOCATION_CIRCLE_PULSE_COLOR = colorHashMap.get(selectedTrackingType);
        setNewLocationComponentOptions(currentPulseDuration, LOCATION_CIRCLE_PULSE_COLOR);
      }
      listPopup.dismiss();
    });
    listPopup.show();
  }

  private void initCustomizationButtonClickListeners() {
    pulsingCircleDurationButton.setOnClickListener(v -> {
      if (locationComponent == null) {
        return;
      }
      showDurationListDialog();
    });
    pulsingCircleColorButton.setOnClickListener(v -> {
      if (locationComponent == null) {
        return;
      }
      showColorListDialog();
    });
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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

  @SuppressLint("MissingPermission")
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
    if (locationComponent != null) {
      outState.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
      outState.putInt(SAVED_STATE_LOCATION_CIRCLE_PULSE_COLOR, LOCATION_CIRCLE_PULSE_COLOR);
      outState.putFloat(SAVED_STATE_LOCATION_CIRCLE_PULSE_DURATION, LOCATION_CIRCLE_PULSE_DURATION);
    }
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
