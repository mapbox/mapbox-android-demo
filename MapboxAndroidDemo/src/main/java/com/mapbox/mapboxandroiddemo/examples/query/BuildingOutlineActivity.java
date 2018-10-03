package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
  private FeatureCollection featureCollection;

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
  public void onMapReady(MapboxMap mapboxMap) {
    BuildingOutlineActivity.this.mapboxMap = mapboxMap;
    setUpLineLayer();
    mapboxMap.addOnCameraIdleListener(this);
    Toast.makeText(this, R.string.move_map_around_instruction, Toast.LENGTH_SHORT).show();
  }

  /**
   * Sets up the source and layer for drawing the building outline
   */
  private void setUpLineLayer() {
    // Create an empty FeatureCollection
    featureCollection = FeatureCollection.fromFeatures(new Feature[] {});

    // Create a GeoJSONSource from the empty FeatureCollection
    GeoJsonSource geoJsonSource = new GeoJsonSource("source", featureCollection);
    mapboxMap.addSource(geoJsonSource);

    // Use runtime styling to adjust the look of the building outline LineLayer
    LineLayer lineLayer = new LineLayer("lineLayer", "source");
    lineLayer.withProperties(
      lineColor(Color.RED),
      lineWidth(6f),
      lineCap(LINE_CAP_ROUND),
      lineJoin(LINE_JOIN_BEVEL)
    );
    mapboxMap.addLayer(lineLayer);
  }

  @Override
  public void onCameraIdle() {
    if (mapboxMap != null) {
      updateOutline();
    } else {
      Toast.makeText(this, R.string.building_layer_not_present, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Query the map for a building Feature in the map's building layer. The query happens in the middle of the
   * map ("the target"). If there's a building Feature in the middle of the map, its coordinates are turned
   * into a list of Point objects so that a LineString can be created.
   * @return the LineString built via the building's coordinates
   */
  private LineString getBuildingFeatureOutline() {
    // Retrieve the middle of the map
    final PointF pixel = mapboxMap.getProjection().toScreenLocation(new LatLng(
      mapboxMap.getCameraPosition().target.getLatitude(),
      mapboxMap.getCameraPosition().target.getLongitude()
    ));

    List<Point> pointList = new ArrayList<>();

    // Check whether the map style has a building layer
    if (mapboxMap.getLayer("building") != null) {

      // Retrieve the building Feature that is displayed in the middle of the map
      List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "building");
      if (features.size() > 0) {
        Feature buildingFeature = features.get(0);
        // Build a list of Point objects from the building Feature's coordinates
        for (int i = 0; i < ((Polygon) buildingFeature.geometry()).coordinates().size(); i++) {
          for (int j = 0;
               j < ((Polygon) buildingFeature.geometry()).coordinates().get(i).size(); j++) {
            pointList.add(Point.fromLngLat(
              ((Polygon) buildingFeature.geometry()).coordinates().get(i).get(j).longitude(),
              ((Polygon) buildingFeature.geometry()).coordinates().get(i).get(j).latitude()
            ));
          }
        }
        // Create a LineString from the list of Point objects
      }
    }
    return LineString.fromLngLats(pointList);
  }

  /**
   * Update the FeatureCollection used by the building outline LineLayer. Then refresh the map.
   */
  private void updateOutline() {
    // Update the data source used by the building outline LineLayer and refresh the map
    featureCollection = FeatureCollection.fromFeatures(new Feature[]
      {Feature.fromGeometry(getBuildingFeatureOutline())});
    GeoJsonSource source = mapboxMap.getSourceAs("source");
    if (source != null) {
      source.setGeoJson(featureCollection);
    }
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
