package com.mapbox.mapboxandroiddemo.examples.camera;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
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

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

/**
 * Restrict the map camera to certain bounds.
 */
public class RestrictCameraActivity extends AppCompatActivity implements OnMapReadyCallback {
  private static final LatLng BOUND_CORNER_NW = new LatLng(-8.491377105132457, 108.26584125231903);
  private static final LatLng BOUND_CORNER_SE = new LatLng(-42.73740968175186, 158.19629538046348);
  private static final LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder()
    .include(BOUND_CORNER_NW)
    .include(BOUND_CORNER_SE)
    .build();

  private final List<List<Point>> points = new ArrayList<>();
  private final List<Point> outerPoints = new ArrayList<>();
  private MapView mapView;

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

    mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Set the boundary area for the map camera
        mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);

        // Set the minimum zoom level of the map camera
        mapboxMap.setMinZoomPreference(2);

        showBoundsArea(style);

        showCrosshair();
      }
    });
  }

  /**
   * Add a FillLayer to show the boundary area
   *
   * @param loadedMapStyle a Style object which has been loaded by the map
   */
  private void showBoundsArea(@NonNull Style loadedMapStyle) {
    outerPoints.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.getNorthWest().getLongitude(),
      RESTRICTED_BOUNDS_AREA.getNorthWest().getLatitude()));
    outerPoints.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.getNorthEast().getLongitude(),
      RESTRICTED_BOUNDS_AREA.getNorthEast().getLatitude()));
    outerPoints.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.getSouthEast().getLongitude(),
      RESTRICTED_BOUNDS_AREA.getSouthEast().getLatitude()));
    outerPoints.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.getSouthWest().getLongitude(),
      RESTRICTED_BOUNDS_AREA.getSouthWest().getLatitude()));
    outerPoints.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.getNorthWest().getLongitude(),
      RESTRICTED_BOUNDS_AREA.getNorthWest().getLatitude()));
    points.add(outerPoints);

    loadedMapStyle.addSource(new GeoJsonSource("source-id",
      Polygon.fromLngLats(points)));

    loadedMapStyle.addLayer(new FillLayer("layer-id", "source-id").withProperties(
      fillColor(Color.RED),
      fillOpacity(.25f)
    ));
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
