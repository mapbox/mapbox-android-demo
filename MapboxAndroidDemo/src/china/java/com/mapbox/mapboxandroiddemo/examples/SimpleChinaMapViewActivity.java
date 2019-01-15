package com.mapbox.mapboxandroiddemo.examples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.china.constants.ChinaStyle;
import com.mapbox.mapboxsdk.plugins.china.maps.ChinaMapView;

/**
 * The most basic example of adding a government approved and performant China map to an activity.
 */
public class SimpleChinaMapViewActivity extends AppCompatActivity {
  private ChinaMapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the MapView. You will need a special Mapbox
    // China access token if you want to view any of our China examples and use China map styles.
    // Please fill out the form at https://www.mapbox.cn/contact to start the process of
    // receiving a special China access token. Thank you!
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_basic_simple_china_mapview);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUrl(ChinaStyle.MAPBOX_STREETS_CHINESE), new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Map is set up and the style has loaded. Now you can add data or make other map adjustments


          }
        });
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_china_map_style, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.menu_streets:
        if (mapboxMap != null) {
          mapboxMap.setStyleUrl(ChinaStyle.MAPBOX_STREETS_CHINESE);
        }
        return true;
      case R.id.menu_dark:
        if (mapboxMap != null) {
          mapboxMap.setStyleUrl(ChinaStyle.MAPBOX_DARK_CHINESE);
        }
        return true;
      case R.id.menu_light:
        if (mapboxMap != null) {
          mapboxMap.setStyleUrl(ChinaStyle.MAPBOX_LIGHT_CHINESE);
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
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