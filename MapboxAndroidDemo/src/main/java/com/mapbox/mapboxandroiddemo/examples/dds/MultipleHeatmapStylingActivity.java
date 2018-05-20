package com.mapbox.mapboxandroiddemo.examples.dds;
// #-code-snippet: multiple-heatmap-styling-activity full-java
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.InputStream;

import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;

public class MultipleHeatmapStylingActivity extends AppCompatActivity
  implements OnMapReadyCallback {

  private static final String HEATMAP_SOURCE_ID = "HEATMAP_SOURCE_ID";
  private static final String HEATMAP_LAYER_ID = "HEATMAP_LAYER_ID";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private Expression[] listOfHeatmapColors;
  private Expression[] listOfHeatmapRadiusStops;
  private Float[] listOfHeatmapIntensityStops;
  private int index;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    index = 0;
    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));
    setContentView(R.layout.activity_multiple_heatmap_styling);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    MultipleHeatmapStylingActivity.this.mapboxMap = mapboxMap;
    CameraPosition cameraPositionForFragmentMap = new CameraPosition.Builder()
      .target(new LatLng(34.056684, -118.254002))
      .zoom(11.047)
      .build();
    mapboxMap.animateCamera(
      CameraUpdateFactory.newCameraPosition(cameraPositionForFragmentMap), 2600);
    addHeatmapDataSource();
    initHeatmapColors();
    initHeatmapRadiusStops();
    initHeatmapIntensityStops();
    addHeatmapLayer();
    findViewById(R.id.switch_heatmap_style_fab).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        index++;
        if (index == listOfHeatmapColors.length - 1) {
          index = 0;
        }
        if (mapboxMap.getLayer(HEATMAP_LAYER_ID) != null) {
          mapboxMap.getLayer(HEATMAP_LAYER_ID).setProperties(
            heatmapColor(listOfHeatmapColors[index]),
            heatmapRadius(listOfHeatmapRadiusStops[index]),
            heatmapIntensity(listOfHeatmapIntensityStops[index])
          );
        }
      }
    });
  }

  private void addHeatmapDataSource() {
    mapboxMap.addSource(new GeoJsonSource(HEATMAP_SOURCE_ID,
      loadGeoJsonFromAsset("la_heatmap_styling_points.geojson")));
  }

  private void addHeatmapLayer() {
    // Create the heatmap layer
    HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, HEATMAP_SOURCE_ID);

    // Heatmap layer disappears at whatever zoom level is set as the maximum
    layer.setMaxZoom(18);

    layer.setProperties(
      // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
      // Begin color ramp at 0-stop with a 0-transparency color to create a blur-like effect.
      heatmapColor(listOfHeatmapColors[index]),

      // Increase the heatmap color weight weight by zoom level
      // heatmap-intensity is a multiplier on top of heatmap-weight
      heatmapIntensity(listOfHeatmapIntensityStops[index]),

      // Adjust the heatmap radius by zoom level
      heatmapRadius(listOfHeatmapRadiusStops[index]
      ),

      heatmapOpacity(1f)
    );

    // Add the heatmap layer to the map and above the "water-label" layer
    mapboxMap.addLayerAbove(layer, "waterway-label");
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

  private void initHeatmapColors() {
    listOfHeatmapColors = new Expression[] {
      // 0
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.01),
        literal(0.25), rgba(224, 176, 63, 0.5),
        literal(0.5), rgb(247, 252, 84),
        literal(0.75), rgb(186, 59, 30),
        literal(0.9), rgb(255, 0, 0)
      ),
      // 1
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(255, 255, 255, 0.4),
        literal(0.25), rgba(4, 179, 183, 1.0),
        literal(0.5), rgba(204, 211, 61, 1.0),
        literal(0.75), rgba(252, 167, 55, 1.0),
        literal(1), rgba(255, 78, 70, 1.0)
      ),
      // 2
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(12, 182, 253, 0.0),
        literal(0.25), rgba(87, 17, 229, 0.5),
        literal(0.5), rgba(255, 0, 0, 1.0),
        literal(0.75), rgba(229, 134, 15, 0.5),
        literal(1), rgba(230, 255, 55, 0.6)
      ),
      // 3
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(135, 255, 135, 0.2),
        literal(0.5), rgba(255, 99, 0, 0.5),
        literal(1), rgba(47, 21, 197, 0.2)
      ),
      // 4
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(4, 0, 0, 0.2),
        literal(0.25), rgba(229, 12, 1, 1.0),
        literal(0.30), rgba(244, 114, 1, 1.0),
        literal(0.40), rgba(255, 205, 12, 1.0),
        literal(0.50), rgba(255, 229, 121, 1.0),
        literal(1), rgba(255, 253, 244, 1.0)
      ),
      // 5
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.01),
        literal(0.05), rgba(0, 0, 0, 0.05),
        literal(0.4), rgba(254, 142, 2, 0.7),
        literal(0.5), rgba(255, 165, 5, 0.8),
        literal(0.8), rgba(255, 187, 4, 0.9),
        literal(0.95), rgba(255, 228, 173, 0.8),
        literal(1), rgba(255, 253, 244, .8)
      ),
      //6
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.01),
        literal(0.3), rgba(82, 72, 151, 0.4),
        literal(0.4), rgba(138, 202, 160, 1.0),
        literal(0.5), rgba(246, 139, 76, 0.9),
        literal(0.9), rgba(252, 246, 182, 0.8),
        literal(1), rgba(255, 255, 255, 0.8)
      ),

      //7
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.01),
        literal(0.1), rgba(0, 2, 114, .1),
        literal(0.2), rgba(0, 6, 219, .15),
        literal(0.3), rgba(0, 74, 255, .2),
        literal(0.4), rgba(0, 202, 255, .25),
        literal(0.5), rgba(73, 255, 154, .3),
        literal(0.6), rgba(171, 255, 59, .35),
        literal(0.7), rgba(255, 197, 3, .4),
        literal(0.8), rgba(255, 82, 1, 0.7),
        literal(0.9), rgba(196, 0, 1, 0.8),
        literal(0.95), rgba(121, 0, 0, 0.8)
      ),
      // 8
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.01),
        literal(0.1), rgba(0, 2, 114, .1),
        literal(0.2), rgba(0, 6, 219, .15),
        literal(0.3), rgba(0, 74, 255, .2),
        literal(0.4), rgba(0, 202, 255, .25),
        literal(0.5), rgba(73, 255, 154, .3),
        literal(0.6), rgba(171, 255, 59, .35),
        literal(0.7), rgba(255, 197, 3, .4),
        literal(0.8), rgba(255, 82, 1, 0.7),
        literal(0.9), rgba(196, 0, 1, 0.8),
        literal(0.95), rgba(121, 0, 0, 0.8)
      ),
      // 9
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.01),
        literal(0.1), rgba(0, 2, 114, .1),
        literal(0.2), rgba(0, 6, 219, .15),
        literal(0.3), rgba(0, 74, 255, .2),
        literal(0.4), rgba(0, 202, 255, .25),
        literal(0.5), rgba(73, 255, 154, .3),
        literal(0.6), rgba(171, 255, 59, .35),
        literal(0.7), rgba(255, 197, 3, .4),
        literal(0.8), rgba(255, 82, 1, 0.7),
        literal(0.9), rgba(196, 0, 1, 0.8),
        literal(0.95), rgba(121, 0, 0, 0.8)
      ),
      // 10
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.01),
        literal(0.1), rgba(0, 2, 114, .1),
        literal(0.2), rgba(0, 6, 219, .15),
        literal(0.3), rgba(0, 74, 255, .2),
        literal(0.4), rgba(0, 202, 255, .25),
        literal(0.5), rgba(73, 255, 154, .3),
        literal(0.6), rgba(171, 255, 59, .35),
        literal(0.7), rgba(255, 197, 3, .4),
        literal(0.8), rgba(255, 82, 1, 0.7),
        literal(0.9), rgba(196, 0, 1, 0.8),
        literal(0.95), rgba(121, 0, 0, 0.8)
      ),
      // 11
      interpolate(
        linear(), heatmapDensity(),
        literal(0.01), rgba(0, 0, 0, 0.25),
        literal(0.25), rgba(229, 12, 1, .7),
        literal(0.30), rgba(244, 114, 1, .7),
        literal(0.40), rgba(255, 205, 12, .7),
        literal(0.50), rgba(255, 229, 121, .8),
        literal(1), rgba(255, 253, 244, .8)
      )
    };
  }

  private void initHeatmapRadiusStops() {
    listOfHeatmapRadiusStops = new Expression[] {
      // 0
      interpolate(
        linear(), zoom(),
        literal(6), literal(50),
        literal(20), literal(100)
      ),
      // 1
      interpolate(
        linear(), zoom(),
        literal(12), literal(70),
        literal(20), literal(100)
      ),
      // 2
      interpolate(
        linear(), zoom(),
        literal(1), literal(7),
        literal(5), literal(50)
      ),
      // 3
      interpolate(
        linear(), zoom(),
        literal(1), literal(7),
        literal(5), literal(50)
      ),
      // 4
      interpolate(
        linear(), zoom(),
        literal(1), literal(7),
        literal(5), literal(50)
      ),
      // 5
      interpolate(
        linear(), zoom(),
        literal(1), literal(7),
        literal(15), literal(200)
      ),
      // 6
      interpolate(
        linear(), zoom(),
        literal(1), literal(10),
        literal(8), literal(70)
      ),
      // 7
      interpolate(
        linear(), zoom(),
        literal(1), literal(10),
        literal(8), literal(200)
      ),
      // 8
      interpolate(
        linear(), zoom(),
        literal(1), literal(10),
        literal(8), literal(200)
      ),
      // 9
      interpolate(
        linear(), zoom(),
        literal(1), literal(10),
        literal(8), literal(200)
      ),
      // 10
      interpolate(
        linear(), zoom(),
        literal(1), literal(10),
        literal(8), literal(200)
      ),
      // 11
      interpolate(
        linear(), zoom(),
        literal(1), literal(10),
        literal(8), literal(200)
      ),
    };
  }

  private void initHeatmapIntensityStops() {
    listOfHeatmapIntensityStops = new Float[] {
      // 0
      0.6f,
      // 1
      0.3f,
      // 2
      1f,
      // 3
      1f,
      // 4
      1f,
      // 5
      1f,
      // 6
      1.5f,
      // 7
      0.8f,
      // 8
      0.25f,
      // 9
      0.8f,
      // 10
      0.25f,
      // 11
      0.5f
    };
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
      Log.e("MultipleHeatmapStyling", "Exception loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
    }
  }
}
// #-end-code-snippet: multiple-heatmap-styling-activity full-java