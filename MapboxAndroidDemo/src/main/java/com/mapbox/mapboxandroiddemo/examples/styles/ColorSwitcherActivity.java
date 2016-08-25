package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class ColorSwitcherActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  FillLayer water;
  FillLayer building;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_style_color_switcher);

    final SeekBar redSeekBar = (SeekBar) findViewById(R.id.red_seek_bar);
    final SeekBar greenSeekBar = (SeekBar) findViewById(R.id.green_seek_bar);
    final SeekBar blueSeekBar = (SeekBar) findViewById(R.id.blue_seek_bar);

    final Spinner layerPicker = (Spinner) findViewById(R.id.spinner_layer_picker);


    layerPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (layerPicker.getSelectedItem().toString().equals("Building")) {

          if (building != null) {
            redSeekBar.setProgress(Color.red(rgbaToColor(building.getFillColor().getValue())));
            greenSeekBar.setProgress(Color.green(rgbaToColor(building.getFillColor().getValue())));
            blueSeekBar.setProgress(Color.blue(rgbaToColor(building.getFillColor().getValue())));

          }

        } else if (layerPicker.getSelectedItem().toString().equals("Water")) {

          if (water != null) {
            redSeekBar.setProgress(Color.red(rgbaToColor(water.getFillColor().getValue())));
            greenSeekBar.setProgress(Color.green(rgbaToColor(water.getFillColor().getValue())));
            blueSeekBar.setProgress(Color.blue(rgbaToColor(water.getFillColor().getValue())));
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });


    //TODO need to reset progress in seek bar when layer changes

    redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (water != null && layerPicker.getSelectedItem().equals("Water") && b) {
          water.setProperties(
              fillColor(Color.rgb(i, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
          );
        } else if (building != null && layerPicker.getSelectedItem().equals("Building") && b) {
          building.setProperties(
              fillColor(Color.rgb(i, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
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
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (water != null && layerPicker.getSelectedItem().equals("Water") && b) {
          water.setProperties(
              fillColor(Color.rgb(redSeekBar.getProgress(), i, blueSeekBar.getProgress()))
          );
        } else if (building != null && layerPicker.getSelectedItem().equals("Building") && b) {
          building.setProperties(
              fillColor(Color.rgb(i, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
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
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (water != null && layerPicker.getSelectedItem().equals("Water") && b) {
          water.setProperties(
              fillColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), i))
          );
        } else if (building != null && layerPicker.getSelectedItem().equals("Building") && b) {
          building.setProperties(
              fillColor(Color.rgb(i, greenSeekBar.getProgress(), blueSeekBar.getProgress()))
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

  public static int normalizeColorComponent(String value) {
    if (value.contains(".")) {
      return (int) (Float.parseFloat(value) * 255);
    } else {
      return Integer.parseInt(value);
    }
  }

  @ColorInt
  public static int rgbaToColor(String value) {
    Pattern c = Pattern.compile("rgba?\\s*\\(\\s*(\\d+\\.?\\d*)\\s*,\\s*(\\d+\\.?\\d*)\\s*,\\s*(\\d+\\.?\\d*)\\s*,?\\s*(\\d+\\.?\\d*)?\\s*\\)");
    Matcher m = c.matcher(value);
    if (m.matches() && m.groupCount() == 3) {
      return Color.rgb(normalizeColorComponent(m.group(1)), normalizeColorComponent(m.group(2)), normalizeColorComponent(m.group(3)));
    } else if (m.matches() && m.groupCount() == 4) {
      return Color.argb(normalizeColorComponent(m.group(4)), normalizeColorComponent(m.group(1)), normalizeColorComponent(m.group(2)), normalizeColorComponent(m.group(3)));
    } else {
      throw new RuntimeException("Not a valid rgb/rgba value");
    }
  }
}
