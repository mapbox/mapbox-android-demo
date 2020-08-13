package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

/**
 * Change symbol icon by pressing on icon
 */
public class SymbolListenerActivity extends AppCompatActivity implements
  OnMapReadyCallback {

  private MapView mapView;
  private static final String MAKI_ICON_CAFE = "cafe-15";
  private static final String MAKI_ICON_HARBOR = "harbor-15";
  private static final String MAKI_ICON_AIRPORT = "airport-15";
  private SymbolManager symbolManager;
  private Symbol symbol;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_annotation_plugin_symbol_listener);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
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
          .withDraggable(true));

        // Add click listener and change the symbol to a cafe icon on click
        symbolManager.addClickListener(new OnSymbolClickListener() {
          @Override
          public boolean onAnnotationClick(Symbol symbol) {
            Toast.makeText(SymbolListenerActivity.this,
              getString(R.string.clicked_symbol_toast), Toast.LENGTH_SHORT).show();
            symbol.setIconImage(MAKI_ICON_CAFE);
            symbolManager.update(symbol);
            return true;
          }
        });

        // Add long click listener and change the symbol to an airport icon on long click
        symbolManager.addLongClickListener((new OnSymbolLongClickListener() {
          @Override
          public boolean onAnnotationLongClick(Symbol symbol) {
            Toast.makeText(SymbolListenerActivity.this,
              getString(R.string.long_clicked_symbol_toast), Toast.LENGTH_SHORT).show();
            symbol.setIconImage(MAKI_ICON_AIRPORT);
            symbolManager.update(symbol);
            return true;
          }
        }));

        symbolManager.addDragListener(new OnSymbolDragListener() {
          @Override
          // Left empty on purpose
          public void onAnnotationDragStarted(Symbol annotation) {
          }

          @Override
          // Left empty on purpose
          public void onAnnotationDrag(Symbol symbol) {
          }

          @Override
          // Left empty on purpose
          public void onAnnotationDragFinished(Symbol annotation) {
          }
        });
        Toast.makeText(SymbolListenerActivity.this,
          getString(R.string.symbol_listener_instruction_toast), Toast.LENGTH_SHORT).show();
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