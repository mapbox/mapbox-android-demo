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
import static com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity.Config.RED_COLOR;
import static com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity.Config.STAR_SHAPE_HOLES;
import static com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity.Config.STAR_SHAPE_POINTS;

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
        .target(new LatLng(45.520486, -122.673541))
        .zoom(12)
        .tilt(40)
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
      .addAll(STAR_SHAPE_POINTS)
      .addAllHoles(STAR_SHAPE_HOLES)
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

    static final List<LatLng> STAR_SHAPE_POINTS = new ArrayList<LatLng>() {
      {
        add(new LatLng(45.522585, -122.685699));
        add(new LatLng(45.534611, -122.708873));
        add(new LatLng(45.530883, -122.678833));
        add(new LatLng(45.547115, -122.667503));
        add(new LatLng(45.530643, -122.660121));
        add(new LatLng(45.533529, -122.636260));
        add(new LatLng(45.521743, -122.659091));
        add(new LatLng(45.510677, -122.648792));
        add(new LatLng(45.515008, -122.664070));
        add(new LatLng(45.502496, -122.669048));
        add(new LatLng(45.515369, -122.678489));
        add(new LatLng(45.506346, -122.702007));
        add(new LatLng(45.522585, -122.685699));
      }
    };

    static final List<List<LatLng>> STAR_SHAPE_HOLES = new ArrayList<List<LatLng>>() {
      {
        add(new ArrayList<>(new ArrayList<LatLng>() {
          {
            add(new LatLng(45.521743, -122.669091));
            add(new LatLng(45.530483, -122.676833));
            add(new LatLng(45.520483, -122.676833));
            add(new LatLng(45.521743, -122.669091));
          }
        }));
        add(new ArrayList<>(new ArrayList<LatLng>() {
          {
            add(new LatLng(45.529743, -122.662791));
            add(new LatLng(45.525543, -122.662791));
            add(new LatLng(45.525543, -122.660));
            add(new LatLng(45.527743, -122.660));
            add(new LatLng(45.529743, -122.662791));
          }
        }));
      }
    };
  }
}
