package com.mapbox.mapboxandroiddemo.examples.basics;

import android.os.Bundle;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.TimingLogger;


/**
 * The most basic example of adding a map to an activity.
 */
public class SimpleMapViewActivity extends AppCompatActivity {

  private MapView mapView;
  private TimingLogger timings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    timings = new TimingLogger("MyTag", "methodA");
    timings.addSplit("onStart");

    String style = Style.MAPBOX_STREETS;

    SimpleMapViewActivity.this.timings = timings;

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_basic_simple_mapview);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.addOnDidFinishLoadingMapListener(new MapView.OnDidFinishLoadingMapListener() {
      @Override
      public void onDidFinishLoadingMap() {
        timings.addSplit("OnDidFinishLoadingMapListener");
        timings.dumpToLog();
      }
    });
    mapView.getMapAsync(new OnMapReadyCallback() {

      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        timings.addSplit("onMapReady");

        mapboxMap.setStyle(style, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            timings.addSplit("onStyleLoaded");
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
