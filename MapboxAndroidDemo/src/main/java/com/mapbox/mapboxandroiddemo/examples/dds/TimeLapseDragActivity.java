package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URL;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

public class TimeLapseDragActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private String TAG = "TimeLapseDragActivity";
  private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
  private static final String GEOJSON_LAYER_ID = "GEOJSON_LAYER_ID";
  private GeoJsonSource geoJsonSource;
  private LineLayer layer;
  private float lastRawX;
  private float currentRawX;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_time_lapse_drag);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    TimeLapseDragActivity.this.mapboxMap = mapboxMap;
    addSenegalGeojson();
    // Create FillLayer with GeoJSON source and add the FillLayer to the map
    layer = new LineLayer(GEOJSON_LAYER_ID, GEOJSON_SOURCE_ID);
    layer.setProperties(fillOpacity(0.6f),
      fillColor(Color.BLUE));
    mapboxMap.addLayer(layer);
    mapView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        currentRawX = event.getRawX();
        if (currentRawX > lastRawX) {

          lastRawX = currentRawX;
        }

        return false;
      }
    });

  }

  private void addSenegalGeojson() {
    try {
      // Load GeoJSONSource

      // Add GeoJsonSource to map
      mapboxMap.addSource(geoJsonSource);

    } catch (Throwable throwable) {
      Log.e("ClickOnLayerActivity", "Couldn't add GeoJsonSource to map", throwable);
    }
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