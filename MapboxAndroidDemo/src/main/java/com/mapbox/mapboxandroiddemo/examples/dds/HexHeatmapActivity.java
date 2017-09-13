package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import static com.mapbox.mapboxsdk.style.functions.Function.property;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.categorical;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Creates a hexgrid-based vector heatmap on the specified map.
 */
public class HexHeatmapActivity extends AppCompatActivity {

  private MapView mapView;
  private static final LatLngBounds LOCKED_MAP_CAMERA_BOUNDS_TO_NYC = new LatLngBounds.Builder()
    .include(new LatLng(39.68392799015035, -75.04728500751165))
    .include(new LatLng(41.87764500765852,
      -72.91058699000139)).build();

  private MapboxMap mapboxMap;
  private Layer layer;
  private Source source;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_hex_heatmap);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        HexHeatmapActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setLatLngBoundsForCameraTarget(LOCKED_MAP_CAMERA_BOUNDS_TO_NYC);

      }
    });
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

  class HexGridHeatMap {

    private MapboxMap mapboxMap;
    private String addBefore;
    private String layername = "hexgrid-heatmap";
    private int intensity = 8;
    private double spread = 0.1;
    private int minCellIntensity = 0; // Drop out cells that have less than this intensity
    private int maxPointIntensity = 20; // Don't let a single point have a greater weight than this
    private int cellDensity = 1;

    private boolean calculatingGrid = false;
    private boolean recalcWhenReady = false;

    public HexGridHeatMap(MapboxMap mapboxMap, String addBefore, String layername) {
      if (layername == null) {
        layername = "hexgrid-heatmap";
      }
      this.mapboxMap = mapboxMap;
      this.addBefore = addBefore;
      this.layername = layername;

    }

    private void setUpGeoJsonSource() {
      GeoJsonSource geoJsonSource = new GeoJsonSource("GeoJSON data");
      mapboxMap.addSource(geoJsonSource);
    }

    private void setUpHexGridLayer(String layerName, String addBefore) {
      FillLayer hexFillLayer = new FillLayer(layerName, "GeoJSON data");
      hexFillLayer.setMaxZoom(16);
      hexFillLayer.setProperties(
        PropertyFactory.fillOpacity(1.0f),
        fillColor(
          property("count", categorical(
            stop(0, circleColor(Color.parseColor("#00b9f3"))),
            stop(50, circleColor(Color.parseColor("#4000b9f3"))),
            stop(130, circleColor(Color.parseColor("#4Dffdf00"))),
            stop(200, circleColor(Color.parseColor("#4dff6900"))))
          )
        )
      );
      mapboxMap.addLayer(hexFillLayer);
      layer = mapboxMap.getLayer(layerName);
      source = mapboxMap.getSource("GeoJSON data");
    }


  }


}