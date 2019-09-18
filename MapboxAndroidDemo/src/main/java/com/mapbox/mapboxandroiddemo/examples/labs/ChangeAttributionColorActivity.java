package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Adjust the Maps SDK's attribution ImageView color so that it matches the map style or app UI
 */
public class ChangeAttributionColorActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String CUSTOM_GREEN_MONOCHROME_STYLE_URI =
      "mapbox://styles/appsatmapboxcom/ck0cyq1lt0bbo1cmwkpf2w7g9";
  private MapView mapView;
  private int index = 0;
  private int[] attributionColors;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    attributionColors = new int[] {
      Color.RED,
      Color.BLUE,
      Color.YELLOW,
      ContextCompat.getColor(this, R.color.colorAccent), // a runtime app UI color
      ContextCompat.getColor(this, R.color.colorPrimaryDark), // a runtime app UI color
      Color.parseColor("#1e7019"), // dark green
    };

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_lab_change_attribution_info_button_color);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Change the color when the floating action button is clicked
        findViewById(R.id.switch_attribution_color_fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (index == attributionColors.length) {
              mapboxMap.setStyle(Style.LIGHT);
              index = 0;
            }

            // Adjust map to the Maps SDK's Dark style
            if (index == 2) {
              mapboxMap.setStyle(Style.DARK);
            }

            // Adjust map to a custom monochrome dark green style to match the
            // "i" attribution's dark green color
            if (index == 5) {
              mapboxMap.setStyle(new Style.Builder()
                  .fromUri(CUSTOM_GREEN_MONOCHROME_STYLE_URI));
            }

            // Change the color of the "i" attribution button
            mapboxMap.getUiSettings().setAttributionTintColor(attributionColors[index]);

            index++;
          }
        });
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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
}
