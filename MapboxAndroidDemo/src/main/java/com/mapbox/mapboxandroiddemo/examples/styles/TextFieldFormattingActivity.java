package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
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

  private final String[] textFonts = new String[] {"Roboto Black", "Arial Unicode MS Regular"};
  private final Float[] textSizes = new Float[] {25f, 13f};
  private final String[] textColors = new String[] {"#00FF08", "#ffd43a"};
  private MapView mapView;
  private boolean fontChanged = false;
  private boolean sizeChanged = false;
  private boolean colorChanged = false;

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
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull final Style style) {

            Toast.makeText(TextFieldFormattingActivity.this, getString(R.string.instruction_toast),
              Toast.LENGTH_SHORT).show();

            // Set click listeners for the text adjustment buttons
            FloatingActionButton textFontFab = findViewById(R.id.fab_toggle_font);
            textFontFab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                adjustText(style, textFont(fontChanged ? new String[] {textFonts[1]} : new String[] {textFonts[0]}));
                fontChanged = !fontChanged;
              }
            });

            FloatingActionButton textSizeFab = findViewById(R.id.fab_toggle_text_size);
            textSizeFab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                adjustText(style, textSize(sizeChanged ? textSizes[1] : textSizes[0]));
                sizeChanged = !sizeChanged;
              }
            });

            FloatingActionButton textColorFab = findViewById(R.id.fab_toggle_text_color);
            textColorFab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                adjustText(style, textColor(colorChanged ? textColors[1] : textColors[0]));
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
   * @param loadedMapStyle a fully loaded map {@link Style} object
   * @param propertyValue the {@link com.mapbox.mapboxsdk.style.layers.PropertyFactory} to adjust for
   *                      the specific layer's text field.
   */
  private void adjustText(@NonNull Style loadedMapStyle, @NonNull PropertyValue propertyValue) {
    for (Layer singleMapLayer : loadedMapStyle.getLayers()) {
      if (singleMapLayer.getId().contains("water-") && !singleMapLayer.getId().equals("water-shadow")) {
        singleMapLayer.setProperties(propertyValue);
      }
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
