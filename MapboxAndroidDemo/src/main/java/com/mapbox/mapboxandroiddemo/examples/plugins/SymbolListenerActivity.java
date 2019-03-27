package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Change symbol icon by pressing on icon
 */
public class SymbolListenerActivity extends AppCompatActivity implements
  OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private static final String LOG = "SymbolListenerActivity";
  private static final String MAKI_ICON_CAFE = "cafe-15";
  private static final String MAKI_ICON_HARBOR = "harbor-15";
  private static final String MAKI_ICON_AIRPORT = "airport-15";
  private SymbolManager symbolManager;
  private Symbol symbol;
  private final List<ValueAnimator> animators = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_annotation_plugin_symbol_activity);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);

  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Set up a SymbolManager instance
        symbolManager = new SymbolManager(mapView, mapboxMap, style);

        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);

        // Add symbol at specified lat/lon
        symbol = symbolManager.create(new SymbolOptions()
          .withLatLng(new LatLng(60.169091, 24.939876))
          .withIconImage(MAKI_ICON_HARBOR)
          .withIconSize(2.0f)
          .setDraggable(true));

        // Add click listener and change the symbol to a cafe icon on click
        symbolManager.addClickListener(new OnSymbolClickListener() {
          @Override
          public void onAnnotationClick(Symbol symbol) {
            Toast.makeText(SymbolListenerActivity.this,
              String.format("Symbol clicked"),
              Toast.LENGTH_SHORT).show();
            symbol.setIconImage(MAKI_ICON_CAFE);
            symbolManager.update(symbol);
          }
        });

        // Add long click listener and change the symbol to an airport icon on long click
        symbolManager.addLongClickListener((new OnSymbolLongClickListener() {
          @Override
          public void onAnnotationLongClick(Symbol symbol) {
            Toast.makeText(SymbolListenerActivity.this,
              String.format("Symbol long clicked"),
              Toast.LENGTH_SHORT).show();
            symbol.setIconImage(MAKI_ICON_AIRPORT);
            symbolManager.update(symbol);
          }
        }));

        symbolManager.addDragListener(new OnSymbolDragListener() {
          @Override
          public void onAnnotationDragStarted(Symbol annotation) {
          }

          @Override
          public void onAnnotationDrag(Symbol symbol) {
          }

          @Override
          public void onAnnotationDragFinished(Symbol annotation) {
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