package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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
import com.mapbox.mapboxsdk.maps.Style;

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
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    QueryFeatureActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        mapboxMap.addOnMapClickListener(QueryFeatureActivity.this);
        Toast.makeText(QueryFeatureActivity.this,
          getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {

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

    return true;
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
