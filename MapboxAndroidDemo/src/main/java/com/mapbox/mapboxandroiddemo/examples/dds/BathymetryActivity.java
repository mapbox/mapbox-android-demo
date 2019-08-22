package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class BathymetryActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
  private static final LatLngBounds LAKE_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(44.936236, -85.673450))
    .include(new LatLng(44.932955, -85.669272))
    .build();
  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_bathymetry);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Set bounds to Lawrence Lake, Michigan
        mapboxMap.setLatLngBoundsForCameraTarget(LAKE_BOUNDS);

        // Remove lake label layer
        style.removeLayer("water-label");

        try {
          // Retrieve GeoJSON from local file and add it to the map's style
          style.addSource(new GeoJsonSource(GEOJSON_SOURCE_ID, new URI("asset://bathymetry-data.geojson")));
        } catch (URISyntaxException exception) {
          Timber.d(exception);
        }

        setUpDepthFillLayers(style);
        setUpDepthNumberSymbolLayer(style);
      }
    });
  }

  /**
   * Adds a FillLayer and uses data-driven styling to display the lake's areas
   */
  private void setUpDepthFillLayers(@NonNull Style loadedMapStyle) {
    FillLayer depthPolygonFillLayer = new FillLayer("DEPTH_POLYGON_FILL_LAYER_ID", GEOJSON_SOURCE_ID);
    depthPolygonFillLayer.withProperties(
      fillColor(interpolate(linear(),
        get("depth"),
        stop(5, rgb(16, 158, 210)),
        stop(10, rgb(37, 116, 145)),
        stop(15, rgb(69, 102, 124)),
        stop(30, rgb(31, 52, 67)))),
      fillOpacity(.7f));
    // Only display Polygon Features in this layer
    depthPolygonFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    loadedMapStyle.addLayer(depthPolygonFillLayer);
  }

  /**
   * Adds a SymbolLayer to display the depth of the lake's areas
   */
  private void setUpDepthNumberSymbolLayer(@NonNull Style loadedMapStyle) {
    SymbolLayer depthNumberSymbolLayer = new SymbolLayer("DEPTH_NUMBER_SYMBOL_LAYER_ID",
        GEOJSON_SOURCE_ID);
    depthNumberSymbolLayer.withProperties(
      textField("{depth}"),
      textSize(17f),
      textColor(Color.WHITE),
      textAllowOverlap(true)
    );
    // Only display Point Features in this layer
    depthNumberSymbolLayer.setFilter(eq(geometryType(), literal("Point")));
    loadedMapStyle.addLayerAbove(depthNumberSymbolLayer, "DEPTH_POLYGON_FILL_LAYER_ID");
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