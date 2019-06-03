package com.mapbox.mapboxandroiddemo.examples.labs;

// #-code-snippet: location-picker-activity full-java

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Drop a marker at a specific location and then perform
 * reverse geocoding to retrieve and display the location's address
 */
public class LocationPickerActivity extends AppCompatActivity implements PermissionsListener,OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Marker droppedMarker;
  private ImageView hoveringMarker;
  private Button selectLocationButton;
  private PermissionsManager permissionsManager;

  private static final String TAG = "LocationPickerActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_location_picker);

    // Initialize the mapboxMap view
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }


  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    LocationPickerActivity.this.mapboxMap = mapboxMap;
    enableLocationPlugin();

    // Toast instructing user to tap on the mapboxMap
    Toast.makeText(
      LocationPickerActivity.this,
      getString(R.string.move_map_instruction),
      Toast.LENGTH_LONG
    ).show();

    // When user is still picking a location, we hover a marker above the mapboxMap in the center.
    // This is done by using an image view with the default marker found in the SDK. You can
    // swap out for your own marker image, just make sure it matches up with the dropped marker.
    hoveringMarker = new ImageView(this);
    hoveringMarker.setImageResource(R.drawable.red_marker);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    hoveringMarker.setLayoutParams(params);
    mapView.addView(hoveringMarker);

    // Button for user to drop marker or to pick marker back up.
    selectLocationButton = findViewById(R.id.select_location_button);
    selectLocationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mapboxMap != null) {
          if (droppedMarker == null) {
            // We first find where the hovering marker position is relative to the mapboxMap.
            // Then we set the visibility to gone.
            float coordinateX = hoveringMarker.getLeft() + (hoveringMarker.getWidth() / 2);
            float coordinateY = hoveringMarker.getBottom();
            float[] coords = new float[] {coordinateX, coordinateY};
            final Point latLng = Point.fromLngLat(mapboxMap.getProjection().fromScreenLocation(
              new PointF(coords[0], coords[1])).getLongitude(), mapboxMap.getProjection().fromScreenLocation(
              new PointF(coords[0], coords[1])).getLatitude());
            hoveringMarker.setVisibility(View.GONE);

            // Transform the appearance of the button to become the cancel button
            selectLocationButton.setBackgroundColor(
              ContextCompat.getColor(LocationPickerActivity.this, R.color.colorAccent));
            selectLocationButton.setText(getString(R.string.location_picker_select_location_button_cancel));

            // Create the marker icon the dropped marker will be using.
            Icon icon = IconFactory.getInstance(LocationPickerActivity.this).fromResource(R.drawable.red_marker);

            // Placing the marker on the mapboxMap as soon as possible causes the illusion
            // that the hovering marker and dropped marker are the same.

            droppedMarker = mapboxMap.addMarker(new MarkerOptions()
              .position(new LatLng(latLng.latitude(), latLng.longitude())));

            // Finally we get the geocoding information
            reverseGeocode(latLng);
          } else {
            // When the marker is dropped, the user has clicked the button to cancel.
            // Therefore, we pick the marker back up.
            mapboxMap.removeMarker(droppedMarker);

            // Switch the button apperance back to select a location.
            selectLocationButton.setBackgroundColor(
              ContextCompat.getColor(LocationPickerActivity.this, R.color.colorPrimary));
            selectLocationButton.setText(getString(R.string.location_picker_select_location_button_select));

            // Lastly, set the hovering marker back to visible.
            hoveringMarker.setVisibility(View.VISIBLE);
            droppedMarker = null;
          }
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
  @SuppressWarnings( {"MissingPermission"})
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
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
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
      enableLocationPlugin();
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void reverseGeocode(final Point point) {
    // This method is used to reverse geocode where the user has dropped the marker.
    try {

      MapboxGeocoding client = MapboxGeocoding.builder()
        .accessToken(getString(R.string.access_token))
        .query(Point.fromLngLat(point.longitude(), point.latitude()))
        .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
        .build();

      client.enqueueCall(new Callback<GeocodingResponse>() {
        @Override
        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

          List<CarmenFeature> results = response.body().features();
          if (results.size() > 0) {
            CarmenFeature feature = results.get(0);
            // If the geocoder returns a result, we take the first in the list and update
            // the dropped marker snippet with the information. Lastly we open the info
            // window.
            if (droppedMarker != null) {
              droppedMarker.setSnippet(feature.placeName());
              mapboxMap.selectMarker(droppedMarker);
            }

          } else {
            if (droppedMarker != null) {
              droppedMarker.setSnippet(getString(R.string.location_picker_dropped_marker_snippet_no_results));
              mapboxMap.selectMarker(droppedMarker);
            }
          }
        }

        @Override
        public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
          Log.e(TAG, "Geocoding Failure: " + throwable.getMessage());
        }
      });
    } catch (ServicesException servicesException) {
      Log.e(TAG, "Error geocoding: " + servicesException.toString());
      servicesException.printStackTrace();
    }
  } // reverseGeocode

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Create an instance of the plugin. Adding in LocationLayerOptions is also an optional
      // parameter
      LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap);

      // Set the plugin's camera mode
      locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
      getLifecycle().addObserver(locationLayerPlugin);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }
}
// #-end-code-snippet: location-picker-activity full-java