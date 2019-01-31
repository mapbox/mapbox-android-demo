package com.mapbox.mapboxandroiddemo.examples.labs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

public class InsetMapActivity extends AppCompatActivity {

  private static final String STYLE_URL = "mapbox://styles/mapbox/cj5l80zrp29942rmtg0zctjto";
  private static final String INSET_FRAGMENT_TAG = "com.mapbox.insetMapFragment";
  private static final int ZOOM_DISTANCE_BETWEEN_MAIN_AND_INSET_MAPS = 3;

  private MapView mainMapView;
  private MapboxMap mainMapboxMap;
  private MapboxMap insetMapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_inset_map);

    mainMapView = findViewById(R.id.mapView);
    mainMapView.onCreate(savedInstanceState);
    mainMapView.getMapAsync(mainLargeMapReadyCallback);

    SupportMapFragment insetMapFragment =
      (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(INSET_FRAGMENT_TAG);

    if (insetMapFragment == null) {
      // Create fragment transaction for the inset fragment
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

      // Build map fragment options
      MapboxMapOptions options = new MapboxMapOptions();
      options.attributionEnabled(false);
      options.logoEnabled(false);
      options.compassEnabled(false);
      options.scrollGesturesEnabled(false);
      options.tiltGesturesEnabled(false);
      options.rotateGesturesEnabled(false);
      options.camera(new CameraPosition.Builder()
        .target(new LatLng(11.302318, 106.025839))
        .zoom(2)
        .build());

      // Create map fragment and pass through map options
      insetMapFragment = SupportMapFragment.newInstance(options);

      // Add fragmentMap fragment to parent container
      transaction.add(R.id.mini_map_fragment_container, insetMapFragment, INSET_FRAGMENT_TAG);
      transaction.commit();
    }

    insetMapFragment.getMapAsync(insetMapReadyCallback);
  }

  private OnMapReadyCallback mainLargeMapReadyCallback = new OnMapReadyCallback() {
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
      InsetMapActivity.this.mainMapboxMap = mapboxMap;
      mapboxMap.setStyle(new Style.Builder().fromUrl(STYLE_URL), new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          mainMapboxMap.addOnCameraMoveListener(mainCameraMoveListener);
        }
      });
    }
  };

  private OnMapReadyCallback insetMapReadyCallback = new OnMapReadyCallback() {
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
      insetMapboxMap = mapboxMap;
      mapboxMap.setStyle(new Style.Builder().fromUrl(STYLE_URL));
    }
  };

  private MapboxMap.OnCameraMoveListener mainCameraMoveListener = new MapboxMap.OnCameraMoveListener() {
    @Override
    public void onCameraMove() {
      CameraPosition mainCameraPosition = mainMapboxMap.getCameraPosition();
      CameraPosition insetCameraPosition = new CameraPosition.Builder(mainCameraPosition)
        .zoom(mainCameraPosition.zoom - ZOOM_DISTANCE_BETWEEN_MAIN_AND_INSET_MAPS).build();

      if (insetMapboxMap != null) {
        insetMapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(insetCameraPosition));
      }
    }
  };

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mainMapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mainMapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mainMapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mainMapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mainMapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mainMapboxMap != null) {
      mainMapboxMap.removeOnCameraMoveListener(mainCameraMoveListener);
    }
    mainMapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mainMapView.onSaveInstanceState(outState);
  }
}