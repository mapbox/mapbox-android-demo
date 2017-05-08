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
        .target(new LatLng(25.255377,55.3089185))
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
        add(new LatLng(25.2593949852332, 55.29405310619586));
        add(new LatLng(25.274581868369467, 55.304514658616824));
        add(new LatLng(25.26221673563208, 55.30561587467446));
        add(new LatLng(25.276075557512, 55.33397218784205));
        add(new LatLng(25.257984085427992, 55.32653897955248));
        add(new LatLng(25.24951834246839, 55.342690148204895));
        add(new LatLng(25.23922586039734, 55.2987332743956));
        add(new LatLng(25.247111307836562, 55.300660402469305));
        add(new LatLng(25.245700265346272, 55.28414216178564));
        add(new LatLng(25.255826206983926, 55.286619897906235));
        add(new LatLng(25.251012339983248, 55.2946954822325));
        add(new LatLng(25.259146004097133, 55.29405310619586));
      }
    };

    static final List<List<LatLng>> STAR_SHAPE_HOLES = new ArrayList<List<LatLng>>() {
      {
        add(new ArrayList<>(new ArrayList<LatLng>() {
          {
            add(new LatLng(25.25879232126489, 55.301700903466696));
            add(new LatLng(25.259029515191017, 55.31184200511552));
            add(new LatLng(25.254759953545147, 55.30353679255319));
            add(new LatLng(25.258080736691085, 55.29942789792764));
            add(new LatLng(25.258871385948012, 55.30161348015554));
          }
        }));
        add(new ArrayList<>(new ArrayList<LatLng>() {
          {
            add(new LatLng(25.261638617862275, 55.32084660399491));
            add(new LatLng(25.24922511321995, 55.310705502345996));
            add(new LatLng(25.249699537976696, 55.32626684797782));
            add(new LatLng(25.26179674348606, 55.32084660399491));
          }
        }));
      }
    };
  }
}
