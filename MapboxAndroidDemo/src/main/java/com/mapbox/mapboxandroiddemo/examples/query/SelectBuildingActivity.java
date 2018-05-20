package com.mapbox.mapboxandroiddemo.examples.query;
// #-code-snippet: select-building-activity full-java
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Polygon;
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
 * Use the query feature to select a building, get its geometry, and draw a polygon highlighting it.
 */
public class SelectBuildingActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener  {

  private MapView mapView;
  private com.mapbox.mapboxsdk.annotations.Polygon selectedBuilding;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_query_select_building);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    SelectBuildingActivity.this.mapboxMap = mapboxMap;
    mapboxMap.addOnMapClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {

    if (selectedBuilding != null) {
      mapboxMap.removePolygon(selectedBuilding);
    }

    final PointF finalPoint = mapboxMap.getProjection().toScreenLocation(point);
    List<Feature> features = mapboxMap.queryRenderedFeatures(finalPoint, "building");

    if (features.size() > 0) {
      String featureId = features.get(0).id();

      for (int a = 0; a < features.size(); a++) {
        if (featureId.equals(features.get(a).id())) {
          if (features.get(a).geometry() instanceof Polygon) {

            List<LatLng> list = new ArrayList<>();
            for (int i = 0; i < ((Polygon) features.get(a).geometry()).coordinates().size(); i++) {
              for (int j = 0;
                   j < ((Polygon) features.get(a).geometry()).coordinates().get(i).size(); j++) {
                list.add(new LatLng(
                  ((Polygon) features.get(a).geometry()).coordinates().get(i).get(j).latitude(),
                  ((Polygon) features.get(a).geometry()).coordinates().get(i).get(j).longitude()
                ));
              }
            }

            selectedBuilding = mapboxMap.addPolygon(new PolygonOptions()
              .addAll(list)
              .fillColor(Color.parseColor("#8A8ACB"))
            );
          }
        }
      }
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
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
// #-end-code-snippet: select-building-activity full-java