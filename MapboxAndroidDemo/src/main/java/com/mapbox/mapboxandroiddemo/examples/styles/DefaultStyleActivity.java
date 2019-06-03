package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

/**
 * Use a variety of professionally designed styles with the Mapbox Android SDK.
 */
public class DefaultStyleActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_default);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        DefaultStyleActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT);
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_map_style, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.menu_streets:
        mapboxMap.setStyle(Style.MAPBOX_STREETS);
        return true;
      case R.id.menu_dark:
        mapboxMap.setStyle(Style.DARK);
        return true;
      case R.id.menu_light:
        mapboxMap.setStyle(Style.LIGHT);
        return true;
      case R.id.menu_outdoors:
        mapboxMap.setStyle(Style.OUTDOORS);
        return true;
      case R.id.menu_satellite:
        mapboxMap.setStyle(Style.SATELLITE);
        return true;
      case R.id.menu_satellite_streets:
        mapboxMap.setStyle(Style.SATELLITE_STREETS);
        return true;
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
