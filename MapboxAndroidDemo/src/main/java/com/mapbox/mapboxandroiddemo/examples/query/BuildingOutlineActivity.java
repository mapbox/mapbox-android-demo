package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_BEVEL;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Query the building layer to draw an outline around the building that is in the middle of the map
 */
public class BuildingOutlineActivity extends AppCompatActivity implements
    OnMapReadyCallback, MapboxMap.OnCameraIdleListener {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_query_building_outline);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    BuildingOutlineActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        setUpLineLayer(style);
        mapboxMap.addOnCameraIdleListener(BuildingOutlineActivity.this);
        showCrosshair();
        Toast.makeText(BuildingOutlineActivity.this, R.string.move_map_around_building_out_instruction,
            Toast.LENGTH_SHORT).show();
        updateOutline(style);
      }
    });
  }

  /**
   * Sets up the source and layer for drawing the building outline
   */
  private void setUpLineLayer(@NonNull Style loadedMapStyle) {
    // Create a GeoJSONSource from an empty FeatureCollection
    loadedMapStyle.addSource(new GeoJsonSource("source",
        FeatureCollection.fromFeatures(new Feature[]{})));

    // Use runtime styling to adjust the look of the building outline LineLayer
    loadedMapStyle.addLayer(new LineLayer("lineLayer", "source").withProperties(
        lineColor(Color.RED),
        lineWidth(6f),
        lineCap(LINE_CAP_ROUND),
        lineJoin(LINE_JOIN_BEVEL)
    ));
  }

  @Override
  public void onCameraIdle() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        updateOutline(style);
      }
    });
  }

  /**
   * Query the map for a building Feature in the map's building layer. The query happens in the middle of the
   * map ("the target"). If there's a building Feature in the middle of the map, its coordinates are turned
   * into a list of Point objects so that a LineString can be created.
   *
   * @return the LineString built via the building's coordinates
   */
  private LineString getBuildingFeatureOutline(@NonNull Style style) {
    // Retrieve the middle of the map
    final PointF pixel = mapboxMap.getProjection().toScreenLocation(new LatLng(
        mapboxMap.getCameraPosition().target.getLatitude(),
        mapboxMap.getCameraPosition().target.getLongitude()
    ));

    List<Point> pointList = new ArrayList<>();

    // Check whether the map style has a building layer
    if (style.getLayer("building") != null) {

      // Retrieve the building Feature that is displayed in the middle of the map
      List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "building");
      if (features.size() > 0) {
        if (features.get(0).geometry() instanceof Polygon) {
          Polygon buildingFeature = (Polygon) features.get(0).geometry();
          // Build a list of Point objects from the building Feature's coordinates
          if (buildingFeature != null) {
            for (int i = 0; i < buildingFeature.coordinates().size(); i++) {
              for (int j = 0;
                   j < buildingFeature.coordinates().get(i).size(); j++) {
                pointList.add(Point.fromLngLat(
                  buildingFeature.coordinates().get(i).get(j).longitude(),
                  buildingFeature.coordinates().get(i).get(j).latitude()
                ));
              }
            }
          }
        }
        // Create a LineString from the list of Point objects
      }
    } else {
      Toast.makeText(this, R.string.building_layer_not_present, Toast.LENGTH_SHORT).show();
    }
    return LineString.fromLngLats(pointList);
  }

  /**
   * Update the FeatureCollection used by the building outline LineLayer. Then refresh the map.
   */
  private void updateOutline(@NonNull Style style) {
    // Update the data source used by the building outline LineLayer and refresh the map
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]
      {Feature.fromGeometry(getBuildingFeatureOutline(style))});
    GeoJsonSource source = style.getSourceAs("source");
    if (source != null) {
      source.setGeoJson(featureCollection);
    }
  }

  private void showCrosshair() {
    View crosshair = new View(this);
    crosshair.setLayoutParams(new FrameLayout.LayoutParams(20, 20, Gravity.CENTER));
    crosshair.setBackgroundColor(Color.RED);
    mapView.addView(crosshair);
  }

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
    if (mapboxMap != null) {
      mapboxMap.removeOnCameraIdleListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
