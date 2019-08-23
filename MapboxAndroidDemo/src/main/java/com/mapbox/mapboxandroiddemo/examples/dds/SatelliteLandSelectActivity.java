package com.mapbox.mapboxandroiddemo.examples.dds;


import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
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
import com.mapbox.mapboxsdk.maps.Style;
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
  private List<Point> fillLayerPointList = new ArrayList<>();
  private List<Point> lineLayerPointList = new ArrayList<>();
  private List<Feature> circleLayerFeatureList = new ArrayList<>();
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
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    mapboxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        mapboxMap.animateCamera(
          CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
            .zoom(16)
            .build()), 4000);

        // Add sources to the map
        circleSource = initCircleSource(style);
        fillSource = initFillSource(style);
        lineSource = initLineSource(style);

        // Add layers to the map
        initCircleLayer(style);
        initLineLayer(style);
        initFillLayer(style);

        initFloatingActionButtonClickListeners();
        Toast.makeText(SatelliteLandSelectActivity.this, R.string.trace_instruction, Toast.LENGTH_LONG).show();
      }
    });
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
        } else {
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
        } else {
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
  private GeoJsonSource initCircleSource(@NonNull Style loadedMapStyle) {
    FeatureCollection circleFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource circleGeoJsonSource = new GeoJsonSource(CIRCLE_SOURCE_ID, circleFeatureCollection);
    loadedMapStyle.addSource(circleGeoJsonSource);
    return circleGeoJsonSource;
  }

  /**
   * Set up the CircleLayer for showing polygon click points
   */
  private void initCircleLayer(@NonNull Style loadedMapStyle) {
    CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID,
      CIRCLE_SOURCE_ID);
    circleLayer.setProperties(
      circleRadius(7f),
      circleColor(Color.parseColor("#d004d3"))
    );
    loadedMapStyle.addLayer(circleLayer);
  }

  /**
   * Set up the FillLayer source for showing map click points
   */
  private GeoJsonSource initFillSource(@NonNull Style loadedMapStyle) {
    FeatureCollection fillFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource fillGeoJsonSource = new GeoJsonSource(FILL_SOURCE_ID, fillFeatureCollection);
    loadedMapStyle.addSource(fillGeoJsonSource);
    return fillGeoJsonSource;
  }

  /**
   * Set up the FillLayer for showing the set boundaries' polygons
   */
  private void initFillLayer(@NonNull Style loadedMapStyle) {
    FillLayer fillLayer = new FillLayer(FILL_LAYER_ID,
      FILL_SOURCE_ID);
    fillLayer.setProperties(
      fillOpacity(.6f),
      fillColor(Color.parseColor("#00e9ff"))
    );
    loadedMapStyle.addLayerBelow(fillLayer, LINE_LAYER_ID);
  }

  /**
   * Set up the LineLayer source for showing map click points
   */
  private GeoJsonSource initLineSource(@NonNull Style loadedMapStyle) {
    FeatureCollection lineFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource lineGeoJsonSource = new GeoJsonSource(LINE_SOURCE_ID, lineFeatureCollection);
    loadedMapStyle.addSource(lineGeoJsonSource);
    return lineGeoJsonSource;
  }

  /**
   * Set up the LineLayer for showing the set boundaries' polygons
   */
  private void initLineLayer(@NonNull Style loadedMapStyle) {
    LineLayer lineLayer = new LineLayer(LINE_LAYER_ID,
      LINE_SOURCE_ID);
    lineLayer.setProperties(
      lineColor(Color.WHITE),
      lineWidth(5f)
    );
    loadedMapStyle.addLayerBelow(lineLayer, CIRCLE_LAYER_ID);
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