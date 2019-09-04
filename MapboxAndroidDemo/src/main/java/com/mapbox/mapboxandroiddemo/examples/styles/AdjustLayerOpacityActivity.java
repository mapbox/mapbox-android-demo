package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.rasterOpacity;

/**
 * Use a seekbar to adjust the opacity of a raster layer on top of a map.
 */
public class AdjustLayerOpacityActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private Layer chicago;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_adjust_layer_opacity);

    final SeekBar opacitySeekBar = findViewById(R.id.seek_bar_layer_opacity);
    final TextView percentTextView = findViewById(R.id.textview_opacity_value);

    opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (chicago != null) {
          chicago.setProperties(
              rasterOpacity((float) progress / 100)
          );
        }
        String progressPercentage = progress + "%";
        percentTextView.setText(progressPercentage);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            style.addSource(new RasterSource("chicago-source", "mapbox://mapbox.u8yyzaor"));
            style.addLayer(new RasterLayer("chicago", "chicago-source"));
            map.getStyle(new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {
                chicago = style.getLayer("chicago");
              }
            });
          }
        });
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
