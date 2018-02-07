package com.mapbox.mapboxandroiddemo.labs;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

public class DownloadableFontActivity extends AppCompatActivity implements OnMapReadyCallback,
  AdapterView.OnItemSelectedListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private ArrayAdapter<CharSequence> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_downloadable_font);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    Spinner spinner = findViewById(R.id.fonts_spinner);

    // Create an ArrayAdapter using the string array and a default spinner layout
    adapter = ArrayAdapter.createFromResource(this,
      R.array.fonts_array, android.R.layout.simple_spinner_item);

    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    if (spinner != null) {
      spinner.setAdapter(adapter);
    }

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    DownloadableFontActivity.this.mapboxMap = mapboxMap;
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    SymbolLayer countryLabelLayer = (SymbolLayer) mapboxMap.getLayer("country-label-lg");
    countryLabelLayer.withProperties(PropertyFactory.textFont(new String[] {adapter.getItem(position).toString()}));

    SymbolLayer waterLabelLayer = (SymbolLayer) mapboxMap.getLayer("marine-label-lg-pt");
    waterLabelLayer.withProperties(PropertyFactory.textFont(new String[] {adapter.getItem(position).toString()}));
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Left empty on purpose
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