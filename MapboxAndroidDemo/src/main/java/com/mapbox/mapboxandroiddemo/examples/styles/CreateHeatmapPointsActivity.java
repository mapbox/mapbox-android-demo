package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.Filter.neq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class CreateHeatmapPointsActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_style_create_heatmap_points);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        addClusteredGeoJsonSource(mapboxMap);

      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
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

  private void addClusteredGeoJsonSource(MapboxMap mapboxMap) {

    // Add a new source from our GeoJSON data and set the 'cluster' option to true.
    try {
      mapboxMap.addSource(
          // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
          // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
          new GeoJsonSource("earthquakes", new URL("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"))
              .withCluster(true)
              .withClusterMaxZoom(15) // Max zoom to cluster points on
              .withClusterRadius(20) // Use small cluster radius for the heatmap look
      );
    } catch (MalformedURLException e) {
      Log.e("heatmapActivity", "Check the URL " + e.getMessage());
    }

    // Use the earthquakes source to create four layers:
    // three for each cluster category, and one for unclustered points

    // Each point range gets a different fill color.
    int[][] layers = new int[][]{
        new int[]{150, Color.parseColor("#E55E5E")},
        new int[]{20, Color.parseColor("#F9886C")},
        new int[]{0, Color.parseColor("#FBB03B")}
    };

    CircleLayer unclustered = new CircleLayer("unclustered-points", "earthquakes");
    unclustered.setProperties(
        circleColor(Color.parseColor("#FBB03B")),
        circleRadius(20f),
        circleBlur(1f));
    unclustered.setFilter(
        neq("cluster", true)
    );
    mapboxMap.addLayer(unclustered);

    for (int i = 0; i < layers.length; i++) {

      CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");
      circles.setProperties(
          circleColor(layers[i][1]),
          circleRadius(70f),
          circleBlur(1f)
      );
      circles.setFilter(
          i == 0 ?
              gte("point_count", layers[i][0]) :
              all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
      );
      mapboxMap.addLayer(circles);
    }
  }
}