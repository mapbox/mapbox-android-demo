package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonElement;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.Feature;

import java.util.List;
import java.util.Map;

public class QueryFeatureActivity extends AppCompatActivity {

  private MapView mapView;
  private Marker featureMarker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_query_feature);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
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
              if (feature.getProperties() != null) {
                for (Map.Entry<String, JsonElement> entry : feature.getProperties().entrySet()) {
                  stringBuilder.append(String.format("%s - %s", entry.getKey(), entry.getValue()));
                  stringBuilder.append(System.getProperty("line.separator"));
                }

                featureMarker = mapboxMap.addMarker(new MarkerViewOptions()
                    .position(point)
                    .title("Properties:")
                    .snippet(stringBuilder.toString())
                );

              } else {
                property = "No feature properties found";
                featureMarker = mapboxMap.addMarker(new MarkerViewOptions()
                    .position(point)
                    .snippet(property)
                );
              }
            } else {
              featureMarker = mapboxMap.addMarker(new MarkerViewOptions()
                  .position(point)
                  .snippet("No feature properties found")
              );
            }

            mapboxMap.selectMarker(featureMarker);

          }
        });
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
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