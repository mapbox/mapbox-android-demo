package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.gson.JsonObject;
import com.mapbox.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;

public class PlacesPluginActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private CarmenFeature home;
  private CarmenFeature work;
  private String geojsonSourceLayerId = "geojsonSourceLayerId";
  private String symbolIconId = "symbolIconId";

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
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    PlacesPluginActivity.this.mapboxMap = mapboxMap;
    initSearchFab();
    addUserLocations();

    // Add the symbol layer icon to map for future use
    Bitmap icon = BitmapFactory.decodeResource(
      PlacesPluginActivity.this.getResources(), R.drawable.blue_marker_view);
    mapboxMap.addImage(symbolIconId, icon);

    // Create an empty GeoJSON source using the empty feature collection
    setUpSource();

    // Set up a new symbol layer for displaying the searched location's feature coordinates
    setupLayer();
  }

  private void initSearchFab() {
    FloatingActionButton searchFab = findViewById(R.id.fab_location_search);
    searchFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
          .accessToken(Mapbox.getAccessToken())
          .placeOptions(PlaceOptions.builder()
            .backgroundColor(Color.parseColor("#EEEEEE"))
            .limit(10)
            .addInjectedFeature(home)
            .addInjectedFeature(work)
            .build(PlaceOptions.MODE_CARDS))
          .build(PlacesPluginActivity.this);
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
      }
    });
  }

  private void addUserLocations() {
    home = CarmenFeature.builder().text("Mapbox SF Office")
      .geometry(Point.fromLngLat(-122.399854, 37.7884400))
      .placeName("85 2nd St, San Francisco, CA")
      .id("mapbox-sf")
      .properties(new JsonObject())
      .build();

    work = CarmenFeature.builder().text("Mapbox DC Office")
      .placeName("740 15th Street NW, Washington DC")
      .geometry(Point.fromLngLat(-77.0338348, 38.899750))
      .id("mapbox-dc")
      .properties(new JsonObject())
      .build();
  }

  private void setUpSource() {
    GeoJsonSource geoJsonSource = new GeoJsonSource(geojsonSourceLayerId);
    mapboxMap.addSource(geoJsonSource);
  }

  private void setupLayer() {
    SymbolLayer selectedLocationSymbolLayer = new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId);
    selectedLocationSymbolLayer.withProperties(PropertyFactory.iconImage(symbolIconId));
    mapboxMap.addLayer(selectedLocationSymbolLayer);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

      // Retrieve selected location's CarmenFeature
      CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

      // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above
      FeatureCollection featureCollection = FeatureCollection.fromFeatures(
        new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())});

      // Retrieve and update the source designated for showing a selected location's symbol layer icon
      GeoJsonSource source = mapboxMap.getSourceAs(geojsonSourceLayerId);
      if (source != null) {
        source.setGeoJson(featureCollection);
      }

      // Move map camera to the selected location
      CameraPosition newCameraPosition = new CameraPosition.Builder()
        .target(new LatLng(getFeatureLat(selectedCarmenFeature), getFeatureLong(selectedCarmenFeature)))
        .zoom(14)
        .build();
      mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition),4000);
    }
  }

  // Extracts latitude from single GeoJSON Feature
  private double getFeatureLat(CarmenFeature singleFeature) {
    String[] coordinateValues = singleFeature.geometry()
      .coordinates().toString().replace("Position [", "")
      .replace(", altitude=NaN]", "").replace("longitude=", "")
      .replace("]", "")
      .split(", ");
    return Double.valueOf(coordinateValues[1].replace("latitude=", ""));
  }

  // Extracts longitude from single GeoJSON Feature
  private double getFeatureLong(CarmenFeature singleFeature) {
    String[] coordinateValues = singleFeature.geometry()
      .coordinates().toString().replace("Position [", "")
      .replace(", altitude=NaN]", "").replace("longitude=", "")
      .replace("[", "")
      .split(", ");
    return Double.valueOf(coordinateValues[0]);
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}