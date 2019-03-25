package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Combine different styled lines with a SymbolLayer icon. Great for showing past and upcoming travel along a certain
 * path.
 */
public class DottedAndSolidLineIconActivity extends AppCompatActivity {

  private MapView mapView;

  private static final Point NEWARK_AIRPORT_POINT = Point.fromLngLat(-74.17799, 40.69297);
  private static final Point SAN_FRANCISCO_AIRPORT_POINT = Point.fromLngLat(-122.38709, 37.616714);
  private static final Point MINNEAPOLIS_CITY_AIRPORT_POINT = Point.fromLngLat(-93.224, 44.8815);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_dotted_and_solid_line_icon);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Add the plane SymbolLayer icon to the map
            style.addImage("plane-icon-id", BitmapFactory.decodeResource(
              getResources(), R.drawable.ic_action_plane));
            style.addSource(new GeoJsonSource("plane-source-id", MINNEAPOLIS_CITY_AIRPORT_POINT));
            style.addLayer(new SymbolLayer("plane-symbol-layer-id", "plane-source-id").withProperties(
              iconImage("plane-icon-id"),
              iconSize(1.2f),
              iconIgnorePlacement(true),
              iconAllowOverlap(true)
            ));

            // Add the marker icons which represent the plane's origin and destination locations
            style.addImage("destination-and-origin-icon-id", BitmapFactory.decodeResource(
              getResources(), R.drawable.red_marker));
            style.addSource(new GeoJsonSource("destination-and-origin-source-id",
              FeatureCollection.fromFeatures(
                new Feature[] {
                  Feature.fromGeometry(NEWARK_AIRPORT_POINT),
                  Feature.fromGeometry(SAN_FRANCISCO_AIRPORT_POINT)
                })));
            style.addLayer(new SymbolLayer("destination-and-origin-symbol-layer-id",
              "destination-and-origin-source-id").withProperties(
              iconImage("destination-and-origin-icon-id"),
              iconSize(1f),
              iconIgnorePlacement(true),
              iconOffset(new Float[] {0f, -8f}),
              iconAllowOverlap(true)
            ));

            // Add the dotted line LineLayer and use runtime-styling to style it
            List<Point> dotList = new ArrayList<>();
            dotList.add(NEWARK_AIRPORT_POINT);
            dotList.add(MINNEAPOLIS_CITY_AIRPORT_POINT);
            style.addSource(new GeoJsonSource("dotted-line-source-id",
              Feature.fromGeometry(LineString.fromLngLats(dotList))));
            style.addLayerBelow(
              new LineLayer("dotted-line-layer-id", "dotted-line-source-id")
                .withProperties(lineWidth(4.5f),
                  lineColor(Color.RED),
                  lineDasharray(new Float[] {1f, 1f})), "plane-symbol-layer-id");


            // Add the solid line LineLayer and use runtime-styling to style it
            List<Point> solidLineList = new ArrayList<>();
            solidLineList.add(MINNEAPOLIS_CITY_AIRPORT_POINT);
            solidLineList.add(SAN_FRANCISCO_AIRPORT_POINT);
            style.addSource(new GeoJsonSource("solid-line-source-id",
              Feature.fromGeometry(LineString.fromLngLats(solidLineList))));
            style.addLayerBelow(new LineLayer("solid-line-layer-id", "solid-line-source-id")
              .withProperties(lineWidth(4.5f),
                lineColor(Color.BLUE)), "plane-symbol-layer-id");
          }
        });
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
