package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.graphics.Color.parseColor;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use layer filters and Feature properties to create the visual effect of
 * toggling between circles and icons when they're tapped on.
 */
public class CircleIconToggleOnClickActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private static final String CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID";
  private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String MARKER_ICON_ID = "MARKER_ICON_ID";
  private static final String PROPERTY_ID = "PROPERTY_ID";
  private static final String PROPERTY_SELECTED = "PROPERTY_SELECTED";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private FeatureCollection featureCollection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_circle_icon_toggle_on_click);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cj4k8wmwy5lbt2smsigkbh18e"),
      new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          CircleIconToggleOnClickActivity.this.mapboxMap = mapboxMap;

          mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
              .zoom(4)
              .target(new LatLng(-19.476950206488414, 46.58203125))
              .build()), 2000);

          initFeatureCollection();

          style.addSource(new GeoJsonSource(SOURCE_ID, featureCollection));

          // Add the CircleLayer and set the filter so that circle are only shown
          // if the PROPERTY_SELECTED boolean property is false.
          CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, SOURCE_ID)
            .withProperties(
              circleRadius(interpolate(linear(), zoom(),
                stop(2, 5f),
                stop(3, 20f)
              )),
              circleColor(parseColor("#2196F3")));
          circleLayer.setFilter(eq(get(PROPERTY_SELECTED), literal(false)));
          style.addLayer(circleLayer);

          // Add the marker icon image to the map
          style.addImage(MARKER_ICON_ID, BitmapFactory.decodeResource(
            CircleIconToggleOnClickActivity.this.getResources(), R.drawable.blue_marker_view));

          // Add the SymbolLayer and set the filter so that circle are only shown
          // if the PROPERTY_SELECTED boolean property is true.
          SymbolLayer symbolLayer = new SymbolLayer(MARKER_LAYER_ID, SOURCE_ID)
            .withProperties(iconImage(MARKER_ICON_ID),
              iconAllowOverlap(true),
              iconOffset(new Float[] {0f, -13f})
            );
          symbolLayer.setFilter(eq(get(PROPERTY_SELECTED), literal(true)));
          style.addLayer(symbolLayer);

          mapboxMap.addOnMapClickListener(CircleIconToggleOnClickActivity.this);

          Toast.makeText(CircleIconToggleOnClickActivity.this, R.string.tap_on_map_to_toggle_instruction,
            Toast.LENGTH_SHORT).show();
        }
      });
  }

  /**
   * Create sample data to use for both the {@link CircleLayer} and
   * {@link SymbolLayer}.
   */
  private void initFeatureCollection() {
    List<Feature> markerCoordinates = new ArrayList<>();

    Feature featureOne = Feature.fromGeometry(
      Point.fromLngLat(45.37353515625, -14.32825967774));
    featureOne.addStringProperty(PROPERTY_ID, "1");
    featureOne.addBooleanProperty(PROPERTY_SELECTED, false);
    markerCoordinates.add(featureOne);

    Feature featureTwo = Feature.fromGeometry(
      Point.fromLngLat(50.1416015625, -20.200346006493735));
    featureTwo.addStringProperty(PROPERTY_ID, "2");
    featureTwo.addBooleanProperty(PROPERTY_SELECTED, false);
    markerCoordinates.add(featureTwo);

    Feature featureThree = Feature.fromGeometry(
      Point.fromLngLat(42.86865234375, -24.266997288418157));
    featureThree.addStringProperty(PROPERTY_ID, "3");
    featureThree.addBooleanProperty(PROPERTY_SELECTED, false);
    markerCoordinates.add(featureThree);

    featureCollection = FeatureCollection.fromFeatures(markerCoordinates);
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
    return true;
  }

  /**
   * This method handles click events for both layers.
   * <p>
   * The PROPERTY_SELECTED feature property is set to its opposite, so
   * that the visual toggling between circles and icons is correct.
   *
   * @param screenPoint the point on screen clicked
   */
  private boolean handleClickIcon(PointF screenPoint) {
    List<Feature> selectedCircleFeatureList = mapboxMap.queryRenderedFeatures(screenPoint, CIRCLE_LAYER_ID);
    List<Feature> selectedMarkerFeatureList = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);

    if (!selectedCircleFeatureList.isEmpty()) {
      Feature selectedCircleFeature = selectedCircleFeatureList.get(0);

      for (int x = 0; x < featureCollection.features().size(); x++) {

        if (selectedCircleFeature.getStringProperty(PROPERTY_ID)
          .equals(featureCollection.features().get(x).getStringProperty(PROPERTY_ID))) {

          if (featureSelectStatusIsTrue(selectedCircleFeature)) {
            setFeatureSelectState(x, featureCollection.features().get(x), true);
          } else {
            setSelected(x);
          }
        }
      }
    } else if (!selectedMarkerFeatureList.isEmpty()) {
      Feature selectedMarkerFeature = selectedMarkerFeatureList.get(0);

      for (int x = 0; x < featureCollection.features().size(); x++) {

        if (selectedMarkerFeature.getStringProperty(PROPERTY_ID)
          .equals(featureCollection.features().get(x).getStringProperty(PROPERTY_ID))) {

          if (featureSelectStatusIsTrue(selectedMarkerFeature)) {
            setFeatureSelectState(x, featureCollection.features().get(x), false);
          } else {
            setSelected(x);
          }
        }
      }
    } else {
      // Reset all features to unselected so that all circles are shown and no icons are shown
      for (int x = 0; x < featureCollection.features().size(); x++) {
        setFeatureSelectState(x, featureCollection.features().get(x), false);
      }
    }
    return true;
  }

  /**
   * Set a feature selected state.
   *
   * @param index the index of selected feature
   */
  private void setSelected(int index) {
    if (featureCollection.features() != null) {
      Feature feature = featureCollection.features().get(index);
      setFeatureSelectState(index, feature, true);
      refreshSource();
    }
  }

  /**
   * Updates the display of data on the map after the FeatureCollection has been modified
   */
  private void refreshSource() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        GeoJsonSource geoJsonSource = style.getSourceAs(SOURCE_ID);
        if (geoJsonSource != null && featureCollection != null) {
          geoJsonSource.setGeoJson(featureCollection);
        }
      }
    });
  }

  /**
   * Selects the state of a feature
   *
   * @param feature the feature to be selected.
   */
  private void setFeatureSelectState(int index, Feature feature, boolean selectedState) {
    feature.addBooleanProperty(PROPERTY_SELECTED, selectedState);
    featureCollection.features().set(index, feature);
    refreshSource();
  }

  /**
   * Checks whether a Feature's boolean "selected" property is true or false
   *
   * @param selectedFeature the specific Feature to check
   * @return true if "selected" is true. False if the boolean property is false.
   */
  private boolean featureSelectStatusIsTrue(Feature selectedFeature) {
    if (featureCollection == null) {
      return false;
    }
    return selectedFeature.getBooleanProperty(PROPERTY_SELECTED);
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
