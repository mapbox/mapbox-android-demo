package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeta;
import com.mapbox.turf.TurfTransformation;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.turf.TurfConstants.UNIT_MILES;

/**
 * Use {@link TurfTransformation#circle(Point, double, int, String)} to draw a hollow circle
 * (i.e. ring) around a center coordinate.
 */
public class TurfRingActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {
  private static final String CIRCLE_GEOJSON_SOURCE_ID = "CIRCLE_GEOJSON_SOURCE_ID";
  private static final String CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID";
  private static final int OUTER_CIRCLE_MILE_RADIUS = 1;
  private static final double MILE_DIFFERENCE_BETWEEN_CIRCLES = .2;
  private static final int CIRCLE_STEPS = 360;
  private static final Point POINT_IN_MIDDLE_OF_CIRCLE = Point.fromLngLat(-115.150738, 36.16218);
  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_hollow_circle);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        TurfRingActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder()
            .fromUri(Style.LIGHT)
            .withSource(new GeoJsonSource(CIRCLE_GEOJSON_SOURCE_ID))
            .withLayer(new FillLayer(CIRCLE_LAYER_ID, CIRCLE_GEOJSON_SOURCE_ID).withProperties(
              fillColor(Color.RED)
            )), new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {
                moveRing(POINT_IN_MIDDLE_OF_CIRCLE);

                TurfRingActivity.this.mapboxMap.addOnMapClickListener(TurfRingActivity.this);

                Toast.makeText(TurfRingActivity.this, getString(R.string.tap_on_map),
                  Toast.LENGTH_SHORT).show();
              }
            }
        );
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    moveRing(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
    return true;
  }

  private void moveRing(Point centerPoint) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Use Turf to calculate the coordinates for the outer ring of the final Polygon
        Polygon outerCirclePolygon = getTurfPolygon(OUTER_CIRCLE_MILE_RADIUS, centerPoint);

        // Use Turf to calculate the coordinates for the inner ring of the final Polygon
        Polygon innerCirclePolygon = getTurfPolygon(
          OUTER_CIRCLE_MILE_RADIUS - MILE_DIFFERENCE_BETWEEN_CIRCLES, centerPoint);

        GeoJsonSource outerCircleSource = style.getSourceAs(CIRCLE_GEOJSON_SOURCE_ID);

        if (outerCircleSource != null) {
          // Use the two Polygon objects above to create the final Polygon that visually represents the ring.
          outerCircleSource.setGeoJson(Polygon.fromOuterInner(
            // Create outer LineString
            LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
            // Create inter LineString
            LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
          ));
        }
      }
    });
  }

  private Polygon getTurfPolygon(@NonNull double radius, Point centerPoint) {
    return TurfTransformation.circle(centerPoint, radius, CIRCLE_STEPS, UNIT_MILES);
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
