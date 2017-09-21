package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.io.InputStream;

public class MultipleGeometriesActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private static final String GEOJSON_SOURCE_ID = "GEOJSON FILE";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_multiple_geometries);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    MultipleGeometriesActivity.this.mapboxMap = mapboxMap;
    createGeoJsonSource();
    addPolygonLayer();
    addPointsLayer();
  }

  private void createGeoJsonSource() {
    Log.d("here", "running createGeoJsonSource()");
    GeoJsonSource geoJsonSource = new GeoJsonSource(GEOJSON_SOURCE_ID,
      loadJsonFromAsset("norway.geojson"));
    mapboxMap.addSource(geoJsonSource);
    Log.d("Here", loadJsonFromAsset("norway.geojson"));
  }

  private void addPolygonLayer() {
    Log.d("here", "running addPolygonLayer()");
    FillLayer borderOutlineLayer = new FillLayer("polygon", GEOJSON_SOURCE_ID);
    borderOutlineLayer.setProperties(
      PropertyFactory.fillColor(Color.RED),
      PropertyFactory.fillOpacity(.4f));
    borderOutlineLayer.setFilter(Filter.eq("type", "Polygon"));
    mapboxMap.addLayer(borderOutlineLayer);
  }

  private void addPointsLayer() {
    Log.d("here", "running addPointsLayer()");
    CircleLayer pointsLayer = new CircleLayer("points", GEOJSON_SOURCE_ID);
    pointsLayer.setProperties(
      PropertyFactory.fillColor(Color.YELLOW),
      PropertyFactory.circleRadius(6f));
    pointsLayer.setFilter(Filter.eq("type", "Point"));
    mapboxMap.addLayer(pointsLayer);
    Log.d("here", "running addPointsLayer()");
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

  private String loadJsonFromAsset(String filename) {
    // Using this method to load in GeoJSON files from the assets folder.
    try {
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");

    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }


}