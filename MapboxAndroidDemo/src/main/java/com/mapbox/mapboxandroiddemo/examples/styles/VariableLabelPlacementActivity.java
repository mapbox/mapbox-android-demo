package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_RIGHT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_JUSTIFY_AUTO;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textJustify;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textRadialOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textVariableAnchor;


/**
 * To increase the chance of high-priority labels staying visible, provide the map
 * renderer a list of preferred text anchor positions via
 * {@link com.mapbox.mapboxsdk.style.layers.PropertyFactory#textVariableAnchor(String[])}.
 */
public class VariableLabelPlacementActivity extends AppCompatActivity {

  private static final String GEOJSON_SRC_ID = "poi_source_id";
  private static final String POI_LABELS_LAYER_ID = "poi_labels_layer_id";
  private MapView mapView;
  private GeoJsonSource geoJsonSource;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_variable_text_placement);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        try {
          geoJsonSource = new GeoJsonSource(GEOJSON_SRC_ID, new URI("asset://poi_places.geojson"));

          mapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT)
              .withSource(geoJsonSource)
              // Adds a SymbolLayer to display POI labels
              .withLayer(new SymbolLayer(POI_LABELS_LAYER_ID, GEOJSON_SRC_ID)
                .withProperties(
                  textField(get("description")),
                  textSize(17f),
                  textColor(Color.RED),
                  textVariableAnchor(
                    new String[]{TEXT_ANCHOR_TOP, TEXT_ANCHOR_BOTTOM, TEXT_ANCHOR_LEFT, TEXT_ANCHOR_RIGHT}),
                  textJustify(TEXT_JUSTIFY_AUTO),
                  textRadialOffset(0.5f))),
            new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull final Style style) {
                Toast.makeText(VariableLabelPlacementActivity.this,
                  getString(R.string.zoom_map_in_and_out_variable_label_instruction),
                  Toast.LENGTH_SHORT).show();
              }
            });
        } catch (URISyntaxException exception) {
          Timber.d(exception);
        }
      }
    });
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