package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;

/**
 * Use {@link com.mapbox.mapboxsdk.style.layers.PropertyFactory#iconIgnorePlacement(Boolean)},
 * {@link com.mapbox.mapboxsdk.style.layers.PropertyFactory#iconAllowOverlap(Boolean)},
 * {@link com.mapbox.mapboxsdk.style.layers.PropertyFactory#textIgnorePlacement(Boolean)},
 * and {@link com.mapbox.mapboxsdk.style.layers.PropertyFactory#iconAllowOverlap(Boolean)},
 * to handle icon and text collisions.
 */
public class SymbolCollisionDetectionActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String ICON_SOURCE_ID = "ICON_SOURCE_ID";
  private static final String ICON_ID = "ICON_ID";
  private static final String ICON_LAYER_ID = "ICON_LAYER_ID";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private List<Feature> symbolLayerIconFeatureList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_symbol_collision);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    initIconCoordinates();

    mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS)

      // Add the SymbolLayer icon image to the map style
      .withImage(ICON_ID, BitmapFactory.decodeResource(
        SymbolCollisionDetectionActivity.this.getResources(), R.drawable.red_marker))

      // Adding a GeoJson source for the SymbolLayer icons.
      .withSource(new GeoJsonSource(ICON_SOURCE_ID,
        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))

      // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
      // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
      // the coordinate point. This is offset is not always needed and is dependent on the image
      // that you use for the SymbolLayer icon.
      .withLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID)
        .withProperties(PropertyFactory.iconImage(ICON_ID),
          iconOffset(new Float[] {0f, -9f}))
      ), new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {

          SymbolCollisionDetectionActivity.this.mapboxMap = mapboxMap;
          setUpCheckListener(R.id.toggle_text_ignore_placement_switch, true, false);
          setUpCheckListener(R.id.toggle_text_ignore_overlap_switch, false, false);
          setUpCheckListener(R.id.toggle_icon_ignore_placement_switch, false, true);
          setUpCheckListener(R.id.toggle_icon_ignore_overlap_switch, false, false);
        }
      });
  }

  /**
   * Add a {@link android.widget.CompoundButton.OnCheckedChangeListener} to a switch button and then
   * use the Maps SDK's runtime-styling to adjust overlap and placement logic.
   *
   * @param checkboxId the id of the switch button to add a listener to
   * @param adjustTextIgnorePlacement whether or not text placement should be ignored
   * @param adjustIconIgnorePlacement whether or not icon placement should be ignored
   */
  private void setUpCheckListener(int checkboxId, boolean adjustTextIgnorePlacement,
                                  boolean adjustIconIgnorePlacement) {
    Switch switchUi = findViewById(checkboxId);
    switchUi.setText(getString(R.string.collision_disabled));
    switchUi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switchUi.setText(checked ? getString(R.string.collision_enabled) : getString(R.string.collision_disabled));

        mapboxMap.getStyle(new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            Layer singleLayer = style.getLayer(adjustIconIgnorePlacement ? ICON_LAYER_ID : "country-label");
            if (singleLayer != null) {
              singleLayer.setProperties(
                adjustTextIgnorePlacement ? textIgnorePlacement(checked) : textAllowOverlap(checked),
                adjustIconIgnorePlacement ? iconIgnorePlacement(checked) : iconAllowOverlap(checked)
              );
            }
          }
        });
      }
    });
  }

  /**
   * Initialize a list of Features to use to show SymbolLayer icons.
   */
  private void initIconCoordinates() {
    symbolLayerIconFeatureList = new ArrayList<>();
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(10.338784, 49.481615)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(15.081775, 49.957444)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(11.810747, 50.53269)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(16.308411, 51.35705)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(19.661215, 49.161803)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(16.799065, 46.864746)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(13.364485, 52.764672)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(8.457943, 51.203595)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(12.873831, 51.459068)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(14.836448, 52.814126)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(9.193924, 52.516561)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(12.219625, 47.143578)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(14.182242, 48.893705)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(16.717289, 50.324313)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(22.686916, 45.905823)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(7.967289, 49.904804)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(6.98598, 47.916571)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(18.925233, 44.231107)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(18.843458, 49.957444)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-17.999544, 62.216028)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(20.151869, 46.415586)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(9.193924, 51.864868)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(16.144859, 48.732152)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(5.105139, 50.324313)));
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
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
  public void onStop() {
    super.onStop();
    mapView.onStop();
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
