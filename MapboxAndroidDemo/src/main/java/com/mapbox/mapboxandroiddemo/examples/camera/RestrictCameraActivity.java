package com.mapbox.mapboxandroiddemo.examples.camera;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

/**
 * Restrict the map camera to certain bounds.
 */
public class RestrictCameraActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final LatLngBounds AUSTRALIA_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(-9.136343, 109.372126))
    .include(new LatLng(-44.640488, 158.590484))
    .build();

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_camera_restrict);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Set bounds to Australia
        mapboxMap.setLatLngBoundsForCameraTarget(AUSTRALIA_BOUNDS);
        mapboxMap.setMinZoomPreference(2);

        // Visualise bounds area
        showBoundsArea();
        showCrosshair();
      }
    });
  }

  private void showBoundsArea() {
    GeoJsonSource geoJsonSource = new GeoJsonSource("source-id",
      FeatureCollection.fromFeatures(new Feature[] {
        Feature.fromGeometry(Point.fromLngLat(AUSTRALIA_BOUNDS.getNorthWest().getLongitude(),
          AUSTRALIA_BOUNDS.getNorthWest().getLatitude())),
        Feature.fromGeometry(Point.fromLngLat(AUSTRALIA_BOUNDS.getNorthEast().getLongitude(),
          AUSTRALIA_BOUNDS.getNorthWest().getLatitude())),
        Feature.fromGeometry(Point.fromLngLat(AUSTRALIA_BOUNDS.getSouthEast().getLongitude(),
          AUSTRALIA_BOUNDS.getNorthWest().getLatitude())),
        Feature.fromGeometry(Point.fromLngLat(AUSTRALIA_BOUNDS.getSouthWest().getLongitude(),
          AUSTRALIA_BOUNDS.getNorthWest().getLatitude()))
      }));

    mapboxMap.getStyle().addSource(geoJsonSource);

    FillLayer boundsAreaFillLayer = new FillLayer("layer-id", "source-id");
    boundsAreaFillLayer.setProperties(
      fillColor(Color.RED),
      fillOpacity(.25f)
    );
    mapboxMap.getStyle().addLayer(boundsAreaFillLayer);
  }

  private void showCrosshair() {
    View crosshair = new View(this);
    crosshair.setLayoutParams(new FrameLayout.LayoutParams(15, 15, Gravity.CENTER));
    crosshair.setBackgroundColor(Color.GREEN);
    mapView.addView(crosshair);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
