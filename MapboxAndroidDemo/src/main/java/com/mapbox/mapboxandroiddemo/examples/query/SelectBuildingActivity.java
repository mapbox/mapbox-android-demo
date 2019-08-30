package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

/**
 * Use the query feature to select a building, get its geometry, and draw a polygon highlighting it.
 */
public class SelectBuildingActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private MapView mapView;
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
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    SelectBuildingActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        style.addSource(new GeoJsonSource("source-id"));

        style.addLayer(new FillLayer("layer-id", "source-id").withProperties(
          PropertyFactory.fillColor(Color.parseColor("#8A8ACB"))
        ));

        mapboxMap.addOnMapClickListener(SelectBuildingActivity.this);
        Toast.makeText(SelectBuildingActivity.this,
          getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        final PointF finalPoint = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(finalPoint, "building");
        if (features.size() > 0) {
          GeoJsonSource selectedBuildingSource = style.getSourceAs("source-id");
          if (selectedBuildingSource != null) {
            selectedBuildingSource.setGeoJson(FeatureCollection.fromFeatures(features));
          }
        }
      }
    });
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
