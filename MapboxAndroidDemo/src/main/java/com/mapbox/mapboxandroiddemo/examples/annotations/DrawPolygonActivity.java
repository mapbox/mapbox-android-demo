package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Draw a vector polygon on a map with the Mapbox Android SDK.
 */
public class DrawPolygonActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_annotation_polygon);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        drawPolygon(mapboxMap);
      }
    });
  }

  private void drawPolygon(MapboxMap mapboxMap) {
    List<LatLng> polygon = new ArrayList<>();
    polygon.add(new LatLng(45.522585, -122.685699));
    polygon.add(new LatLng(45.534611, -122.708873));
    polygon.add(new LatLng(45.530883, -122.678833));
    polygon.add(new LatLng(45.547115, -122.667503));
    polygon.add(new LatLng(45.530643, -122.660121));
    polygon.add(new LatLng(45.533529, -122.636260));
    polygon.add(new LatLng(45.521743, -122.659091));
    polygon.add(new LatLng(45.510677, -122.648792));
    polygon.add(new LatLng(45.515008, -122.664070));
    polygon.add(new LatLng(45.502496, -122.669048));
    polygon.add(new LatLng(45.515369, -122.678489));
    polygon.add(new LatLng(45.506346, -122.702007));
    polygon.add(new LatLng(45.522585, -122.685699));
    mapboxMap.addPolygon(new PolygonOptions()
      .addAll(polygon)
      .fillColor(Color.parseColor("#3bb2d0")));
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
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
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
}