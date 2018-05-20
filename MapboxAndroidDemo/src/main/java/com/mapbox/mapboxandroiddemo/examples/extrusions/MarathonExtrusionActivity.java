package com.mapbox.mapboxandroiddemo.examples.extrusions;
// #-code-snippet: marathon-extrusion-activity full-java
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;

/**
 * Use data-driven styling and GeoJSON data to set extrusions' heights
 */
public class MarathonExtrusionActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_marathon_extrusion);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        MarathonExtrusionActivity.this.mapboxMap = mapboxMap;

        // Add the marathon route source to the map
        try {
          // Create a GeoJsonSource and use the Mapbox Datasets API to retrieve the GeoJSON data
          // More info about the Datasets API at https://www.mapbox.com/api-documentation/#retrieve-a-dataset
          GeoJsonSource courseRouteGeoJson = new GeoJsonSource("coursedata",
            new URL(
              "https://api.mapbox.com/datasets/v1/appsatmapboxcom/cjhksgac501g5c6qx051jxavj/"
                + "features?access_token=" + getString(R.string.access_token)));
          mapboxMap.addSource(courseRouteGeoJson);
          addExtrusionsLayerToMap();
        } catch (MalformedURLException malformedUrlException) {
          Timber.d("Check the URL " + malformedUrlException.getMessage());
        }
      }
    });
  }

  private void addExtrusionsLayerToMap() {
    // Add FillExtrusion layer to map using GeoJSON data
    FillExtrusionLayer courseExtrusionLayer = new FillExtrusionLayer("course", "coursedata");
    courseExtrusionLayer.setProperties(
      fillExtrusionColor(Color.YELLOW),
      fillExtrusionOpacity(0.7f),
      fillExtrusionHeight(get("e")));
    mapboxMap.addLayer(courseExtrusionLayer);
  }
}
// #-end-code-snippet: marathon-extrusion-activity full-java