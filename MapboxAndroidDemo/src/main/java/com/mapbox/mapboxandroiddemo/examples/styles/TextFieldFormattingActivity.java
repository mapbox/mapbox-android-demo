package com.mapbox.mapboxandroiddemo.examples.styles;

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
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Use runtime styling to adjust the font, size, and color of SymbolLayer text fields.
 */
public class TextFieldFormattingActivity extends AppCompatActivity {

  private static final String[] textFonts = new String[] {"Roboto Black", "Arial Unicode MS Regular"};
  private static final Float[] textSizes = new Float[] {25f, 13f};
  private static final String[] textColors = new String[] {"#00FF08", "#ffd43a"};
  private static final String WATER_RELATED_LAYER = "water-";
  private static final String WATER_SHADOW_LAYER_ID = "water-shadow";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean fontChanged;
  private boolean sizeChanged;
  private boolean colorChanged;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_text_field_formatting);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull final Style style) {

            TextFieldFormattingActivity.this.mapboxMap = mapboxMap;
            Toast.makeText(TextFieldFormattingActivity.this, getString(R.string.instruction_toast),
              Toast.LENGTH_SHORT).show();

            // Set click listeners for the text adjustment buttons
            findViewById(R.id.fab_toggle_font).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                adjustText(textFont(fontChanged ? new String[] {textFonts[1]} : new String[] {textFonts[0]}));
                fontChanged = !fontChanged;
              }
            });

            findViewById(R.id.fab_toggle_text_size).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                adjustText(textSize(sizeChanged ? textSizes[1] : textSizes[0]));
                sizeChanged = !sizeChanged;
              }
            });

            findViewById(R.id.fab_toggle_text_color).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                adjustText(textColor(colorChanged ? textColors[1] : textColors[0]));
                colorChanged = !colorChanged;
              }
            });
          }
        });
      }
    });
  }

  /**
   * Retrieve a specific layer and adjust a property of its text field.
   *
   * @param propertyValue the {@link com.mapbox.mapboxsdk.style.layers.PropertyFactory} to adjust for
   *                      the specific layer's text field.
   */
  private void adjustText(@NonNull PropertyValue propertyValue) {
    if (mapboxMap.getStyle() != null) {
      for (Layer singleMapLayer : mapboxMap.getStyle().getLayers()) {
        if (singleMapLayer.getId().contains(WATER_RELATED_LAYER)
          && !singleMapLayer.getId().equals(WATER_SHADOW_LAYER_ID)) {
          singleMapLayer.setProperties(propertyValue);
        }
      }
    } else {
      Toast.makeText(this, R.string.map_style_not_loaded_yet, Toast.LENGTH_SHORT).show();
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
