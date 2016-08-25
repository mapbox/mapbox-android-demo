package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;

import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

public class SelectBuildingActivity extends AppCompatActivity {

  private MapView mapView;
  private com.mapbox.mapboxsdk.annotations.Polygon selectedBuilding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_query_select_building);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
          @Override
          public void onMapClick(@NonNull LatLng latLng) {

            if (selectedBuilding != null) {
              mapboxMap.removePolygon(selectedBuilding);
            }

            final PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
            List<Feature> features = mapboxMap.queryRenderedFeatures(point, "building");

            if (features.size() > 0) {
              String featureID = features.get(0).getId();

              for (int a = 0; a < features.size(); a++) {
                if (featureID.equals(features.get(a).getId())) {
                  if (features.get(a).getGeometry() instanceof Polygon) {

                    List<LatLng> list = new ArrayList<>();
                    for (int i = 0; i < ((Polygon) features.get(a).getGeometry()).getCoordinates().size(); i++) {
                      for (int j = 0; j < ((Polygon) features.get(a).getGeometry()).getCoordinates().get(i).size(); j++) {
                        list.add(new LatLng(
                            ((Polygon) features.get(a).getGeometry()).getCoordinates().get(i).get(j).getLatitude(),
                            ((Polygon) features.get(a).getGeometry()).getCoordinates().get(i).get(j).getLongitude()
                        ));
                      }
                    }

                    selectedBuilding = mapboxMap.addPolygon(new PolygonOptions().addAll(list).fillColor(Color.parseColor("#8A8ACB")));
                  }
                }
              }
            }
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