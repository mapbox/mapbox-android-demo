package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.utils.CustomFragmentForExample;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

public class MiniWindowActivity extends AppCompatActivity implements MapboxMap.OnCameraMoveStartedListener {

  MapCameraDataFromActivityToFragment mapCameraDataFromActivityToFragment;
  private MapView mapView;
  private MapboxMap mainLargeMapboxMap;
  private LatLng panamaCanal = new LatLng(9.143803, -79.7285160);

  private LatLng postionOfMainLargeMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_mini_window);

    mapView = (MapView) findViewById(R.id.main_mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        MiniWindowActivity.this.mainLargeMapboxMap = mapboxMap;

      }
    });

    // Create supportMapFragment
    SupportMapFragment mapFragment;
    if (savedInstanceState == null) {

      // Create fragment
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


      // Build mapboxMap
      MapboxMapOptions options = new MapboxMapOptions();
      options.styleUrl(Style.MAPBOX_STREETS);
      options.camera(new CameraPosition.Builder()
        .target(panamaCanal)
        .zoom(9)
        .build());

      // Create map fragment
      mapFragment = SupportMapFragment.newInstance(options);


      // Add map fragment to parent container
      transaction.add(R.id.mini_map_fragment_container, mapFragment, "com.mapbox.map");
      transaction.commit();

    } else {
      mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
    }


    mapFragment.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        // Customize map with markers, polylines, etc.

        PolygonOptions polygonArea = new PolygonOptions()
          .add(AUSTRALIA_BOUNDS.getNorthWest())
          .add(AUSTRALIA_BOUNDS.getNorthEast())
          .add(AUSTRALIA_BOUNDS.getSouthEast())
          .add(AUSTRALIA_BOUNDS.getSouthWest());
        polygonArea.alpha(0.25f);
        polygonArea.fillColor(Color.parseColor("#ff9a00"));
        mapboxMap.addPolygon(polygonArea);


      }
    });

  }

  public interface MapCameraDataFromActivityToFragment {
    void sendCameraTargetPosition(LatLng latLng);
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