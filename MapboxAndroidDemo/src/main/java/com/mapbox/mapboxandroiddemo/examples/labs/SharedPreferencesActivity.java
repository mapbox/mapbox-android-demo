package com.mapbox.mapboxandroiddemo.examples.labs;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;


/**
 * Use shared preferences to save and retrieve data, so that the data can be displayed after closing the app.
 */
public class SharedPreferencesActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private static final String SAVED_LAT_KEY = "SAVED_LAT_KEY";
  private static final String SAVED_LONG_KEY = "SAVED_LONG_KEY";
  private static final String CLICK_LOCATION_SOURCE_ID = "CLICK_LOCATION_SOURCE_ID";
  private static final String CLICK_LOCATION_ICON_ID = "CLICK_LOCATION_ICON_ID";
  private static final String CLICK_LOCATION_LAYER_ID = "CLICK_LOCATION_LAYER_ID";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private SharedPreferences sharedPreferences;
  private TextView longTextView;
  private TextView latTextView;
  private double savedLat;
  private double savedLong;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_shared_preferences);

    longTextView = findViewById(R.id.shared_pref_saved_long_textview);
    latTextView = findViewById(R.id.shared_pref_saved_lat_textview);
    longTextView.setText(String.format(getString(R.string.saved_long_textview), String.valueOf(0)));
    latTextView.setText(String.format(getString(R.string.saved_lat_textview), String.valueOf(0)));

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    SharedPreferencesActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(

      // Set the map to Mapbox's daytime traffic style
      new Style.Builder().fromUri(Style.TRAFFIC_DAY)

      // Add the SymbolLayer icon image to the map style
      .withImage(CLICK_LOCATION_ICON_ID, BitmapFactory.decodeResource(
        SharedPreferencesActivity.this.getResources(), R.drawable.red_marker))

      // Adding a GeoJson source for the SymbolLayer icons.
      .withSource(new GeoJsonSource(CLICK_LOCATION_SOURCE_ID))

      // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
      // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
      // the coordinate point. This is offset is not always needed and is dependent on the image
      // that you use for the SymbolLayer icon.
      .withLayer(new SymbolLayer(CLICK_LOCATION_LAYER_ID, CLICK_LOCATION_SOURCE_ID)
        .withProperties(PropertyFactory.iconImage(CLICK_LOCATION_ICON_ID),
          iconAllowOverlap(true),
          iconOffset(new Float[] {0f, -9f}))
      ), new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            mapboxMap.addOnMapClickListener(SharedPreferencesActivity.this);

            // Get the coordinates from shared preferences
            savedLong = getCoordinateFromSharedPref(SAVED_LONG_KEY);
            savedLat = getCoordinateFromSharedPref(SAVED_LAT_KEY);

            // Coordinates haven't been saved if both == 0
            if (savedLong == 0 && savedLat == 0) {
              Toast.makeText(SharedPreferencesActivity.this,
                getString(R.string.tap_on_map_save_to_shared_pref), Toast.LENGTH_SHORT).show();

              longTextView.setText(String.format(getString(R.string.saved_long_textview),
                getString(R.string.not_saved_yet)));
              latTextView.setText(String.format(getString(R.string.saved_lat_textview),
                getString(R.string.not_saved_yet)));

            } else {

              // Move the camera to the previously-saved coordinates
              mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                  .target(new LatLng(savedLat, savedLong))
                  .zoom(4)
                  .build()), 1200);

              Toast.makeText(SharedPreferencesActivity.this,
                getString(R.string.shared_pref_marker_placement), Toast.LENGTH_SHORT).show();

              // Move the marker to the previously-saved coordinates
              moveMarkerToLngLat(savedLong, savedLat);

              longTextView.setText(String.format(
                getString(R.string.saved_long_textview), String.valueOf(savedLong)));

              latTextView.setText(String.format(
                getString(R.string.saved_lat_textview), String.valueOf(savedLat)));
            }
          }
      });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng mapClickPoint) {
    double clickLatitude = mapClickPoint.getLatitude();
    double clickLongitude = mapClickPoint.getLongitude();

    longTextView.setText(String.format(
        getString(R.string.saved_long_textview), String.valueOf(clickLongitude)));
    latTextView.setText(String.format(
        getString(R.string.saved_lat_textview), String.valueOf(clickLatitude)));

    // Save the map click point coordinates to shared preferences
    if (sharedPreferences != null) {
      putCoordinateToSharedPref(SAVED_LAT_KEY, clickLatitude);
      putCoordinateToSharedPref(SAVED_LONG_KEY, clickLongitude);
    }

    // Move the marker to the newly-saved coordinates
    moveMarkerToLngLat(clickLongitude, clickLatitude);
    return true;
  }

  /**
   * Move the SymbolLayer icon to a new location
   *
   * @param newLong the new longitude
   * @param newLat  the new latitude
   */
  private void moveMarkerToLngLat(double newLong, double newLat) {
    // Move and display the click center layer's red marker icon to
    // wherever the map was clicked on
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_LOCATION_SOURCE_ID);
        if (clickLocationSource != null) {
          clickLocationSource.setGeoJson(Point.fromLngLat(newLong, newLat));
        }
      }
    });
  }

  /**
   * Save a specific number to shared preferences
   *
   * @param key   the number's key
   * @param value the actual number
   */
  private void putCoordinateToSharedPref(final String key, final double value) {
    sharedPreferences.edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
  }

  /**
   * Retrieve a specific number from shared preferences
   *
   * @param key the key to use for retrieval
   * @return the saved number
   */
  double getCoordinateFromSharedPref(final String key) {
    return Double.longBitsToDouble(sharedPreferences.getLong(key, 0));
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
