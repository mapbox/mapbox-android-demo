package com.mapbox.mapboxandroiddemo.labs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.geocoding.v5.models.GeocodingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationPickerActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private LocationServices locationServices;
  private Marker droppedMarker;
  private ImageView hoveringMarker;
  private Button selectLocationButton;
  private boolean initialCameraPositonSet = false;

  private static final int PERMISSIONS_LOCATION = 0;
  private static final String TAG = "LocationPickerActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_navigation_location_picker);

    // Get the location services object for later use.
    locationServices = LocationServices.getLocationServices(LocationPickerActivity.this);

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
        if (!locationServices.areLocationPermissionsGranted()) {
          ActivityCompat.requestPermissions(LocationPickerActivity.this, new String[]{
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
        } else {
          setInitialCamera();
        }
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
      public void onClick(View v) {
        if (map != null) {
          if (droppedMarker == null) {
            // We first find where the hovering marker position is relative to the map.
            // Then we set the visibility to gone.
            float x = hoveringMarker.getLeft() + (hoveringMarker.getWidth() / 2);
            float y = hoveringMarker.getBottom();
            float[] coords = new float[]{x, y};
            LatLng latLng = map.getProjection().fromScreenLocation(new PointF(coords[0], coords[1]));
            hoveringMarker.setVisibility(View.GONE);

            // Transform the appearance of the button to become the cancel button
            selectLocationButton.setBackgroundColor(ContextCompat.getColor(LocationPickerActivity.this, R.color.colorAccent));
            selectLocationButton.setText("Cancel");

            // Create the marker icon the dropped marker will be using.
            IconFactory iconFactory = IconFactory.getInstance(LocationPickerActivity.this);
            Drawable iconDrawable = ContextCompat.getDrawable(LocationPickerActivity.this, R.drawable.red_marker);
            Icon icon = iconFactory.fromDrawable(iconDrawable);

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
            selectLocationButton.setBackgroundColor(ContextCompat.getColor(LocationPickerActivity.this, R.color.colorPrimary));
            selectLocationButton.setText("Select a location");

            // Lastly, set the hovering marker back to visible.
            hoveringMarker.setVisibility(View.VISIBLE);
            droppedMarker = null;
          }
        }
      }
    });
  }// End onCreate

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
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
    if (locationServices.getLastLocation() != null) {
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationServices.getLastLocation()), 16));
      initialCameraPositonSet = true;
    }

    // This location listener is used in a very specific use case. If the users last location is
    // unknown we wait till the GPS locates them and position the camera above.
    locationServices.addLocationListener(new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        if (location != null && !initialCameraPositonSet) {
          // Move the map camera to where the user location is
          map.setCameraPosition(new CameraPosition.Builder()
              .target(new LatLng(location))
              .zoom(16)
              .build());

          // Set to true so we aren't setting the camera every time location is updated
          initialCameraPositonSet = true;
        }
      }
    });
    // Enable the location layer on the map and track the user location until they perform a
    // map gesture.
    map.setMyLocationEnabled(true);
    map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
  }// End setInitialCamera

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
              droppedMarker.setSnippet("No results");
              map.selectMarker(droppedMarker);
            }
          }
        }

        @Override
        public void onFailure(Call<GeocodingResponse> call, Throwable t) {
          Log.e(TAG, "Geocoding Failure: " + t.getMessage());
        }
      });
    } catch (ServicesException e) {
      Log.e(TAG, "Error geocoding: " + e.toString());
      e.printStackTrace();
    }
  }// reverseGeocode

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_LOCATION: {
        if (grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          setInitialCamera();
        }
      }
    }
  }
}
