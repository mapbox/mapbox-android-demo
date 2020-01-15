package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.turf.TurfConstants.UNIT_METERS;

/**
 * Use {@link com.mapbox.turf.TurfMeasurement#distance(Point, Point)} to calculate a straight line distance
 * ("as the crow flies") between the device location and the map camera's target.
 */
public class StraightLineDistanceMapMovementActivity extends AppCompatActivity implements
  PermissionsListener, MapboxMap.OnCameraMoveListener {

  private static final String DISTANCE_SOURCE_ID = "DISTANCE_SOURCE_ID";
  private static final String DISTANCE_LINE_LAYER_ID = "DISTANCE_LINE_LAYER_ID";

  // Adjust private static final variables below to change the example's UI
  private static final int LINE_COLOR = Color.RED;
  private static final float LINE_WIDTH = 2f;

  private PermissionsManager permissionsManager;
  private List<Point> pointList;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private TextView distanceTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_straight_line_distance);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {

        StraightLineDistanceMapMovementActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder()
            .fromUri(Style.DARK)

            // Add the source to the map
            .withSource(new GeoJsonSource(DISTANCE_SOURCE_ID))

            // Style and add the LineLayer to the map.
            .withLayer(new LineLayer(DISTANCE_LINE_LAYER_ID, DISTANCE_SOURCE_ID).withProperties(
              lineColor(LINE_COLOR),
              lineWidth(LINE_WIDTH),
              lineJoin(LINE_JOIN_ROUND))), new Style.OnStyleLoaded() {
                @Override
                  public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);

                    StraightLineDistanceMapMovementActivity.this.mapboxMap
                      .addOnCameraMoveListener(StraightLineDistanceMapMovementActivity.this);

                    distanceTextView = findViewById(R.id.straight_line_distance_textview);
                    distanceTextView.setTextColor(Color.RED);

                    Toast.makeText(StraightLineDistanceMapMovementActivity.this,
                      getString(R.string.move_map_around_instruction), Toast.LENGTH_SHORT).show();
                  }
                }
        );
      }
    });
  }

  @Override
  public void onCameraMove() {
    redrawLine(mapboxMap.getCameraPosition().target);
  }

  private void redrawLine(@NonNull LatLng targetLatLng) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        GeoJsonSource geoJsonSource = style.getSourceAs(DISTANCE_SOURCE_ID);
        if (geoJsonSource != null) {
          pointList = new ArrayList<>();
          Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
          if (lastKnownLocation != null) {

            // Add the start and ending points to the straight line
            Point targetPoint = Point.fromLngLat(targetLatLng.getLongitude(), targetLatLng.getLatitude());
            Point deviceLocationPoint = Point.fromLngLat(lastKnownLocation.getLongitude(),
              lastKnownLocation.getLatitude());
            pointList.add(targetPoint);
            pointList.add(deviceLocationPoint);

            // Update the source with the new LineString line
            geoJsonSource.setGeoJson(LineString.fromLngLats(pointList));

            // Update the TextView with the new straight line distance
            double distanceBetweenDeviceAndTarget = TurfMeasurement.distance(deviceLocationPoint,
              targetPoint, UNIT_METERS);

            distanceTextView.setText(String.format("%s meters", String.valueOf(
              NumberFormat.getNumberInstance(Locale.US).format(distanceBetweenDeviceAndTarget))));
          }
        }
      }
    });
  }

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationComponent(@NonNull Style loadedMapStyle) {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Get an instance of the component
      LocationComponent locationComponent = mapboxMap.getLocationComponent();

      // Activate with options
      locationComponent.activateLocationComponent(
        LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

      // Enable to make component visible
      locationComponent.setLocationComponentEnabled(true);

      // Set the component's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING);

      // Set the component's render mode
      locationComponent.setRenderMode(RenderMode.COMPASS);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
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

  // Add the mapView lifecycle to the activity's lifecycle methods
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
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mapboxMap != null) {
      mapboxMap.removeOnCameraMoveListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}