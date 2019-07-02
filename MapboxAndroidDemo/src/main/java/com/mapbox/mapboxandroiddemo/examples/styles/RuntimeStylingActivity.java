package com.mapbox.mapboxandroiddemo.examples.styles;

import com.mapbox.mapboxandroiddemo.R;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;

import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import android.graphics.Color;
import android.view.View;

public class RuntimeStylingActivity extends AppCompatActivity  {

  private MapView mapView;
  private FloatingActionButton changeMapPropertiesFab;
  private Layer waterLayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Mapbox.getInstance(this, getString(R.string.access_token));
    setContentView(R.layout.activity_styles_runtime_styling);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    changeMapPropertiesFab = findViewById(R.id.floatingActionButton);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull final Style style) {
            waterLayer = style.getLayer("water");
            changeMapPropertiesFab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (waterLayer != null) {
                  waterLayer.setProperties(PropertyFactory.fillColor(Color.parseColor("#023689"))
                  );
                }
                for (Layer singleMapLayer : style.getLayers()) {
                  if (singleMapLayer.getId().contains("water-") && !singleMapLayer.getId().equals("water-shadow")) {
                    singleMapLayer.setProperties(
                            PropertyFactory.textHaloBlur(10f),
                            PropertyFactory.textSize(25f),
                            PropertyFactory.textColor(Color.parseColor("#00FF08")),
                            PropertyFactory.textOpacity(1f)
                    );
                  }
                }
              }
            });
          }
        });
      }
    });
  }

  // Add the mapView's own lifecycle methods to the activity's lifecycle methods
  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
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
  public void onStop() {
    super.onStop();
    mapView.onStop();
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