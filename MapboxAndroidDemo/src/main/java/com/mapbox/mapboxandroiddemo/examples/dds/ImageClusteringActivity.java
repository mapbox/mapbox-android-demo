package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.layers.TransitionOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Use GeoJSON data with SymbolLayers to create a data clustering effect with icons rather than circles.
 */
public class ImageClusteringActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String CLUSTER_EARTHQUAKE_TRIANGLE_ICON_ID = "quake-triangle-icon-id";
  private static final String SINGLE_EARTHQUAKE_TRIANGLE_ICON_ID = "single-quake-icon-id";
  private static final String EARTHQUAKE_SOURCE_ID = "earthquakes";
  private static final String POINT_COUNT = "point_count";
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

        // Disable any type of fading transition when icons collide on the map. This enhances the visual
        // look of the data clustering together and breaking apart.
        style.setTransition(new TransitionOptions(0, 0, false));

        initLayerIcons(style);
        addClusteredGeoJsonSource(style);
        Toast.makeText(ImageClusteringActivity.this, R.string.zoom_map_in_and_out_instruction,
          Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void initLayerIcons(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage(SINGLE_EARTHQUAKE_TRIANGLE_ICON_ID, BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.single_quake_icon)));
    loadedMapStyle.addImage(CLUSTER_EARTHQUAKE_TRIANGLE_ICON_ID, BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.earthquake_triangle)));
  }

  private void addClusteredGeoJsonSource(@NonNull Style loadedMapStyle) {
    // Add a new source from the GeoJSON data and set the 'cluster' option to true.
    try {
      loadedMapStyle.addSource(
        // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
        // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
        new GeoJsonSource(EARTHQUAKE_SOURCE_ID,
          new URI("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
          new GeoJsonOptions()
            .withCluster(true)
            .withClusterMaxZoom(14)
            .withClusterRadius(50)
        )
      );
    } catch (URISyntaxException uriSyntaxException) {
      Timber.e("Check the URL %s" , uriSyntaxException.getMessage());
    }

    SymbolLayer unclusteredSymbolLayer = new SymbolLayer("unclustered-points", EARTHQUAKE_SOURCE_ID).withProperties(
      iconImage(SINGLE_EARTHQUAKE_TRIANGLE_ICON_ID),
      iconSize(
        division(
          get("mag"), literal(4.0f)
        )
      )
    );
    unclusteredSymbolLayer.setFilter(has("mag"));

    //Creating a SymbolLayer icon layer for single data/icon points
    loadedMapStyle.addLayer(unclusteredSymbolLayer);

    // Use the earthquakes GeoJSON source to create three point ranges.
    int[] layers = new int[] {150, 20, 0};

    for (int i = 0; i < layers.length; i++) {
      //Add clusters' SymbolLayers images
      SymbolLayer symbolLayer = new SymbolLayer("cluster-" + i, EARTHQUAKE_SOURCE_ID);

      symbolLayer.setProperties(
        iconImage(CLUSTER_EARTHQUAKE_TRIANGLE_ICON_ID)
      );
      Expression pointCount = toNumber(get(POINT_COUNT));

      // Add a filter to the cluster layer that hides the icons based on "point_count"
      symbolLayer.setFilter(
        i == 0
          ? all(has(POINT_COUNT),
          gte(pointCount, literal(layers[i]))
        ) : all(has(POINT_COUNT),
          gte(pointCount, literal(layers[i])),
          lt(pointCount, literal(layers[i - 1]))
        )
      );
      loadedMapStyle.addLayer(symbolLayer);
    }

    //Add a SymbolLayer for the cluster data number point count
    loadedMapStyle.addLayer(new SymbolLayer("count", EARTHQUAKE_SOURCE_ID).withProperties(
      textField(Expression.toString(get(POINT_COUNT))),
      textSize(12f),
      textColor(Color.BLACK),
      textIgnorePlacement(true),
      // The .5f offset moves the data numbers down a little bit so that they're
      // in the middle of the triangle cluster image
      textOffset(new Float[] {0f, .5f}),
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
