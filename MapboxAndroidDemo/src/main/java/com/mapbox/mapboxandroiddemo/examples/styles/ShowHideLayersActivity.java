package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.layers.Property.*;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class ShowHideLayersActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private FloatingActionButton floatingActionButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_style_show_hide_layers);

    floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_layer_toggle);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        VectorSource museums = new VectorSource("museums_source", "mapbox://mapbox.2opop9hr");
        map.addSource(museums);

        CircleLayer museumsLayer = new CircleLayer("museums", "museums_source");
        museumsLayer.setSourceLayer("museum-cusco");
        museumsLayer.setProperties(
            visibility(VISIBLE),
            circleRadius(8f),
            circleColor(Color.argb(1, 55, 148, 179))
        );

        map.addLayer(museumsLayer);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

            toggleLayer();

          }
        });
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
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

  private void toggleLayer() {
    Layer layer = map.getLayer("museums");
    if (layer != null) {
      if (VISIBLE.equals(layer.getVisibility().getValue())) {
        layer.setProperties(visibility(NONE));
      } else {
        layer.setProperties(visibility(VISIBLE));
      }
    }
  }
}