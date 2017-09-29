package com.mapbox.mapboxandroiddemo.examples.mas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.ui.geocoder.GeocoderAutoCompleteView;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.commons.models.Position;

/**
 * Use Mapbox Android Service's geocoder to convert location text into geographic coordinates.
 */
public class GeocodingActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_mas_geocoding);

    // Set up the MapView
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

      }
    });

    // Set up autocomplete widget
    GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.query);
    autocomplete.setAccessToken(Mapbox.getAccessToken());
    autocomplete.setType(GeocodingCriteria.TYPE_POI);
    autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
      @Override
      public void onFeatureClick(CarmenFeature feature) {
        hideOnScreenKeyboard();
        Position position = feature.asPosition();
        updateMap(position.getLatitude(), position.getLongitude());
      }
    });
  }

  private void updateMap(double latitude, double longitude) {
    // Build marker
    map.addMarker(new MarkerOptions()
      .position(new LatLng(latitude, longitude))
      .title(getString(R.string.geocode_activity_marker_options_title)));

    // Animate camera to geocoder result location
    CameraPosition cameraPosition = new CameraPosition.Builder()
      .target(new LatLng(latitude, longitude))
      .zoom(15)
      .build();
    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
  }

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

  private void hideOnScreenKeyboard() {
    try {
      InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
      if (getCurrentFocus() != null) {
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
      }
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }

  }
}