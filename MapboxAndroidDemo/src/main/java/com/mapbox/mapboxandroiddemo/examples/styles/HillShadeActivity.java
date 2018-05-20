package com.mapbox.mapboxandroiddemo.examples.styles;
// #-code-snippet: hill-shade-activity full-java
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.HillshadeLayer;
import com.mapbox.mapboxsdk.style.sources.RasterDemSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.hillshadeHighlightColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.hillshadeShadowColor;

/**
 * Use terrain data to show hills and use runtime styling to style the hill shading.
 */
public class HillShadeActivity extends AppCompatActivity implements
  OnMapReadyCallback {

  private MapView mapView;
  private static final String LAYER_ID = "hillshade-layer";
  private static final String LAYER_BELOW_ID = "waterway-river-canal-shadow";
  private static final String SOURCE_ID = "hillshade-source";
  private static final String SOURCE_URL = "mapbox://mapbox.terrain-rgb";
  private static final String HILLSHADE_HIGHLIGHT_COLOR = "#008924";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_hillshade);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    // Add hillshade data source to map
    RasterDemSource rasterDemSource = new RasterDemSource(SOURCE_ID, SOURCE_URL);
    mapboxMap.addSource(rasterDemSource);

    // Create and style a hillshade layer to add to the map
    HillshadeLayer hillshadeLayer = new HillshadeLayer(LAYER_ID, SOURCE_ID).withProperties(
      hillshadeHighlightColor(Color.parseColor(HILLSHADE_HIGHLIGHT_COLOR)),
      hillshadeShadowColor(Color.BLACK)
    );

    // Add the hillshade layer to the map
    mapboxMap.addLayerBelow(hillshadeLayer, LAYER_BELOW_ID);
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
// #-end-code-snippet: hill-shade-activity full-java