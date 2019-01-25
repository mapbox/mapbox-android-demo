package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTranslate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Use GeoJson data with SymbolLayers to create a data clustering effect.
 */
public class ImageClusteringActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_image_clustering);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        initLayerIcons(style);
        addClusteredGeoJsonSource(style);
        Toast.makeText(ImageClusteringActivity.this, R.string.zoom_map_in_and_out_instruction,
          Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void initLayerIcons(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage("single-quake-icon-id", BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.single_quake_icon)));
    loadedMapStyle.addImage("quake-triangle-icon-id", BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.earthquake_triangle)));
  }

  private void addClusteredGeoJsonSource(@NonNull Style loadedMapStyle) {
    // Add a new source from the GeoJSON data and set the 'cluster' option to true.
    try {
      loadedMapStyle.addSource(
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

    //Creating a SymbolLayer icon layer for single data/icon points
    loadedMapStyle.addLayer(new SymbolLayer("unclustered-points", "earthquakes").withProperties(
      iconImage("single-quake-icon-id"),
      iconSize(
        division(
          get("mag"), literal(4.0f)
        )
      )
    ));

    // Use the earthquakes GeoJSON source to create three point ranges.
    int[] layers = new int[] {150, 20, 0};

    for (int i = 0; i < layers.length; i++) {
      //Add clusters' SymbolLayers images
      SymbolLayer symbolLayer = new SymbolLayer("cluster-" + i, "earthquakes");

      symbolLayer.setProperties(
        iconImage("quake-triangle-icon-id"),
        iconTranslate(new Float[] {0f, -9f})
      );
      Expression pointCount = toNumber(get("point_count"));

      // Add a filter to the cluster layer that hides the icons based on "point_count"
      symbolLayer.setFilter(
        i == 0
          ? all(has("point_count"),
          gte(pointCount, literal(layers[i]))
        ) : all(has("point_count"),
          gt(pointCount, literal(layers[i])),
          lt(pointCount, literal(layers[i - 1]))
        )
      );
      loadedMapStyle.addLayer(symbolLayer);
    }

    //Add a SymbolLayer for the cluster data number point count
    loadedMapStyle.addLayer(new SymbolLayer("count", "earthquakes").withProperties(
      textField(Expression.toString(get("point_count"))),
      textSize(12f),
      textColor(Color.BLACK),
      textIgnorePlacement(true),
      textAllowOverlap(true)
    ));
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
}
