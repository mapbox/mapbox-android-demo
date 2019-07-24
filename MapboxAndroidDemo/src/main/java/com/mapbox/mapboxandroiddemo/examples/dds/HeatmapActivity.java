package com.mapbox.mapboxandroiddemo.examples.dds;

import android.os.Bundle;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;

public class HeatmapActivity extends AppCompatActivity {

  private static final String EARTHQUAKE_SOURCE_URL = "https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson";
  private static final String EARTHQUAKE_SOURCE_ID = "earthquakes";
  private static final String HEATMAP_LAYER_ID = "earthquakes-heat";
  private static final String HEATMAP_LAYER_SOURCE = "earthquakes";
  private static final String CIRCLE_LAYER_ID = "earthquakes-circle";

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_heatmap);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        HeatmapActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            addEarthquakeSource(style);
            addHeatmapLayer(style);
            addCircleLayer(style);
          }
        });
      }
    });
  }

  private void addEarthquakeSource(@NonNull Style loadedMapStyle) {
    try {
      loadedMapStyle.addSource(new GeoJsonSource(EARTHQUAKE_SOURCE_ID, new URI(EARTHQUAKE_SOURCE_URL)));
    } catch (URISyntaxException uriSyntaxException) {
      Timber.e(uriSyntaxException, "That's not an url... ");
    }
  }

  private void addHeatmapLayer(@NonNull Style loadedMapStyle) {
    HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, EARTHQUAKE_SOURCE_ID);
    layer.setMaxZoom(9);
    layer.setSourceLayer(HEATMAP_LAYER_SOURCE);
    layer.setProperties(

      // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
      // Begin color ramp at 0-stop with a 0-transparency color
      // to create a blur-like effect.
      heatmapColor(
        interpolate(
          linear(), heatmapDensity(),
          literal(0), rgba(33, 102, 172, 0),
          literal(0.2), rgb(103, 169, 207),
          literal(0.4), rgb(209, 229, 240),
          literal(0.6), rgb(253, 219, 199),
          literal(0.8), rgb(239, 138, 98),
          literal(1), rgb(178, 24, 43)
        )
      ),

      // Increase the heatmap weight based on frequency and property magnitude
      heatmapWeight(
        interpolate(
          linear(), get("mag"),
          stop(0, 0),
          stop(6, 1)
        )
      ),

      // Increase the heatmap color weight weight by zoom level
      // heatmap-intensity is a multiplier on top of heatmap-weight
      heatmapIntensity(
        interpolate(
          linear(), zoom(),
          stop(0, 1),
          stop(9, 3)
        )
      ),

      // Adjust the heatmap radius by zoom level
      heatmapRadius(
        interpolate(
          linear(), zoom(),
          stop(0, 2),
          stop(9, 20)
        )
      ),

      // Transition from heatmap to circle layer by zoom level
      heatmapOpacity(
        interpolate(
          linear(), zoom(),
          stop(7, 1),
          stop(9, 0)
        )
      )
    );

    loadedMapStyle.addLayerAbove(layer, "waterway-label");
  }

  private void addCircleLayer(@NonNull Style loadedMapStyle) {
    CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, EARTHQUAKE_SOURCE_ID);
    circleLayer.setProperties(

      // Size circle radius by earthquake magnitude and zoom level
      circleRadius(
        interpolate(
          linear(), zoom(),
          literal(7), interpolate(
            linear(), get("mag"),
            stop(1, 1),
            stop(6, 4)
          ),
          literal(16), interpolate(
            linear(), get("mag"),
            stop(1, 5),
            stop(6, 50)
          )
        )
      ),

      // Color circle by earthquake magnitude
      circleColor(
        interpolate(
          linear(), get("mag"),
          literal(1), rgba(33, 102, 172, 0),
          literal(2), rgb(103, 169, 207),
          literal(3), rgb(209, 229, 240),
          literal(4), rgb(253, 219, 199),
          literal(5), rgb(239, 138, 98),
          literal(6), rgb(178, 24, 43)
        )
      ),

      // Transition from heatmap to circle layer by zoom level
      circleOpacity(
        interpolate(
          linear(), zoom(),
          stop(7, 0),
          stop(8, 1)
        )
      ),
      circleStrokeColor("white"),
      circleStrokeWidth(1.0f)
    );

    loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }
}
