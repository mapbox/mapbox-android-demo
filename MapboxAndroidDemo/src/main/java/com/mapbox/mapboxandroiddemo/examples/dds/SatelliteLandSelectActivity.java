package com.mapbox.mapboxandroiddemo.examples.dds;

// #-code-snippet: satellite-land-select-activity full-java

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Use map click location to select an area of land on satellite photos and draw the selected area
 * with a CircleLayer, LineLayer, and FillLayer.
 */
public class SatelliteLandSelectActivity extends AppCompatActivity implements
  OnMapReadyCallback {

  private static final String CIRCLE_SOURCE_ID = "circle-source-id";
  private static final String FILL_SOURCE_ID = "fill-source-id";
  private static final String LINE_SOURCE_ID = "line-source-id";
  private static final String CIRCLE_LAYER_ID = "circle-layer-id";
  private static final String FILL_LAYER_ID = "fill-layer-polygon-id";
  private static final String LINE_LAYER_ID = "line-layer-id";
  private List<Point> fillLayerPointList;
  private List<Point> lineLayerPointList;
  private List<Feature> circleLayerFeatureList;
  private List<List<Point>> listOfList;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private GeoJsonSource circleSource;
  private GeoJsonSource fillSource;
  private GeoJsonSource lineSource;
  private Point firstPointOfPolygon;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_satellite_land_select);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    mapboxMap.animateCamera(
      CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
        .zoom(16)
        .build()), 4000);

    fillLayerPointList = new ArrayList<>();
    circleLayerFeatureList = new ArrayList<>();
    lineLayerPointList = new ArrayList<>();

    // Add sources to the map
    initCircleSource();
    initLineSource();
    initFillSource();

    this.circleSource = mapboxMap.getSourceAs(CIRCLE_SOURCE_ID);
    this.fillSource = mapboxMap.getSourceAs(FILL_SOURCE_ID);
    this.lineSource = mapboxMap.getSourceAs(LINE_SOURCE_ID);

    // Add layers to the map
    initCircleLayer();
    initLineLayer();
    initFillLayer();

    initFloatingActionButtonClickListeners();
    Toast.makeText(this, R.string.trace_instruction, Toast.LENGTH_LONG).show();
  }

  /**
   * Set the button click listeners
   */
  private void initFloatingActionButtonClickListeners() {
    Button clearBoundariesFab = findViewById(R.id.clear_button);
    clearBoundariesFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        clearEntireMap();
      }
    });

    FloatingActionButton dropPinFab = findViewById(R.id.drop_pin_button);
    dropPinFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        // Use the map click location to create a Point object
        Point mapTargetPoint = Point.fromLngLat(mapboxMap.getCameraPosition().target.getLongitude(),
          mapboxMap.getCameraPosition().target.getLatitude());

        // Make note of the first map click location so that it can be used to create a closed polygon later on
        if (circleLayerFeatureList.size() == 0) {
          firstPointOfPolygon = mapTargetPoint;
        }

        // Add the click point to the circle layer and update the display of the circle layer data
        circleLayerFeatureList.add(Feature.fromGeometry(mapTargetPoint));
        if (circleSource != null) {
          circleSource.setGeoJson(FeatureCollection.fromFeatures(circleLayerFeatureList));
        }

        // Add the click point to the line layer and update the display of the line layer data
        if (circleLayerFeatureList.size() < 3) {
          lineLayerPointList.add(mapTargetPoint);
        } else if (circleLayerFeatureList.size() == 3) {
          lineLayerPointList.add(mapTargetPoint);
          lineLayerPointList.add(firstPointOfPolygon);
        } else if (circleLayerFeatureList.size() >= 4) {
          lineLayerPointList.remove(circleLayerFeatureList.size() - 1);
          lineLayerPointList.add(mapTargetPoint);
          lineLayerPointList.add(firstPointOfPolygon);
        }
        if (lineSource != null) {
          lineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]
            {Feature.fromGeometry(LineString.fromLngLats(lineLayerPointList))}));
        }

        // Add the click point to the fill layer and update the display of the fill layer data
        if (circleLayerFeatureList.size() < 3) {
          fillLayerPointList.add(mapTargetPoint);
        } else if (circleLayerFeatureList.size() == 3) {
          fillLayerPointList.add(mapTargetPoint);
          fillLayerPointList.add(firstPointOfPolygon);
        } else if (circleLayerFeatureList.size() >= 4) {
          fillLayerPointList.remove(fillLayerPointList.size() - 1);
          fillLayerPointList.add(mapTargetPoint);
          fillLayerPointList.add(firstPointOfPolygon);
        }
        listOfList = new ArrayList<>();
        listOfList.add(fillLayerPointList);
        List<Feature> finalFeatureList = new ArrayList<>();
        finalFeatureList.add(Feature.fromGeometry(Polygon.fromLngLats(listOfList)));
        FeatureCollection newFeatureCollection = FeatureCollection.fromFeatures(finalFeatureList);
        if (fillSource != null) {
          fillSource.setGeoJson(newFeatureCollection);
        }
      }
    });
  }

  /**
   * Remove the drawn area from the map by resetting the FeatureCollections used by the layers' sources
   */
  private void clearEntireMap() {
    fillLayerPointList = new ArrayList<>();
    circleLayerFeatureList = new ArrayList<>();
    lineLayerPointList = new ArrayList<>();
    if (circleSource != null) {
      circleSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {}));
    }
    if (lineSource != null) {
      lineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {}));
    }
    if (fillSource != null) {
      fillSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {}));
    }
  }

  /**
   * Set up the CircleLayer source for showing map click points
   */
  private void initCircleSource() {
    FeatureCollection circleFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource circleGeoJsonSource = new GeoJsonSource(CIRCLE_SOURCE_ID, circleFeatureCollection);
    mapboxMap.addSource(circleGeoJsonSource);
  }

  /**
   * Set up the CircleLayer for showing polygon click points
   */
  private void initCircleLayer() {
    CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID,
      CIRCLE_SOURCE_ID);
    circleLayer.setProperties(
      circleRadius(7f),
      circleColor(Color.parseColor("#d004d3"))
    );
    mapboxMap.addLayer(circleLayer);
  }

  /**
   * Set up the FillLayer source for showing map click points
   */
  private void initFillSource() {
    FeatureCollection fillFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource fillGeoJsonSource = new GeoJsonSource(FILL_SOURCE_ID, fillFeatureCollection);
    mapboxMap.addSource(fillGeoJsonSource);
  }

  /**
   * Set up the FillLayer for showing the set boundaries' polygons
   */
  private void initFillLayer() {
    FillLayer fillLayer = new FillLayer(FILL_LAYER_ID,
      FILL_SOURCE_ID);
    fillLayer.setProperties(
      fillOpacity(.6f),
      fillColor(Color.parseColor("#00e9ff"))
    );
    mapboxMap.addLayerBelow(fillLayer, LINE_LAYER_ID);
  }

  /**
   * Set up the LineLayer source for showing map click points
   */
  private void initLineSource() {
    FeatureCollection lineFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource lineGeoJsonSource = new GeoJsonSource(LINE_SOURCE_ID, lineFeatureCollection);
    mapboxMap.addSource(lineGeoJsonSource);
  }

  /**
   * Set up the LineLayer for showing the set boundaries' polygons
   */
  private void initLineLayer() {
    LineLayer lineLayer = new LineLayer(LINE_LAYER_ID,
      LINE_SOURCE_ID);
    lineLayer.setProperties(
      lineColor(Color.WHITE),
      lineWidth(5f)
    );
    mapboxMap.addLayerBelow(lineLayer, CIRCLE_LAYER_ID);
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
// #-end-code-snippet: satellite-land-select-activity full-java