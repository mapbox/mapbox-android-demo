package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.InputStream;

import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class BathymetryActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final LatLngBounds LAKE_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(44.936236, -85.673450))
    .include(new LatLng(44.932955, -85.669272))
    .build();
  private FeatureCollection featureCollection;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private String geojsonSourceId = "geojsonSourceId";

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
  public void onMapReady(MapboxMap mapboxMap) {
    BathymetryActivity.this.mapboxMap = mapboxMap;

    // Set bounds to Lawrence Lake, Michigan
    mapboxMap.setLatLngBoundsForCameraTarget(LAKE_BOUNDS);

    // Remove lake label layer
    mapboxMap.removeLayer("water-label");

    // Initialize FeatureCollection object for future use with layers
    featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("bathymetry-data.geojson"));

    // Retrieve GeoJSON from local file and add it to the map
    GeoJsonSource geoJsonSource = new GeoJsonSource(geojsonSourceId,
      featureCollection);
    mapboxMap.addSource(geoJsonSource);

    setUpDepthFillLayers();
    setUpDepthNumberSymbolLayer();
  }

  /**
   * Adds a FillLayer and uses data-driven styling to display the lake's areas
   */
  private void setUpDepthFillLayers() {
    FillLayer depthPolygonFillLayer = new FillLayer("DEPTH_POLYGON_FILL_LAYER_ID", geojsonSourceId);
    depthPolygonFillLayer.withProperties(
      fillColor(interpolate(linear(),
        get("depth"),
        stop(5, color(Color.parseColor("#109ED2"))),
        stop(10, color(Color.parseColor("#257491"))),
        stop(15, color(Color.parseColor("#45667C"))),
        stop(30, color(Color.parseColor("#1F3443"))))),
      fillOpacity(.7f));
    // Only display Polygon Features in this layer
    depthPolygonFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    mapboxMap.addLayer(depthPolygonFillLayer);
  }

  /**
   * Adds a SymbolLayer to display the depth of the lake's areas
   */
  private void setUpDepthNumberSymbolLayer() {
    SymbolLayer depthNumberSymbolLayer = new SymbolLayer("DEPTH_NUMBER_SYMBOL_LAYER_ID",
      geojsonSourceId);
    depthNumberSymbolLayer.withProperties(
      textField("{depth}"),
      textSize(17f),
      textColor(Color.WHITE),
      textAllowOverlap(true)
    );
    // Only display Point Features in this layer
    depthNumberSymbolLayer.setFilter(eq(geometryType(), literal("Point")));
    mapboxMap.addLayerAbove(depthNumberSymbolLayer, "DEPTH_POLYGON_FILL_LAYER_ID");
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

  private String loadGeoJsonFromAsset(String filename) {
    try {
      // Load GeoJSON file
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");

    } catch (Exception exception) {
      Log.e("StyleLineActivity", "Exception Loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
    }
  }
}