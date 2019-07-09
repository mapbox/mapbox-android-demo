package com.mapbox.mapboxandroiddemo.examples.dds;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.step;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Use the SymbolLayer's setMinZoom and setMaxZoom methods to create the effect of SymbolLayer icons switching
 * based on the map camera's zoom level.
 */
public class SymbolSwitchOnZoomActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final float ZOOM_LEVEL_FOR_SWITCH = 12;
  private static final String BLUE_PERSON_ICON_ID = "blue-car-icon-marker-icon-id";
  private static final String BLUE_PIN_ICON_ID = "blue-marker-icon-marker-icon-id";
  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_zoom_based_icon_switch);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {

    mapboxMap.setStyle(new Style.Builder().fromUri(Style.OUTDOORS)

            // Add images to the map so that the SymbolLayers can reference the images.
            .withImage(BLUE_PERSON_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.ic_person)))
            .withImage(BLUE_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.blue_marker)))

            // Add random data to the GeoJsonSource and then add the GeoJsonSource to the map
            .withSource(new GeoJsonSource("source-id",
                FeatureCollection.fromFeatures(new Feature[]{
                    Feature.fromGeometry(Point.fromLngLat(
                        9.205394983291626,
                        45.47661043757903)),
                    Feature.fromGeometry(Point.fromLngLat(
                        9.223880767822266,
                        45.47623240235297)),
                    Feature.fromGeometry(Point.fromLngLat(
                        9.15530204772949,
                        45.4706650227671)),
                    Feature.fromGeometry(Point.fromLngLat(
                        9.153714179992676,
                        45.48625229963004)),
                    Feature.fromGeometry(Point.fromLngLat(
                        9.158306121826172,
                        45.482731998239636)),
                    Feature.fromGeometry(Point.fromLngLat(
                        9.188523888587952,
                        45.4923746929562)),
                    Feature.fromGeometry(Point.fromLngLat(
                        9.20929491519928,
                        45.45314676076135)),
                    Feature.fromGeometry(Point.fromLngLat(
                        9.177778959274292,
                        45.45569808340158))
                })
            )), new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                // Create a SymbolLayer and use the {@link com.mapbox.mapboxsdk.style.expressions.Expression.step()}
                // to adjust the SymbolLayer icon based on the zoom level. The blue person icon is set as the default
                // icon and then a step is used to switch to the blue person icon at a certain map camera zoom level.
                SymbolLayer singleLayer = new SymbolLayer("symbol-layer-id", "source-id");
                singleLayer.setProperties(
                    iconImage(step(zoom(), literal(BLUE_PERSON_ICON_ID),
                        stop(ZOOM_LEVEL_FOR_SWITCH, BLUE_PIN_ICON_ID))),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true));
                style.addLayer(singleLayer);

                Toast.makeText(SymbolSwitchOnZoomActivity.this,
                    R.string.zoom_map_in_and_out_icon_switch_instruction, Toast.LENGTH_SHORT).show();
                }
            }
    );
  }

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
