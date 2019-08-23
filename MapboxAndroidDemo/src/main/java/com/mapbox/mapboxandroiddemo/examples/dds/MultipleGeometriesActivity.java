package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;

public class MultipleGeometriesActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private static final String GEOJSON_SOURCE_ID = "GEOJSONFILE";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_multiple_geometries);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        createGeoJsonSource(style);
        addPolygonLayer(style);
        addPointsLayer(style);
      }
    });
  }

  private void createGeoJsonSource(@NonNull Style loadedMapStyle) {
    try {
      // Load data from GeoJSON file in the assets folder
      loadedMapStyle.addSource(new GeoJsonSource(GEOJSON_SOURCE_ID,
        new URI("asset://fake_norway_campsites.geojson")));
    } catch (URISyntaxException exception) {
      Timber.d(exception);
    }
  }

  private void addPolygonLayer(@NonNull Style loadedMapStyle) {
    // Create and style a FillLayer that uses the Polygon Feature's coordinates in the GeoJSON data
    FillLayer countryPolygonFillLayer = new FillLayer("polygon", GEOJSON_SOURCE_ID);
    countryPolygonFillLayer.setProperties(
      PropertyFactory.fillColor(Color.RED),
      PropertyFactory.fillOpacity(.4f));
    countryPolygonFillLayer.setFilter(eq(literal("$type"), literal("Polygon")));
    loadedMapStyle.addLayer(countryPolygonFillLayer);
  }

  private void addPointsLayer(@NonNull Style loadedMapStyle) {
    // Create and style a CircleLayer that uses the Point Features' coordinates in the GeoJSON data
    CircleLayer individualCirclesLayer = new CircleLayer("points", GEOJSON_SOURCE_ID);
    individualCirclesLayer.setProperties(
      PropertyFactory.circleColor(Color.YELLOW),
      PropertyFactory.circleRadius(3f));
    individualCirclesLayer.setFilter(eq(literal("$type"), literal("Point")));
    loadedMapStyle.addLayer(individualCirclesLayer);
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
