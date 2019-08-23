package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Detect a tap on a data-driven styled line and add a line behind it for a highlight effect
 */
public class HighlightedLineActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private LineLayer backgroundLineLayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_query_highlighted_line);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        initSource(style);
        initLayers(style);
        mapboxMap.addOnMapClickListener(HighlightedLineActivity.this);
        Toast.makeText(HighlightedLineActivity.this, getString(R.string.tap_on_line), Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Detect whether a linestring was clicked on
        PointF pointf = mapboxMap.getProjection().toScreenLocation(point);
        RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);
        List<Feature> featureList = mapboxMap.queryRenderedFeatures(rectF, "line-layer-id");
        if (featureList.size() > 0) {
          for (Feature feature : featureList) {
            GeoJsonSource source = style.getSourceAs("background-geojson-source-id");
            if (source != null) {
              source.setGeoJson(feature);
              backgroundLineLayer.setProperties(visibility(VISIBLE));
            }
          }
        }
      }
    });
    return true;
  }

  /**
   * Set up the line layer source
   */
  private void initSource(@NonNull Style loadedMapStyle) {
    try {
      loadedMapStyle.addSource(new GeoJsonSource("source-id", new URI("asset://brussels_station_exits.geojson")));
    } catch (URISyntaxException exception) {
      Timber.d(exception);
    }
    loadedMapStyle.addSource(new GeoJsonSource("background-geojson-source-id"));
  }

  /**
   * Set up the main and background LineLayers
   */
  private void initLayers(@NonNull Style loadedMapStyle) {
    // Add the regular LineLayer
    LineLayer routeLineLayer = new LineLayer("line-layer-id", "source-id");
    routeLineLayer.setProperties(
      lineWidth(9f),
      lineColor(Color.BLUE),
      lineCap(LINE_CAP_ROUND),
      lineJoin(LINE_JOIN_ROUND)
    );
    loadedMapStyle.addLayer(routeLineLayer);

    // Add the background LineLayer that will act as the highlighting effect
    backgroundLineLayer = new LineLayer("background-line-layer-id",
      "background-geojson-source-id");
    backgroundLineLayer.setProperties(
      lineWidth(routeLineLayer.getLineWidth().value + 8),
      lineColor(Color.parseColor("#ff8402")),
      lineCap(LINE_CAP_ROUND),
      lineJoin(LINE_JOIN_ROUND),
      visibility(NONE)
    );
    loadedMapStyle.addLayerBelow(backgroundLineLayer, "line-layer-id");
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
      mapboxMap = null;
      backgroundLineLayer = null;
    }
    mapView.onDestroy();
    mapView = null;
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
