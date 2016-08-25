package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.rasterOpacity;

public class AdjustLayerOpacityActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private Layer chicago;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_style_adjust_layer_opacity);

    final SeekBar opacitySeekBar = (SeekBar) findViewById(R.id.seek_bar_layer_opacity);
    final TextView percentTextView = (TextView) findViewById(R.id.textview_opacity_value);

    opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (chicago != null) {
          chicago.setProperties(
              rasterOpacity((float) i / 100)
          );
        }
        String s = i + "%";
        percentTextView.setText(s);
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

        RasterSource chicagoSource = new RasterSource("chicago-source", "mapbox://mapbox.u8yyzaor");
        map.addSource(chicagoSource);

        RasterLayer chicagoLayer = new RasterLayer("chicago", "chicago-source");
        map.addLayer(chicagoLayer);

        chicago = map.getLayer("chicago");

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
}