package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
  private static final String GEOJSON_SOURCE_ID = "GEOJSONFILE";

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
    // Load data from GeoJSON file in the assets folder
    GeoJsonSource geoJsonSource = new GeoJsonSource(GEOJSON_SOURCE_ID,
      loadJsonFromAsset("fake_norway_campsites.geojson"));
    mapboxMap.addSource(geoJsonSource);
  }

  private void addPolygonLayer() {
    // Create and style a FillLayer that uses the Polygon Feature's coordinates in the GeoJSON data
    FillLayer borderOutlineLayer = new FillLayer("polygon", GEOJSON_SOURCE_ID);
    borderOutlineLayer.setProperties(
      PropertyFactory.fillColor(Color.RED),
      PropertyFactory.fillOpacity(.4f));
    borderOutlineLayer.setFilter(Filter.eq("$type", "Polygon"));
    mapboxMap.addLayer(borderOutlineLayer);
  }

  private void addPointsLayer() {
    // Create and style a CircleLayer that uses the Point Features' coordinates in the GeoJSON data
    CircleLayer pointsLayer = new CircleLayer("points", GEOJSON_SOURCE_ID);
    pointsLayer.setProperties(
      PropertyFactory.circleColor(Color.YELLOW),
      PropertyFactory.circleRadius(3f));
    pointsLayer.setFilter(Filter.eq("$type", "Point"));
    mapboxMap.addLayer(pointsLayer);
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