package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.PointF;
import android.location.Location;
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

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.services.commons.models.Position;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Drop a marker at a specific location and then perform
 * reverse geocoding to retrieve and display the location's address
 */
public class LocationPickerActivity extends AppCompatActivity implements PermissionsListener {

  private MapView mapView;
  private MapboxMap map;
  private LocationEngine locationEngine;
  private Marker droppedMarker;
  private ImageView hoveringMarker;
  private Button selectLocationButton;
  private LocationEngineListener locationEngineListener;
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

    // Get the location engine object for later use.
    locationEngine = new LocationSource(this);
    locationEngine.activate();

    // Initialize the map view
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        // Once map is ready, we want to position the camera above the user location. We
        // first check that the user has granted the location permission, then we call
        // setInitialCamera.
        permissionsManager = new PermissionsManager(LocationPickerActivity.this);
        if (!PermissionsManager.areLocationPermissionsGranted(LocationPickerActivity.this)) {
          permissionsManager.requestLocationPermissions(LocationPickerActivity.this);
        } else {
          setInitialCamera();
        }

        // Toast instructing user to tap on the map
        Toast.makeText(
          LocationPickerActivity.this,
          getString(R.string.move_map_instruction),
          Toast.LENGTH_LONG
        ).show();

      }
    });

    // When user is still picking a location, we hover a marker above the map in the center.
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
    selectLocationButton = (Button) findViewById(R.id.select_location_button);
    selectLocationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (map != null) {
          if (droppedMarker == null) {
            // We first find where the hovering marker position is relative to the map.
            // Then we set the visibility to gone.
            float coordinateX = hoveringMarker.getLeft() + (hoveringMarker.getWidth() / 2);
            float coordinateY = hoveringMarker.getBottom();
            float[] coords = new float[] {coordinateX, coordinateY};
            final LatLng latLng = map.getProjection().fromScreenLocation(new PointF(coords[0], coords[1]));
            hoveringMarker.setVisibility(View.GONE);

            // Transform the appearance of the button to become the cancel button
            selectLocationButton.setBackgroundColor(
              ContextCompat.getColor(LocationPickerActivity.this, R.color.colorAccent));
            selectLocationButton.setText(getString(R.string.location_picker_select_location_button_cancel));

            // Create the marker icon the dropped marker will be using.
            Icon icon = IconFactory.getInstance(LocationPickerActivity.this).fromResource(R.drawable.red_marker);

            // Placing the marker on the map as soon as possible causes the illusion
            // that the hovering marker and dropped marker are the same.
            droppedMarker = map.addMarker(new MarkerViewOptions().position(latLng).icon(icon));

            // Finally we get the geocoding information
            reverseGeocode(latLng);
          } else {
            // When the marker is dropped, the user has clicked the button to cancel.
            // Therefore, we pick the marker back up.
            map.removeMarker(droppedMarker);

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
  } // End onCreate

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
    if (locationEngine != null && locationEngineListener != null) {
      locationEngine.activate();
      locationEngine.addLocationEngineListener(locationEngineListener);
      locationEngine.requestLocationUpdates();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
    if (locationEngine != null && locationEngineListener != null) {
      locationEngine.removeLocationUpdates();
      locationEngine.removeLocationEngineListener(locationEngineListener);
      locationEngine.deactivate();
    }
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

  private void setInitialCamera() {
    // Method is used to set the initial map camera position. Should only be called once when
    // the map is ready. We first try using the users last location so we can quickly set the
    // camera as fast as possible.
    if (locationEngine.getLastLocation() != null) {
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationEngine.getLastLocation()), 16));
    }

    // This location listener is used in a very specific use case. If the users last location is
    // unknown we wait till the GPS locates them and position the camera above.
    locationEngineListener = new LocationEngineListener() {
      @Override
      public void onConnected() {
        locationEngine.requestLocationUpdates();
      }

      @Override
      public void onLocationChanged(Location location) {
        if (location != null) {
          // Move the map camera to where the user location is
          map.setCameraPosition(new CameraPosition.Builder()
            .target(new LatLng(location))
            .zoom(16)
            .build());
          locationEngine.removeLocationEngineListener(this);
        }
      }
    };
    locationEngine.addLocationEngineListener(locationEngineListener);
    // Enable the location layer on the map and track the user location until they perform a
    // map gesture.
    map.setMyLocationEnabled(true);
    map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
  } // End setInitialCamera

  private void reverseGeocode(final LatLng point) {
    // This method is used to reverse geocode where the user has dropped the marker.
    try {
      MapboxGeocoding client = new MapboxGeocoding.Builder()
        .setAccessToken(getString(R.string.access_token))
        .setCoordinates(Position.fromCoordinates(point.getLongitude(), point.getLatitude()))
        .setGeocodingType(GeocodingCriteria.TYPE_ADDRESS)
        .build();

      client.enqueueCall(new Callback<GeocodingResponse>() {
        @Override
        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

          List<CarmenFeature> results = response.body().getFeatures();
          if (results.size() > 0) {
            CarmenFeature feature = results.get(0);
            // If the geocoder returns a result, we take the first in the list and update
            // the dropped marker snippet with the information. Lastly we open the info
            // window.
            if (droppedMarker != null) {
              droppedMarker.setSnippet(feature.getPlaceName());
              map.selectMarker(droppedMarker);
            }

          } else {
            if (droppedMarker != null) {
              droppedMarker.setSnippet(getString(R.string.location_picker_dropped_marker_snippet_no_results));
              map.selectMarker(droppedMarker);
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
      setInitialCamera();
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted,
        Toast.LENGTH_LONG).show();
      finish();
    }
  }
}
