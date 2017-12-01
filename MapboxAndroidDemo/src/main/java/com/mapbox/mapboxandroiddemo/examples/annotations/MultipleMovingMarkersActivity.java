package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

public class MultipleMovingMarkersActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Handler handler;
  private Runnable runnable;

  private static final String CAR_LOCATION_LAYER_ID = "car_marker_layer_id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_multiple_moving_markers);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        MultipleMovingMarkersActivity.this.mapboxMap = mapboxMap;


        SymbolLayer carLocationLayer = new SymbolLayer(CAR_LOCATION_LAYER_ID, "");

        // Use the RefreshCarIconLocationRunnable class and runnable to keep updating the car locations
        runnable = new RefreshCarIconLocationRunnable(handler = new Handler());
        handler.postDelayed(runnable, 100);


      }
    });
  }

  private static class RefreshCarIconLocationRunnable implements Runnable {

    private Handler handler;

    public RefreshCarIconLocationRunnable(Handler handler) {
      this.handler = handler;
    }

    @Override
    public void run() {



      handler.postDelayed(this, 50);
    }
  }

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