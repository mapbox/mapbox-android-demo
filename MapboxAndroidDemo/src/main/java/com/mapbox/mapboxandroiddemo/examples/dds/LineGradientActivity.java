package com.mapbox.mapboxandroiddemo.examples.dds;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lineProgress;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineGradient;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Use data-driven styling properties to add color gradients to a LineLayer line.
 */
public class LineGradientActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private List<Point> routeCoordinates;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_line_gradient);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        initCoordinates();

        // Create the LineString from the list of coordinates and then make a GeoJSON
        // FeatureCollection so we can add the line to our map as a layer.
        LineString lineString = LineString.fromLngLats(routeCoordinates);

        FeatureCollection featureCollection = FeatureCollection.fromFeature(Feature.fromGeometry(lineString));

        style.addSource(new GeoJsonSource("line-source", featureCollection,
            new GeoJsonOptions().withLineMetrics(true)));

        // The layer properties for our line. This is where we set the gradient colors, set the
        // line width, etc
        style.addLayer(new LineLayer("linelayer", "line-source").withProperties(
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND),
            lineWidth(14f),
            lineGradient(interpolate(
              linear(), lineProgress(),
              stop(0f, rgb(6, 1, 255)), // blue
              stop(0.1f, rgb(59, 118, 227)), // royal blue
              stop(0.3f, rgb(7, 238, 251)), // cyan
              stop(0.5f, rgb(0, 255, 42)), // lime
              stop(0.7f, rgb(255, 252, 0)), // yellow
              stop(1f, rgb(255, 30, 0)) // red
            ))));
      }
    });
  }

  private void initCoordinates() {
    routeCoordinates = new ArrayList<>();
    routeCoordinates.add(Point.fromLngLat(-77.044211, 38.852924));
    routeCoordinates.add(Point.fromLngLat(-77.045659, 38.860158));
    routeCoordinates.add(Point.fromLngLat(-77.044232, 38.862326));
    routeCoordinates.add(Point.fromLngLat(-77.040879, 38.865454));
    routeCoordinates.add(Point.fromLngLat(-77.039936, 38.867698));
    routeCoordinates.add(Point.fromLngLat(-77.040338, 38.86943));
    routeCoordinates.add(Point.fromLngLat(-77.04264, 38.872528));
    routeCoordinates.add(Point.fromLngLat(-77.03696, 38.878424));
    routeCoordinates.add(Point.fromLngLat(-77.032309, 38.87937));
    routeCoordinates.add(Point.fromLngLat(-77.030056, 38.880945));
    routeCoordinates.add(Point.fromLngLat(-77.027645, 38.881779));
    routeCoordinates.add(Point.fromLngLat(-77.026946, 38.882645));
    routeCoordinates.add(Point.fromLngLat(-77.026942, 38.885502));
    routeCoordinates.add(Point.fromLngLat(-77.028054, 38.887449));
    routeCoordinates.add(Point.fromLngLat(-77.02806, 38.892088));
    routeCoordinates.add(Point.fromLngLat(-77.03364, 38.892108));
    routeCoordinates.add(Point.fromLngLat(-77.033643, 38.899926));
  }

  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
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
  public void onStop() {
    super.onStop();
    mapView.onStop();
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