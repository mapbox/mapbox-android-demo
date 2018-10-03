package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity.Config.BLUE_COLOR;
import static com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity.Config.HOLE_COORDINATES;
import static com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity.Config.POLYGON_COORDINATES;
import static com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity.Config.RED_COLOR;

/**
 * Add holes to a polygon drawn on top of the map.
 */
public class PolygonHolesActivity extends AppCompatActivity implements OnMapReadyCallback {
  private MapView mapView;
  private MapboxMap mapboxMap;
  private Polygon polygon;

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
      .styleUrl(Style.MAPBOX_STREETS)
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
  public void onMapReady(MapboxMap map) {
    mapboxMap = map;
    polygon = mapboxMap.addPolygon(new PolygonOptions()
      .addAll(POLYGON_COORDINATES)
      .addAllHoles(HOLE_COORDINATES)
      .fillColor(BLUE_COLOR));
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

  static final class Config {
    static final int BLUE_COLOR = Color.parseColor("#3bb2d0");
    static final int RED_COLOR = Color.parseColor("#AF0000");

    static final List<LatLng> POLYGON_COORDINATES = new ArrayList<LatLng>() {
      {
        add(new LatLng(25.26476622289597, 55.30122473231012));
        add(new LatLng(25.25827212207261, 55.29743486255916));
        add(new LatLng(25.251356725509737, 55.28978863411328));
        add(new LatLng(25.246425506635504, 55.300027931336984));
        add(new LatLng(25.244200378933655, 55.307474692951274));
        add(new LatLng(25.256408010450187, 55.31212891895635));
        add(new LatLng(25.26266169122738, 55.30774064871093));
        add(new LatLng(25.264946609615492, 55.301357710197806));
      }
    };

    static final List<List<LatLng>> HOLE_COORDINATES = new ArrayList<List<LatLng>>() {
      {
        add(new ArrayList<>(new ArrayList<LatLng>() {
          {
            add(new LatLng(25.256531695820797, 55.30084858315658));
            add(new LatLng(25.252243254705405, 55.298280197635705));
            add(new LatLng(25.250501032248863, 55.30163885563897));
            add(new LatLng(25.254700192612702, 55.304059065092645));
            add(new LatLng(25.256531695820797, 55.30084858315658));
          }
        }));
        add(new ArrayList<>(new ArrayList<LatLng>() {
          {
            add(new LatLng(25.262517391695198, 55.30173763969924));
            add(new LatLng(25.26122200491396, 55.301095543307355));
            add(new LatLng(25.259479911263526, 55.30396028103232));
            add(new LatLng(25.261132667394975, 55.30489872958182));
            add(new LatLng(25.262517391695198, 55.30173763969924));
          }
        }));
      }
    };
  }
}
