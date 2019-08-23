package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use the places plugin to take advantage of Mapbox's location search ("geocoding") capabilities. The plugin
 * automatically makes geocoding requests, has built-in saved locations, includes location picker functionality,
 * and adds beautiful UI into your Android project.
 */
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
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        initSearchFab();

        addUserLocations();

        // Add the symbol layer icon to map for future use
        style.addImage(symbolIconId, BitmapFactory.decodeResource(
          PlacesPluginActivity.this.getResources(), R.drawable.blue_marker_view));

        // Create an empty GeoJSON source using the empty feature collection
        setUpSource(style);

        // Set up a new symbol layer for displaying the searched location's feature coordinates
        setupLayer(style);
      }
    });
  }

  private void initSearchFab() {
    findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
          .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
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
      .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
      .placeName("50 Beale St, San Francisco, CA")
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

  private void setUpSource(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
  }

  private void setupLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
      iconImage(symbolIconId),
      iconOffset(new Float[] {0f, -8f})
    ));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

      // Retrieve selected location's CarmenFeature
      CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

      // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
      // Then retrieve and update the source designated for showing a selected location's symbol layer icon

      if (mapboxMap != null) {
        Style style = mapboxMap.getStyle();
        if (style != null) {
          GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
          if (source != null) {
            source.setGeoJson(FeatureCollection.fromFeatures(
              new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
          }

          // Move map camera to the selected location
          mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
            new CameraPosition.Builder()
              .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                ((Point) selectedCarmenFeature.geometry()).longitude()))
              .zoom(14)
              .build()), 4000);
        }
      }
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
