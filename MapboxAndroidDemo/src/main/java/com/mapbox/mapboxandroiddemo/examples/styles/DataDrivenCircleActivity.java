package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.layers.Function.stop;
import static com.mapbox.mapboxsdk.style.layers.Function.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class DataDrivenCircleActivity extends AppCompatActivity {

  MapView mapView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_style_data_driven_circle);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        VectorSource vectorSource = new VectorSource("population", "mapbox://examples.8fgz4egr");
        mapboxMap.addSource(vectorSource);

        CircleLayer circleLayer = new CircleLayer("population", "population");
        circleLayer.setSourceLayer("sf2010");
        circleLayer.setProperties(

          // make circles larger as the user zooms from z12 to z22
          circleRadius(zoom(0.75f,
            stop(12, circleRadius(2f)),
            stop(22, circleRadius(8f))
          ))

          // color circles by ethnicity, using data-driven styles
//          circleColor(
//            stop()
//          )

        );

        mapboxMap.addLayer(circleLayer);
      }
    });
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
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
