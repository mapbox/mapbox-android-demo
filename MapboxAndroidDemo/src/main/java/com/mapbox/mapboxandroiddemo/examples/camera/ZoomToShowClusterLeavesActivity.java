package com.mapbox.mapboxandroiddemo.examples.camera;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.layers.TransitionOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Tap on a cluster and adjust the map camera bounds to fit that cluster's leaves.
 */
public class ZoomToShowClusterLeavesActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private GeoJsonSource geoJsonSource;
  private String[] queryLayerIds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the MapView.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_zoom_to_show_cluster_leaves);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap map) {
        map.setStyle(Style.DARK, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            mapboxMap = map;

            // Disable any type of fading transition when icons collide on the map. This enhances the visual
            // look of the data clustering together and breaking apart.
            style.setTransition(new TransitionOptions(0, 0, false));

            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(12.099, -79.045), 3));

            addClusteredGeoJsonSource(style);

            style.addImage(
              "icon-id",
              BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.single_quake_icon)));

            mapboxMap.addOnMapClickListener(ZoomToShowClusterLeavesActivity.this);

            Toast.makeText(ZoomToShowClusterLeavesActivity.this,
                R.string.tap_on_cluster_circle_instruction, Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    PointF pointf = mapboxMap.getProjection().toScreenLocation(point);
    RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);
    List<Feature> mapClickFeatureList = mapboxMap.queryRenderedFeatures(rectF, queryLayerIds);
    if (mapClickFeatureList.size() > 0) {
      FeatureCollection clusterLeavesFeatureCollection = geoJsonSource.getClusterLeaves(mapClickFeatureList.get(0),
        8000, 0);
      moveCameraToLeavesBounds(clusterLeavesFeatureCollection);
    }

    return true;
  }

  private void moveCameraToLeavesBounds(FeatureCollection featureCollectionToInspect) {
    List<LatLng> latLngList = new ArrayList<>();
    if (featureCollectionToInspect.features() != null) {
      for (Feature singleClusterFeature : featureCollectionToInspect.features()) {
        Point clusterPoint = (Point) singleClusterFeature.geometry();
        if (clusterPoint != null) {
          latLngList.add(new LatLng(clusterPoint.latitude(), clusterPoint.longitude()));
        }
      }
      if (latLngList.size() > 1) {
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
          .includes(latLngList)
          .build();
        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 230),
          1300);
      }
    }
  }

  private void addClusteredGeoJsonSource(@NonNull Style loadedMapStyle) {

    // Add a new source from the GeoJSON data and set the "cluster" option to true.
    try {

      // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
      // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
      geoJsonSource = new GeoJsonSource("earthquakes",
          new URI("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
          new GeoJsonOptions()
              .withCluster(true)
              .withClusterMaxZoom(14)
              .withClusterRadius(50)
      );
      loadedMapStyle.addSource(

        geoJsonSource
      );
    } catch (URISyntaxException uriSyntaxException) {
      Timber.e("Check the URL %s", uriSyntaxException.getMessage());
    }

    // Creating a marker layer for single data points
    SymbolLayer unclustered = new SymbolLayer("unclustered-points", "earthquakes");

    unclustered.setProperties(
      iconImage("icon-id"),
      iconSize(
        division(
          get("mag"), literal(4.0f)
        )
      )
    );
    unclustered.setFilter(has("mag"));
    loadedMapStyle.addLayer(unclustered);

    // Use the earthquakes GeoJSON source to create three layers: One layer for each cluster category.
    // Each point range gets a different fill color.
    int[][] layers = new int[][] {
      new int[] {150, ContextCompat.getColor(this, R.color.mapboxPurple)},
      new int[] {20, ContextCompat.getColor(this, R.color.mapboxOrange)},
      new int[] {0, ContextCompat.getColor(this, R.color.mapboxPinkDark)}
    };

    queryLayerIds = new String[layers.length];

    for (int i = 0; i < layers.length; i++) {
      queryLayerIds[i] = "cluster-" + i;
      // Add clusters' circles
      CircleLayer circles = new CircleLayer(queryLayerIds[i], "earthquakes");
      circles.setProperties(
        circleColor(layers[i][1]),
        circleRadius(18f)
      );

      Expression pointCount = toNumber(get("point_count"));

      // Add a filter to the cluster layer that hides the circles based on "point_count"
      circles.setFilter(
        i == 0
          ? all(has("point_count"),
          gte(pointCount, literal(layers[i][0]))
        ) : all(has("point_count"),
          gte(pointCount, literal(layers[i][0])),
          lt(pointCount, literal(layers[i - 1][0]))
        )
      );
      loadedMapStyle.addLayer(circles);
    }

    // Add the count labels
    SymbolLayer count = new SymbolLayer("count", "earthquakes");
    count.setProperties(
      textField(Expression.toString(get("point_count"))),
      textSize(12f),
      textColor(Color.WHITE),
      textIgnorePlacement(true),
      textAllowOverlap(true)
    );
    loadedMapStyle.addLayer(count);
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
