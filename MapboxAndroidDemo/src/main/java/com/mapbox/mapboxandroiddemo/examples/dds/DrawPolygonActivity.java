package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Draw a vector polygon on a map with the Mapbox Android SDK.
 */
public class DrawPolygonActivity extends AppCompatActivity {

  private MapView mapView;

  private static final List<List<Point>> POINTS = new ArrayList<>();
  private static final List<Point> OUTER_POINTS = new ArrayList<>();

  static {
    OUTER_POINTS.add(Point.fromLngLat(-122.685699, 45.522585));
    OUTER_POINTS.add(Point.fromLngLat(-122.708873, 45.534611));
    OUTER_POINTS.add(Point.fromLngLat(-122.678833, 45.530883));
    OUTER_POINTS.add(Point.fromLngLat(-122.667503, 45.547115));
    OUTER_POINTS.add(Point.fromLngLat(-122.660121, 45.530643));
    OUTER_POINTS.add(Point.fromLngLat(-122.636260, 45.533529));
    OUTER_POINTS.add(Point.fromLngLat(-122.659091, 45.521743));
    OUTER_POINTS.add(Point.fromLngLat(-122.648792, 45.510677));
    OUTER_POINTS.add(Point.fromLngLat(-122.664070, 45.515008));
    OUTER_POINTS.add(Point.fromLngLat(-122.669048, 45.502496));
    OUTER_POINTS.add(Point.fromLngLat(-122.678489, 45.515369));
    OUTER_POINTS.add(Point.fromLngLat(-122.702007, 45.506346));
    OUTER_POINTS.add(Point.fromLngLat(-122.685699, 45.522585));
    POINTS.add(OUTER_POINTS);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_draw_polygon);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            style.addSource(new GeoJsonSource("source-id", Polygon.fromLngLats(POINTS)));
            style.addLayerBelow(new FillLayer("layer-id", "source-id").withProperties(
              fillColor(Color.parseColor("#3bb2d0"))), "settlement-label"
            );
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
