package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;

public class MultipleMovingMarkersActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Handler handler;
  private Runnable runnable;
  private List<Feature> locationList;
  private static final String CAR_SYMBOL_LAYER_ID = "car_marker_layer_id";
  private static final String CAR_FEATURE__COLLECTION_ID = "CAR_FEATURE_COLLECTION_ID";
  private static final String CAR_GEOJSON_SOURCE_ID = "CAR_GEOJSON_SOURCE_ID";
  private FeatureCollection carFeatureCollection;
  private SymbolLayer carLocationLayer;

  @Override

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_multiple_moving_markers);

    initLocationList();
    initFeatureCollection();
    GeoJsonSource carGeoJsonSource = new GeoJsonSource(CAR_GEOJSON_SOURCE_ID, carFeatureCollection);
    carLocationLayer = new SymbolLayer(CAR_SYMBOL_LAYER_ID, CAR_GEOJSON_SOURCE_ID);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        MultipleMovingMarkersActivity.this.mapboxMap = mapboxMap;


        mapboxMap.addLayer(carLocationLayer);

        // Use the RefreshCarIconLocationRunnable class and runnable to keep updating the car locations
        runnable = new RefreshCarIconLocationRunnable(handler = new Handler());
        handler.postDelayed(runnable, 100);

      }
    });
  }

  private static class RefreshCarIconLocationRunnable implements Runnable {

    private Handler handler;

    public RefreshCarIconLocationRunnable(Handler handler) {
      this.handler = handler;
    }

    @Override
    public void run() {


      handler.postDelayed(this, 50);
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private void initFeatureCollection() {
    carFeatureCollection = FeatureCollection.fromFeatures(locationList);
  }

  private void initLocationList() {
    locationList = new ArrayList<>();
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.223612, 121.457761))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.203922, 121.433578))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.216156, 121.426813))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.218743, 121.531865))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.20144, 121.417334))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.208591, 121.516148))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.200713, 121.5236))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.20379, 121.503879))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.221197, 121.49006))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.218662, 121.445382))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.260226, 121.395577))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.213893, 121.494313))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.207333, 121.47999))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.223176, 121.488394))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.239005, 121.512189))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.223891, 121.46114))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.258623, 121.38207))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.23733, 121.444588))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.196006, 121.502773))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.2334, 121.477018))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.207648, 121.473235))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.224315, 121.48233))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.253812, 121.416214))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.214624, 121.463561))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.215741, 121.477191))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.214168, 121.496586))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.226763, 121.438357))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.213005, 121.447077))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.207859, 121.445382))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.207648, 121.486622))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.19509, 121.507228))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.231078, 121.455534))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.197579, 121.513306))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.237944, 121.459351))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.211004, 121.511693))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.200085, 121.483629))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.216643, 121.475366))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.232898, 121.509628))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.210181, 121.464742))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.197662, 121.443611))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.217793, 121.521988))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.238414, 121.386572))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.24362, 121.473011))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.232005, 121.482421))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.222877, 121.515359))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.20145, 121.460709))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.193431, 121.461475))));
  }

}