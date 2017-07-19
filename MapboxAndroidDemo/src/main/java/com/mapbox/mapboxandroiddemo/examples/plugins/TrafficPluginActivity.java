package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin;

/**
 * Toggle the Mapbox Traffic plugin to display real-time traffic data on top
 * of your map (not all regions supported at the moment).
 */
public class TrafficPluginActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private TrafficPlugin trafficPlugin;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_traffic_plugin);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
        TrafficPluginActivity.this.trafficPlugin.setVisibility(true); // Enable the traffic view by default
      }
    });

    findViewById(R.id.traffic_toggle_fab).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (map != null) {
          trafficPlugin.setVisibility(!trafficPlugin.isVisible());
        }
      }
    });
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
}
