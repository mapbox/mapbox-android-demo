package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Use GeoJSON to visualize point data as a clusters.
 */
public class GeoJsonClusteringActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_geojson_clustering);

    mapView = (MapView) findViewById(R.id.mapView);

    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap map) {

        mapboxMap = map;

        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(12.099, -79.045), 3));

        addClusteredGeoJsonSource();

        Toast.makeText(GeoJsonClusteringActivity.this, R.string.zoom_map_in_and_out_instruction,
          Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
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
  public void onStop() {
    super.onStop();
    mapView.onStop();
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


  private void addClusteredGeoJsonSource() {

    // Add a new source from the GeoJSON data and set the 'cluster' option to true.
    try {
      mapboxMap.addSource(
        // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
        // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
        new GeoJsonSource("earthquakes",
          new URL("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
          new GeoJsonOptions()
            .withCluster(true)
            .withClusterMaxZoom(14)
            .withClusterRadius(50)
        )
      );
    } catch (MalformedURLException malformedUrlException) {
      Log.e("dataClusterActivity", "Check the URL " + malformedUrlException.getMessage());
    }


    // Use the earthquakes GeoJSON source to create three layers: One layer for each cluster category.
    // Each point range gets a different fill color.
    int[][] layers = new int[][] {
      new int[] {150, ContextCompat.getColor(this, R.color.mapboxRed)},
      new int[] {20, ContextCompat.getColor(this, R.color.mapboxGreen)},
      new int[] {0, ContextCompat.getColor(this, R.color.mapbox_blue)}
    };

    //Creating a marker layer for single data points
    SymbolLayer unclustered = new SymbolLayer("unclustered-points", "earthquakes");
    unclustered.setProperties(iconImage("marker-15"));
    mapboxMap.addLayer(unclustered);

    for (int i = 0; i < layers.length; i++) {
      //Add clusters' circles
      CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");
      circles.setProperties(
        circleColor(layers[i][1]),
        circleRadius(18f)
      );

      // Add a filter to the cluster layer that hides the circles based on "point_count"
      circles.setFilter(
        i == 0
          ? gte("point_count", layers[i][0]) :
          all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
      );
      mapboxMap.addLayer(circles);
    }

    //Add the count labels
    SymbolLayer count = new SymbolLayer("count", "earthquakes");
    count.setProperties(
      textField("{point_count}"),
      textSize(12f),
      textColor(Color.WHITE)
    );
    mapboxMap.addLayer(count);

  }
}
