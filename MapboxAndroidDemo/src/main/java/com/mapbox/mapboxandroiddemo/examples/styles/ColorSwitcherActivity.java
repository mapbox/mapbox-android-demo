package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Using setPaintProperty to change a layer's fill color.
 */
public class ColorSwitcherActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  FillLayer water;
  FillLayer building;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_color_switcher);

    final SeekBar redSeekBar = (SeekBar) findViewById(R.id.red_seek_bar);
    final SeekBar greenSeekBar = (SeekBar) findViewById(R.id.green_seek_bar);
    final SeekBar blueSeekBar = (SeekBar) findViewById(R.id.blue_seek_bar);

    final Spinner layerPicker = (Spinner) findViewById(R.id.spinner_layer_picker);

    layerPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (layerPicker.getSelectedItem().toString().equals("Building")) {

          if (building != null) {
            redSeekBar.setProgress(Color.red(building.getFillColorAsInt()));
            greenSeekBar.setProgress(Color.green(building.getFillColorAsInt()));
            blueSeekBar.setProgress(Color.blue(building.getFillColorAsInt()));

          }

        } else if (layerPicker.getSelectedItem().toString().equals("Water")) {

          if (water != null) {
            redSeekBar.setProgress(Color.red(water.getFillColorAsInt()));
            greenSeekBar.setProgress(Color.green(water.getFillColorAsInt()));
            blueSeekBar.setProgress(Color.blue(water.getFillColorAsInt()));
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (water != null && layerPicker.getSelectedItem().equals("Water") && fromUser) {
          water.setProperties(
            fillColor(Color.rgb(progress, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
          );
        } else if (building != null && layerPicker.getSelectedItem().equals("Building") && fromUser) {
          building.setProperties(
            fillColor(Color.rgb(progress, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
          );
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (water != null && layerPicker.getSelectedItem().equals("Water") && fromUser) {
          water.setProperties(
            fillColor(Color.rgb(redSeekBar.getProgress(), progress, blueSeekBar.getProgress()))
          );
        } else if (building != null && layerPicker.getSelectedItem().equals("Building") && fromUser) {
          building.setProperties(
            fillColor(Color.rgb(progress, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
          );
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (water != null && layerPicker.getSelectedItem().equals("Water") && fromUser) {
          water.setProperties(
            fillColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), progress))
          );
        } else if (building != null && layerPicker.getSelectedItem().equals("Building") && fromUser) {
          building.setProperties(
            fillColor(Color.rgb(progress, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
          );
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        water = (FillLayer) map.getLayer("water");
        building = (FillLayer) map.getLayer("building");

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
