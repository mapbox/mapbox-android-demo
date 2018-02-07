package com.mapbox.mapboxandroiddemo.labs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

public class DownloadableFontActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private String TAG = "DownloadableFontActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_downloadable_font);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    DownloadableFontActivity.this.mapboxMap = mapboxMap;
    initFontFabs();
  }

  private void initFontFabs() {

    findViewById(R.id.fab_toggle_font_one).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "onClick: Clicked on one");
        adjustLayers(getResources().getStringArray(R.array.fonts_array)[0]);
      }
    });

    findViewById(R.id.fab_toggle_font_two).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "onClick: Clicked on two");

        adjustLayers(getResources().getStringArray(R.array.fonts_array)[1]);
      }
    });

    findViewById(R.id.fab_toggle_font_three).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        Log.d(TAG, "onClick: Clicked on three");

        adjustLayers(getResources().getStringArray(R.array.fonts_array)[2]);
      }
    });

    findViewById(R.id.fab_toggle_font_four).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "onClick: Clicked on four");

        adjustLayers(getResources().getStringArray(R.array.fonts_array)[3]);
      }
    });
  }

  private void adjustLayers(String fontName) {
    SymbolLayer countryLabelLayer = (SymbolLayer) mapboxMap.getLayer("country-label-lg");
    countryLabelLayer.setProperties(PropertyFactory.textFont(new String[] {fontName}));

    SymbolLayer waterLabelLayer = (SymbolLayer) mapboxMap.getLayer("marine-label-lg-pt");
    waterLabelLayer.setProperties(PropertyFactory.textFont(new String[] {fontName}));
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