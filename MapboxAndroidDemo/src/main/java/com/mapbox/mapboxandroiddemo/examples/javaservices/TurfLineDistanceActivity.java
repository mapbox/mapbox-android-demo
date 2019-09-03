package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.turf.TurfConstants.UNIT_KILOMETERS;

/**
 * Use {@link com.mapbox.turf.TurfMeasurement#distance(Point, Point)} to measure the distance of a drawn line.
 */
public class TurfLineDistanceActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID";
  private static final String LINE_LAYER_ID = "LINE_LAYER_ID";

  // Adjust private static final variables below to change the example's UI
  private static final String STYLE_URI = "mapbox://styles/mapbox/cjv6rzz4j3m4b1fqcchuxclhb";
  private static final int CIRCLE_COLOR = Color.RED;
  private static final int LINE_COLOR = CIRCLE_COLOR;
  private static final float CIRCLE_RADIUS = 6f;
  private static final float LINE_WIDTH = 4f;
  private static final String DISTANCE_UNITS = UNIT_KILOMETERS; // DISTANCE_UNITS must be equal to a String
  // found in the TurfConstants class

  private List<Point> pointList = new ArrayList<>();
  private MapView mapView;
  private MapboxMap mapboxMap;
  private TextView lineLengthTextView;
  private double totalLineDistance = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_turf_measure_line);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {

        lineLengthTextView = findViewById(R.id.line_length_textView);

        // DISTANCE_UNITS must be equal to a String found in the TurfConstants class
        lineLengthTextView.setText(String.format(getString(R.string.line_distance_textview),
          DISTANCE_UNITS, String.valueOf(totalLineDistance)));

        TurfLineDistanceActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder()
              .fromUri(STYLE_URI)

              // Add the source to the map
              .withSource(new GeoJsonSource(SOURCE_ID))

              // Style and add the CircleLayer to the map
              .withLayer(new CircleLayer(CIRCLE_LAYER_ID, SOURCE_ID).withProperties(
                circleColor(CIRCLE_COLOR),
                circleRadius(CIRCLE_RADIUS)
              ))

            // Style and add the LineLayer to the map. The LineLayer is placed below the CircleLayer.
            .withLayerBelow(new LineLayer(LINE_LAYER_ID, SOURCE_ID).withProperties(
              lineColor(LINE_COLOR),
              lineWidth(LINE_WIDTH),
              lineJoin(LINE_JOIN_ROUND)
            ), CIRCLE_LAYER_ID), new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {
                TurfLineDistanceActivity.this.mapboxMap.addOnMapClickListener(TurfLineDistanceActivity.this);
                Toast.makeText(TurfLineDistanceActivity.this, getString(
                  R.string.line_distance_tap_instruction), Toast.LENGTH_SHORT).show();
              }
            }
        );
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    addClickPointToLine(point);
    return true;
  }

  /**
   * Handle the map click location and re-draw the circle and line data.
   *
   * @param clickLatLng where the map was tapped on.
   */
  private void addClickPointToLine(@NonNull LatLng clickLatLng) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Get the source from the map's style
        GeoJsonSource geoJsonSource = style.getSourceAs(SOURCE_ID);
        if (geoJsonSource != null) {

          pointList.add(Point.fromLngLat(clickLatLng.getLongitude(), clickLatLng.getLatitude()));

          int pointListSize = pointList.size();

          double distanceBetweenLastAndSecondToLastClickPoint = 0;

          // Make the Turf calculation between the last tap point and the second-to-last tap point.
          if (pointList.size() >= 2) {
            distanceBetweenLastAndSecondToLastClickPoint = TurfMeasurement.distance(
              pointList.get(pointListSize - 2), pointList.get(pointListSize - 1));
          }

          // Re-draw the new GeoJSON data
          if (pointListSize >= 2 && distanceBetweenLastAndSecondToLastClickPoint > 0) {

            // Add the last TurfMeasurement#distance calculated distance to the total line distance.
            totalLineDistance += distanceBetweenLastAndSecondToLastClickPoint;

            // Adjust the TextView to display the new total line distance.
            // DISTANCE_UNITS must be equal to a String found in the TurfConstants class
            lineLengthTextView.setText(String.format(getString(R.string.line_distance_textview), DISTANCE_UNITS,
              String.valueOf(totalLineDistance)));

            // Set the specific source's GeoJSON data
            geoJsonSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(pointList)));
          }
        }
      }
    });
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
