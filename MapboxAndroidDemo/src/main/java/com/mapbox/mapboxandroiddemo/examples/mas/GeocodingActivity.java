package com.mapbox.mapboxandroiddemo.examples.mas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;

public class GeocodingActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
    autocomplete.setAccessToken(MapboxAccountManager.getInstance().getAccessToken());
    autocomplete.setType(GeocodingCriteria.TYPE_POI);
    autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
      @Override
      public void OnFeatureClick(CarmenFeature feature) {
        Position position = feature.asPosition();
        updateMap(position.getLatitude(), position.getLongitude());
      }
    });

  }

  private void updateMap(double latitude, double longitude) {
    // Build marker
    map.addMarker(new MarkerOptions()
        .position(new LatLng(latitude, longitude))
        .title("Geocoder result"));

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
}