package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.turf.TurfClassification;
import com.mapbox.turf.TurfJoins;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.turf.TurfConstants.UNIT_YARDS;

public class PickupStationActivity extends AppCompatActivity
    implements OnMapReadyCallback,
    MapboxMap.OnCameraIdleListener, MapboxMap.OnCameraMoveStartedListener {

  private static final LatLngBounds AIRPORT_BOUNDS = new LatLngBounds.Builder()
      .include(new LatLng(52.305829, 4.764549))
      .include(new LatLng(52.310747, 4.755457))
      .build();

  private static final String STATION_IMAGE_ID = "STATION_IMAGE_ID";
  private static final String UNSELECTED_STATION_SOURCE_ID = "UNSELECTED_STATION_SOURCE_ID";
  private static final String UNSELECTED_STATION_SYMBOL_LAYER = "UNSELECTED_STATION_SYMBOL_LAYER";
  private static final String TARGET_SOURCE_ID = "TARGET_SOURCE_ID";
  private static final String TARGET_SYMBOL_LAYER_ID = "TARGET_SYMBOL_LAYER_ID";
  private static final String SELECTED_STATION_LAYER_ID = "SELECTED_STATION_LAYER_ID";
  private static final String SELECTED_STATION_SOURCE_ID = "SELECTED_STATION_SOURCE_ID";
  private static final float UNSELECTED_STATION_MARKER_SIZE = .4f;
  private static final float SELECTED_STATION_MARKER_SIZE = .6f;
  private MapboxMap mapboxMap;
  private MapView mapView;
  private GeoJsonSource pickupLocationTargetGeoJsonSource;
  private Point targetPoint;
  private LatLng closestStation;
  private List<Point> stationPointList;
  private FeatureCollection stationFeatureCollection;
  private boolean stationSelected = false;
  private static final double radiusConst = 70;
  private static final String unitConst = UNIT_YARDS;
  private String TAG = "PickupStationActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_pickup_station);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    // Set bounds to Amsterdam airport
    mapboxMap.setLatLngBoundsForCameraTarget(AIRPORT_BOUNDS);
    initStationPointList();
    initTargetLocationSymbolLayer();
    initPickupLocationSetterView();
    initStationSymbolLayer();
    initSelectedStationLayer();
    mapboxMap.addOnCameraIdleListener(this);
  }

  private void initTargetLocationSymbolLayer() {
    FeatureCollection featureCollection =
        FeatureCollection.fromFeatures(new Feature[]{});
    pickupLocationTargetGeoJsonSource =
        new GeoJsonSource(TARGET_SOURCE_ID, featureCollection);
    mapboxMap.addSource(pickupLocationTargetGeoJsonSource);
    SymbolLayer pickupSymbolLayer = new SymbolLayer(TARGET_SYMBOL_LAYER_ID, TARGET_SOURCE_ID);
    mapboxMap.addLayer(pickupSymbolLayer);
  }

  private void initPickupLocationSetterView() {
    // When user is still picking a location, we hover a marker above the mapboxMap in the center.
    // This is done by using an image view with the default marker found in the SDK. You can
    // swap out for your own marker image, just make sure it matches up with the dropped marker.
    ImageView targetMarker = new ImageView(this);
    targetMarker.setImageResource(R.drawable.red_marker);

    // TODO: Get current FrameLayout instead of creating a new one?
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    targetMarker.setLayoutParams(params);
    mapView.addView(targetMarker);
  }

  private void initStationPointList() {
    Log.d(TAG, "initStationPointList: ");
    stationPointList = new ArrayList<>();
    stationPointList.add(Point.fromLngLat(4.760532, 52.306163));
    stationPointList.add(Point.fromLngLat(4.763428, 52.307468));
    stationPointList.add(Point.fromLngLat(4.763357, 52.308859));
    stationPointList.add(Point.fromLngLat(4.763619, 52.309737));
    stationPointList.add(Point.fromLngLat(4.310131, 52.762107));
  }

  private void initStationSymbolLayer() {
    Log.d(TAG, "initStationSymbolLayer: ");
    // Add the stations' icon images to the map
    Bitmap icon = BitmapFactory.decodeResource(
        this.getResources(), R.drawable.ic_circle);
    mapboxMap.addImage(STATION_IMAGE_ID, icon);
    stationFeatureCollection = FeatureCollection.fromFeatures(new Feature[]{});
    List<Feature> featureList = new ArrayList<>();
    for (int x = 0; x < stationPointList.size(); x++) {
      Log.d(TAG, "initStationSymbolLayer: x = " + x);
      featureList.add(Feature.fromGeometry(
          stationPointList.get(x)));
    }
    stationFeatureCollection = FeatureCollection.fromFeatures(featureList);
    String[] stationNames = new String[]{
        "Concourse B",
        "Concourse C",
        "Concourse D",
        "Concourse E",
        "Concourse F",
    };
    for (int x = 0; x < stationFeatureCollection.features().size(); x++) {
      stationFeatureCollection.features().get(x).addStringProperty(
          "STATION NAME", stationNames[x]);
    }
    Log.d(TAG, "initStationSymbolLayer: stationFeatureCollection = "
        + stationFeatureCollection.features().size());
    GeoJsonSource geoJsonSource =
        new GeoJsonSource(UNSELECTED_STATION_SOURCE_ID, stationFeatureCollection);
    mapboxMap.addSource(geoJsonSource);
    SymbolLayer stationSymbolLayer =
        new SymbolLayer(UNSELECTED_STATION_SYMBOL_LAYER, UNSELECTED_STATION_SOURCE_ID);
    stationSymbolLayer.withProperties(
        iconImage(STATION_IMAGE_ID),
        iconSize(UNSELECTED_STATION_MARKER_SIZE),
        iconAllowOverlap(true)
    );
    mapboxMap.addLayer(stationSymbolLayer);
  }

  private void initSelectedStationLayer() {
    Log.d(TAG, "initSelectedStationLayer: ");
    FeatureCollection emptySource = FeatureCollection.fromFeatures(new Feature[]{});
    Source selectedMarkerSource = new GeoJsonSource(SELECTED_STATION_SOURCE_ID, emptySource);
    mapboxMap.addSource(selectedMarkerSource);
    SymbolLayer stationSymbolLayer =
        new SymbolLayer(SELECTED_STATION_LAYER_ID, SELECTED_STATION_SOURCE_ID);
    stationSymbolLayer.withProperties(
        iconImage(STATION_IMAGE_ID)
    );
    mapboxMap.addLayer(stationSymbolLayer);
  }

  private void moveTargetToStation(LatLng latLngDestination) {
    Log.d(TAG, "moveTargetToStation: ");
    mapboxMap.moveCamera(CameraUpdateFactory
        .newCameraPosition(new CameraPosition.Builder()
            .target(latLngDestination) // Sets the new camera position
            .build()));
  }

  @Override
  public void onCameraIdle() {
    Log.d(TAG, "onCameraIdle: ");
    targetPoint = Point.fromLngLat(
        mapboxMap.getCameraPosition().target.getLongitude(),
        mapboxMap.getCameraPosition().target.getLatitude());

    Log.d(TAG, "onCameraIdle: targetPoint = " + targetPoint.toString());
    pickupLocationTargetGeoJsonSource.setGeoJson(FeatureCollection.fromFeature(
        Feature.fromGeometry(targetPoint)));
    Point closestStation = retrieveClosestStationToTarget(
        new LatLng(targetPoint.latitude(), targetPoint.longitude()));
    if (targetIsWithinClosestStationArea(closestStation)) {
      /*moveTargetToStation(new LatLng(closestStation.latitude(),
          closestStation.longitude()));*/
      evaluateStationIconSize();
    }
  }

  @Override
  public void onCameraMoveStarted(int reason) {
    mapboxMap.addOnCameraIdleListener(this);

  }

  private boolean targetIsWithinClosestStationArea(Point closetStation) {

    return TurfJoins.inside(targetPoint,
        TurfTransformation.circle(closetStation, radiusConst,
            unitConst));
  }

  private Point retrieveClosestStationToTarget(LatLng targetLocation) {
    Log.d(TAG, "retrieveClosestStationToTarget: ");
    Log.d(TAG, "retrieveClosestStationToTarget: nearestPoint =" +
      TurfClassification.nearestPoint(
        Point.fromLngLat(targetLocation.getLongitude(), targetLocation.getLatitude()),
        stationPointList).toString());

    return TurfClassification.nearestPoint(
        Point.fromLngLat(targetLocation.getLongitude(), targetLocation.getLatitude()),
        stationPointList);
  }

  private void evaluateStationIconSize() {
    final SymbolLayer singleStation = (SymbolLayer) mapboxMap.getLayer(SELECTED_STATION_LAYER_ID);
    final PointF pixel = mapboxMap.getProjection().toScreenLocation(new LatLng(
        targetPoint.latitude(), targetPoint.longitude()));
    List<Feature> unselectedStationList = mapboxMap.queryRenderedFeatures(pixel, UNSELECTED_STATION_SYMBOL_LAYER);
    List<Feature> selectedStations = mapboxMap.queryRenderedFeatures(pixel, SELECTED_STATION_LAYER_ID);
    if (selectedStations.size() > 0 && stationSelected) {
      return;
    }
    if (unselectedStationList.isEmpty()) {
      if (stationSelected) {
        deselectStation(singleStation);
      }
      return;
    }
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(
        new Feature[]{Feature.fromGeometry(unselectedStationList.get(0).geometry())});
    GeoJsonSource selectedStationSource = mapboxMap.getSourceAs(SELECTED_STATION_SOURCE_ID);
    if (selectedStationSource != null) {
      selectedStationSource.setGeoJson(featureCollection);
    }

    if (stationSelected) {
      deselectStation(singleStation);
    }
    if (unselectedStationList.size() > 0) {
      selectMarker(singleStation);
    }
  }

  private void selectMarker(final SymbolLayer marker) {
    ValueAnimator markerAnimator = new ValueAnimator();
    markerAnimator.setObjectValues(UNSELECTED_STATION_MARKER_SIZE,
        SELECTED_STATION_MARKER_SIZE);
    markerAnimator.setDuration(100);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        marker.setProperties(
            PropertyFactory.iconSize((float) animator.getAnimatedValue())
        );
      }
    });
    markerAnimator.start();
    stationSelected = true;
  }

  private void deselectStation(final SymbolLayer marker) {
    ValueAnimator markerAnimator = new ValueAnimator();
    markerAnimator.setObjectValues(SELECTED_STATION_MARKER_SIZE,
        UNSELECTED_STATION_MARKER_SIZE);
    markerAnimator.setDuration(100);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        marker.setProperties(
            PropertyFactory.iconSize((float) animator.getAnimatedValue())
        );
      }
    });
    markerAnimator.start();
    stationSelected = false;
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
    mapboxMap.removeOnCameraIdleListener(this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}