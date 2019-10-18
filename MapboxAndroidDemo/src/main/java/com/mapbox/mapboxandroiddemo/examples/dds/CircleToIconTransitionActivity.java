package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

/**
 * Create a smooth visual transition between circles and icons based on zooming in and out.
 */
public class CircleToIconTransitionActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final float BASE_CIRCLE_INITIAL_RADIUS = 3.4f;
  private static final float RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS = 14f;
  private static final float ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION = 11f;
  private static final float ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON = 12f;
  private static final float FINAL_OPACITY_OF_SHADING_CIRCLE = .5f;
  private static final String BASE_CIRCLE_COLOR = "#3BC802";
  private static final String SHADING_CIRCLE_COLOR = "#858585";
  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String ICON_LAYER_ID = "ICON_LAYER_ID";
  private static final String BASE_CIRCLE_LAYER_ID = "BASE_CIRCLE_LAYER_ID";
  private static final String SHADOW_CIRCLE_LAYER_ID = "SHADOW_CIRCLE_LAYER_ID";
  private static final String ICON_IMAGE_ID = "ICON_ID";
  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_circle_to_icon_transition);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    mapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT)

        // Add images to the map so that the SymbolLayers can reference the images.
        .withImage(ICON_IMAGE_ID, BitmapUtils.getBitmapFromDrawable(
          getResources().getDrawable(R.drawable.atm_symbol_icon)))

        // Add GeoJSON data to the GeoJsonSource and then add the GeoJsonSource to the map
        .withSource(new GeoJsonSource(SOURCE_ID,
          FeatureCollection.fromFeatures(initFeatureArray()))), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

              // Add the base CircleLayer, which will show small circles when the map is zoomed far enough
              // away from the map.
              CircleLayer baseCircleLayer = new CircleLayer(BASE_CIRCLE_LAYER_ID, SOURCE_ID).withProperties(
                circleColor(Color.parseColor(BASE_CIRCLE_COLOR)),
                circleRadius(
                  interpolate(
                    linear(), zoom(),
                    stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION, BASE_CIRCLE_INITIAL_RADIUS),
                    stop(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON, RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS)
                  )
                )
              );
              style.addLayer(baseCircleLayer);

              // Add a "shading" CircleLayer, whose circles' radii will match the radius of the SymbolLayer
              // circular icon
              CircleLayer shadowTransitionCircleLayer = new CircleLayer(SHADOW_CIRCLE_LAYER_ID, SOURCE_ID)
                .withProperties(
                  circleColor(Color.parseColor(SHADING_CIRCLE_COLOR)),
                  circleRadius(RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS),
                  circleOpacity(
                    interpolate(
                      linear(), zoom(),
                      stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION - .5, 0),
                      stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION, FINAL_OPACITY_OF_SHADING_CIRCLE)
                    )
                  )
                );
              style.addLayerBelow(shadowTransitionCircleLayer, BASE_CIRCLE_LAYER_ID);

              // Add the SymbolLayer
              SymbolLayer symbolIconLayer = new SymbolLayer(ICON_LAYER_ID, SOURCE_ID);
              symbolIconLayer.withProperties(
                iconImage(ICON_IMAGE_ID),
                iconSize(1.5f),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
              );

              symbolIconLayer.setMinZoom(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON);
              style.addLayer(symbolIconLayer);

              Toast.makeText(CircleToIconTransitionActivity.this,
                R.string.zoom_map_in_and_out_circle_to_icon_transition, Toast.LENGTH_SHORT).show();

              mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                  .zoom(12.5)
                  .build()), 3000);
            }
          }
    );
  }

  private Feature[] initFeatureArray() {
    return new Feature[] {
      Feature.fromGeometry(Point.fromLngLat(
        135.516316,
        34.681345)),
      Feature.fromGeometry(Point.fromLngLat(
        135.509537,
        34.707929)),
      Feature.fromGeometry(Point.fromLngLat(
        135.487953,
        34.680369)),
      Feature.fromGeometry(Point.fromLngLat(
        135.479682,
        34.698283)),
      Feature.fromGeometry(Point.fromLngLat(
        135.499368,
        34.708894)),
      Feature.fromGeometry(Point.fromLngLat(
        135.469701,
        34.691089)),
      Feature.fromGeometry(Point.fromLngLat(
        135.471265,
        34.672435)),
      Feature.fromGeometry(Point.fromLngLat(
        135.485418,
        34.704285)),
      Feature.fromGeometry(Point.fromLngLat(
        135.493762,
        34.669337)),
      Feature.fromGeometry(Point.fromLngLat(
        135.509407,
        34.696032)),
      Feature.fromGeometry(Point.fromLngLat(
        135.492719,
        34.68424)),
      Feature.fromGeometry(Point.fromLngLat(
        135.51045,
        34.684133)),
      Feature.fromGeometry(Point.fromLngLat(
        135.500802,
        34.700212)),
      Feature.fromGeometry(Point.fromLngLat(
        135.519576,
        34.698712)),
      Feature.fromGeometry(Point.fromLngLat(
        135.502888,
        34.67888)),
      Feature.fromGeometry(Point.fromLngLat(
        135.518533,
        34.67116))
    };
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
