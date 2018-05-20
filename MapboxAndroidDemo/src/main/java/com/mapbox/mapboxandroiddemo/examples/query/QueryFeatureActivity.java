package com.mapbox.mapboxandroiddemo.examples.query;
// #-code-snippet: query-feature-activity full-java
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;
import java.util.Map;

/**
 * Display map property information for a clicked map feature
 */
public class QueryFeatureActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private MapView mapView;
  private Marker featureMarker;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_query_feature);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    QueryFeatureActivity.this.mapboxMap = mapboxMap;
    mapboxMap.addOnMapClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    if (featureMarker != null) {
      mapboxMap.removeMarker(featureMarker);
    }

    final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
    List<Feature> features = mapboxMap.queryRenderedFeatures(pixel);

    if (features.size() > 0) {
      Feature feature = features.get(0);

      String property;

      StringBuilder stringBuilder = new StringBuilder();
      if (feature.properties() != null) {
        for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
          stringBuilder.append(String.format("%s - %s", entry.getKey(), entry.getValue()));
          stringBuilder.append(System.getProperty("line.separator"));
        }

        featureMarker = mapboxMap.addMarker(new MarkerOptions()
          .position(point)
          .title(getString(R.string.query_feature_marker_title))
          .snippet(stringBuilder.toString())
        );

      } else {
        property = getString(R.string.query_feature_marker_snippet);
        featureMarker = mapboxMap.addMarker(new MarkerOptions()
          .position(point)
          .snippet(property)
        );
      }
    } else {
      featureMarker = mapboxMap.addMarker(new MarkerOptions()
        .position(point)
        .snippet(getString(R.string.query_feature_marker_snippet))
      );
    }
    mapboxMap.selectMarker(featureMarker);
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
// #-end-code-snippet: query-feature-activity full-java