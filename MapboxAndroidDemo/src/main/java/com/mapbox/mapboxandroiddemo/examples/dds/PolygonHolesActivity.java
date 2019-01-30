package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxandroiddemo.examples.dds.PolygonHolesActivity.Config.BLUE_COLOR;
import static com.mapbox.mapboxandroiddemo.examples.dds.PolygonHolesActivity.Config.HOLE_COORDINATES;
import static com.mapbox.mapboxandroiddemo.examples.dds.PolygonHolesActivity.Config.POLYGON_COORDINATES;
import static com.mapbox.mapboxandroiddemo.examples.dds.PolygonHolesActivity.Config.RED_COLOR;

/**
 * Add holes to a polygon drawn on top of the map.
 */
public class PolygonHolesActivity extends AppCompatActivity implements OnMapReadyCallback {
  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // Configure initial map state
    MapboxMapOptions options = new MapboxMapOptions()
      .attributionTintColor(RED_COLOR)
      .compassFadesWhenFacingNorth(false)
      .camera(new CameraPosition.Builder()
        .target(new LatLng(25.255377, 55.3089185))
        .zoom(11.86)
        .tilt(10)
        .build());

    mapView = new MapView(this, options);
    mapView.setId(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
    setContentView(mapView);
  }

  @Override
  public void onMapReady(final MapboxMap map) {
    this.mapboxMap = map;
    map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        LineString outerLineString = LineString.fromLngLats(POLYGON_COORDINATES);
        LineString innerLineString = LineString.fromLngLats(HOLE_COORDINATES.get(0));
        LineString secondInnerLineString = LineString.fromLngLats(HOLE_COORDINATES.get(1));

        List<LineString> innerList = new ArrayList<>();
        innerList.add(innerLineString);
        innerList.add(secondInnerLineString);

        style.addSource(new GeoJsonSource("source-id",
          Feature.fromGeometry(Polygon.fromOuterInner(outerLineString, innerList))));

        style.addLayer(new FillLayer("layer-id", "source-id").withProperties(
          PropertyFactory.fillColor(BLUE_COLOR)
        ));
      }
    });
  }

  static final class Config {
    static final int BLUE_COLOR = Color.parseColor("#3bb2d0");
    static final int RED_COLOR = Color.parseColor("#AF0000");

    static final List<Point> POLYGON_COORDINATES = new ArrayList<Point>() {
      {
        add(Point.fromLngLat(55.30122473231012, 25.26476622289597 ));
        add(Point.fromLngLat(55.29743486255916, 25.25827212207261));
        add(Point.fromLngLat(55.28978863411328, 25.251356725509737));
        add(Point.fromLngLat(55.300027931336984, 25.246425506635504));
        add(Point.fromLngLat(55.307474692951274, 25.244200378933655));
        add(Point.fromLngLat(55.31212891895635, 25.256408010450187));
        add(Point.fromLngLat(55.30774064871093, 25.26266169122738));
        add(Point.fromLngLat(55.301357710197806, 25.264946609615492));
        add(Point.fromLngLat(55.30122473231012, 25.26476622289597 ));
      }
    };

    static final List<List<Point>> HOLE_COORDINATES = new ArrayList<List<Point>>() {
      {
        add(new ArrayList<>(new ArrayList<Point>() {
          {
            add(Point.fromLngLat(55.30084858315658, 25.256531695820797));
            add(Point.fromLngLat(55.298280197635705, 25.252243254705405));
            add(Point.fromLngLat(55.30163885563897, 25.250501032248863));
            add(Point.fromLngLat(55.304059065092645, 25.254700192612702));
            add(Point.fromLngLat(55.30084858315658, 25.256531695820797));
          }
        }));
        add(new ArrayList<>(new ArrayList<Point>() {
          {
            add(Point.fromLngLat(55.30173763969924, 25.262517391695198));
            add(Point.fromLngLat(55.301095543307355, 25.26122200491396));
            add(Point.fromLngLat(55.30396028103232, 25.259479911263526));
            add(Point.fromLngLat(55.30489872958182, 25.261132667394975));
            add(Point.fromLngLat(55.30173763969924, 25.262517391695198));
          }
        }));
      }
    };
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
