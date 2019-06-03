package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;

/**
 * Use the Scale Bar Plugin to provide a visual indication of how far various map features are from
 * one another at a certain zoom level. The scalebar can be customized with options such as the
 * text color/size, referesh interval, margins, and border width.
 */
public class ScalebarPluginActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private ScaleBarPlugin scaleBarPlugin;
  private ScaleBarOptions[] listOfScalebarStyleVariations;
  private String[] listOfStyles = new String[] {Style.LIGHT, Style.DARK, Style.SATELLITE_STREETS, Style.OUTDOORS};
  private int index = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));
    setContentView(R.layout.activity_scalebar_plugin);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    mapboxMap.setStyle(listOfStyles[index], new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull final Style style) {

        initStyling();

        // Create a new ScaleBarPlugin object
        scaleBarPlugin = new ScaleBarPlugin(mapView, mapboxMap);

        scaleBarPlugin.create(listOfScalebarStyleVariations[index]);

        findViewById(R.id.switch_scalebar_style_fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

            if (index == listOfScalebarStyleVariations.length - 1) {
              index = 0;
            }

            mapboxMap.setStyle(listOfStyles[index], new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                scaleBarPlugin.create(listOfScalebarStyleVariations[index]);

              }
            });

            index++;

          }
        });

        Toast.makeText(ScalebarPluginActivity.this, getString(R.string.zoom_map_fab_instruction),
          Toast.LENGTH_LONG).show();
      }
    });
  }

  private void initStyling() {
    listOfScalebarStyleVariations = new ScaleBarOptions[] {

      // Using the plugin's default styling to start
      new ScaleBarOptions(this),

      // Random styling option #2
      new ScaleBarOptions(this)
        .setTextColor(R.color.mapboxRed)
        .setTextSize(40f)
        .setBarHeight(15f)
        .setBorderWidth(5f)
        .setMetricUnit(true)
        .setRefreshInterval(15)
        .setMarginTop(30f)
        .setMarginLeft(16f)
        .setTextBarMargin(15f),

      // Random styling option #3
      new ScaleBarOptions(this)
        .setTextColor(R.color.mapbox_blue)
        .setTextSize(60f)
        .setBarHeight(15f)
        .setBorderWidth(5f)
        .setMetricUnit(true)
        .setRefreshInterval(15)
        .setMarginTop(30f)
        .setMarginLeft(30f)
        .setTextBarMargin(25f),

      // Random styling option #4
      new ScaleBarOptions(this)
        .setTextColor(R.color.mapboxYellow)
        .setTextSize(30f)
        .setBarHeight(15f)
        .setBorderWidth(5f)
        .setMetricUnit(false)
        .setRefreshInterval(15)
        .setMarginTop(30f)
        .setMarginLeft(30f)
        .setTextBarMargin(25f),
    };
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
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
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }
}
