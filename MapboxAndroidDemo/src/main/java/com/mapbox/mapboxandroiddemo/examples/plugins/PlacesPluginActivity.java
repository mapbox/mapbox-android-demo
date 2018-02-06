package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonObject;
import com.mapbox.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.services.commons.geojson.Feature;

public class PlacesPluginActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
  private CarmenFeature home;
  private CarmenFeature work;
  private FloatingActionButton searchFab;
  private String TAG = "PlacesPluginActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_places_plugin);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        PlacesPluginActivity.this.mapboxMap = mapboxMap;

        // Customize map with markers, polylines, etc.

        addUserLocations();

        searchFab = findViewById(R.id.fab_location_search);
        searchFab.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
              .accessToken(Mapbox.getAccessToken())
              .placeOptions(PlaceOptions.builder()
                .backgroundColor(Color.parseColor("#EEEEEE"))
                .addInjectedFeature(home)
                .addInjectedFeature(work)
                .limit(10)
                .build(PlaceOptions.MODE_CARDS))
              .build(PlacesPluginActivity.this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
          }
        });
      }
    });
  }

  private void addUserLocations() {
    home = CarmenFeature.builder().text("Directions to Home")
      .placeName("740 15th St NW")
      .id("Mapbox DC")
      .properties(new JsonObject())
      .build();

    work = CarmenFeature.builder().text("Directions to Work")
      .placeName("85 2nd St")
      .id("Mapbox SF")
      .properties(new JsonObject())
      .build();
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
      CarmenFeature feature = PlaceAutocomplete.getPlace(data);
      CameraPosition newCameraPosition = new CameraPosition.Builder()
        .target(new LatLng(getFeatureLat(feature), getFeatureLong(feature)))
        .build();
      mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
    }
  }

  private double getFeatureLat(CarmenFeature singleFeature) {
    String[] coordinateValues = singleFeature.geometry()
      .coordinates().toString().replace("Position [", "")
      .replace(", altitude=NaN]", "").replace("longitude=", "")
      .replace("]","")
      .split(", ");
    return Double.valueOf(coordinateValues[1].replace("latitude=", ""));
  }

  private double getFeatureLong(CarmenFeature singleFeature) {
    String[] coordinateValues = singleFeature.geometry()
      .coordinates().toString().replace("Position [", "")
      .replace(", altitude=NaN]", "").replace("longitude=", "")
      .replace("[","")
      .split(", ");
    return Double.valueOf(coordinateValues[0]);
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
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}