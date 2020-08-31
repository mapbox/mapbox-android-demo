package com.mapbox.mapboxandroiddemo.examples.extrusions;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;

import java.util.List;

import static android.graphics.Color.parseColor;
import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.id;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;

/**
 * Tap on the map to extrude a single 3D building extrusion. This is done by applying a
 * filter to a Maps SDK {@link FillExtrusionLayer}.
 */
public class SingleHighlightedBuildingExtrusionActivity extends AppCompatActivity
    implements MapboxMap.OnMapClickListener {

  private static final String EXTRUSION_BUILDING_LAYER_LAYER_ID = "EXTRUSION_BUILDING_LAYER_LAYER_ID";
  private static final String COMPOSITE_SOURCE_ID = "composite";
  private static final String DEFAULT_BUILDING_ID = "0";
  private static final String BUILDING_LAYER_ID = "building";
  private static final String EXTRUSION_COLOR = "#fb14ff";
  private static final float EXTRUSION_OPACITY = 0.8f;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean firstBuildingTapHasHappened = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_single_highlighted_building_extrusion);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        SingleHighlightedBuildingExtrusionActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT),
          new Style.OnStyleLoaded() {
            @Override
        public void onStyleLoaded(@NonNull Style style) {
              addExtrusionLayerToMap(style);
              mapboxMap.addOnMapClickListener(SingleHighlightedBuildingExtrusionActivity.this);
              Toast.makeText(SingleHighlightedBuildingExtrusionActivity.this,
                  R.string.tap_on_building,
                  Toast.LENGTH_SHORT).show();
            }
          });
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng mapTapLatLng) {
    setFilterOnCorrectBuilding(mapTapLatLng);
    return true;
  }

  /**
   * Sets a filter on the {@link FillExtrusionLayer}.
   *
   * @param clickLatLng The {@link LatLng} of wherever the map was tapped.
   */
  private void setFilterOnCorrectBuilding(LatLng clickLatLng) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        FillExtrusionLayer buildingExtrusionLayer = style.getLayerAs(EXTRUSION_BUILDING_LAYER_LAYER_ID);
        if (buildingExtrusionLayer != null) {
          buildingExtrusionLayer.setFilter(
              getBuildingFilterExpression(getBuildingId(clickLatLng)));
        }
      }
    });
  }

  /**
   * Returns the correctly built {@link Expression} to apply to the {@link FillExtrusionLayer}.
   *
   * @param buildingId the ID of the selected building
   * @return an {@link Expression} to use in a filter
   */
  private Expression getBuildingFilterExpression(String buildingId) {
    return all(
        eq(get("extrude"), "true"),
        eq(get("underground"), "false"),
        eq(Expression.toString(id()), Expression.toString(literal(buildingId)))
    );
  }

  /**
   * Gets the {@link Feature#id()} of the building {@link Feature} that has the queryLatLng
   * within its footprint. This ID is then used in the filter that's applied to the
   * {@link FillExtrusionLayer}.
   *
   * @param queryLatLng the {@link LatLng} to use for querying the {@link MapboxMap} to eventually
   *          get the building ID.
   * @return the selected building's ID
   */
  private String getBuildingId(LatLng queryLatLng) {
    List<Feature> renderedBuildingFootprintFeatures = mapboxMap.queryRenderedFeatures(
        mapboxMap.getProjection().toScreenLocation(queryLatLng), BUILDING_LAYER_ID);
    if (!renderedBuildingFootprintFeatures.isEmpty()) {
      if (!firstBuildingTapHasHappened) {
        Toast.makeText(SingleHighlightedBuildingExtrusionActivity.this,
            R.string.keep_tapping_on_building_footprint,
            Toast.LENGTH_SHORT).show();
      }
      firstBuildingTapHasHappened = true;
      return renderedBuildingFootprintFeatures.get(0).id();
    }
    return DEFAULT_BUILDING_ID;
  }

  /**
   * Adds a {@link FillExtrusionLayer} to the map.
   *
   * @param loadedMapStyle A loaded {@link Style} on the {@link MapboxMap}.
   */
  private void addExtrusionLayerToMap(@NonNull Style loadedMapStyle) {
    FillExtrusionLayer fillExtrusionLayer = new FillExtrusionLayer(
        EXTRUSION_BUILDING_LAYER_LAYER_ID, COMPOSITE_SOURCE_ID);
    fillExtrusionLayer.setSourceLayer(BUILDING_LAYER_ID);
    fillExtrusionLayer.setProperties(
        fillExtrusionColor(parseColor(EXTRUSION_COLOR)),
        fillExtrusionColor(parseColor(EXTRUSION_COLOR)),
        fillExtrusionHeight(get("height")),
        fillExtrusionOpacity(EXTRUSION_OPACITY));
    loadedMapStyle.addLayer(fillExtrusionLayer);
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
  protected void onDestroy() {
    super.onDestroy();
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }
}

