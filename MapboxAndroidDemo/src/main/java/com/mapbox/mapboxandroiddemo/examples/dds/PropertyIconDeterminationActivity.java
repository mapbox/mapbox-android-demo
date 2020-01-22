package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Display {@link SymbolLayer} icons on the map and determine the appropriate icon
 * based on a property in each {@link Feature}.
 */
public class PropertyIconDeterminationActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String RED_ICON_ID = "RED_ICON_ID";
  private static final String YELLOW_ICON_ID = "YELLOW_ICON_ID";
  private static final String LAYER_ID = "LAYER_ID";
  private static final String ICON_PROPERTY = "ICON_PROPERTY";
  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_property_icon_switch);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cj44mfrt20f082snokim4ungi")

      // Add the SymbolLayer icon image to the map style
      .withImage(RED_ICON_ID, BitmapFactory.decodeResource(
        PropertyIconDeterminationActivity.this.getResources(), R.drawable.red_marker))

      .withImage(YELLOW_ICON_ID, BitmapFactory.decodeResource(
        PropertyIconDeterminationActivity.this.getResources(), R.drawable.yellow_marker))

      // Adding a GeoJson source for the SymbolLayer icons.
      .withSource(new GeoJsonSource(SOURCE_ID,
        FeatureCollection.fromFeatures(initCoordinateData())))

      // Adding the actual SymbolLayer to the map style. The match expression will check the
      // ICON_PROPERTY property key and then use the partner value for the actual icon id.
      .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
        .withProperties(iconImage(match(
          get(ICON_PROPERTY), literal(RED_ICON_ID),
          stop(YELLOW_ICON_ID, YELLOW_ICON_ID),
          stop(RED_ICON_ID, RED_ICON_ID))),
          iconAllowOverlap(true),
          iconAnchor(Property.ICON_ANCHOR_BOTTOM))
      ), new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.

            PropertyIconDeterminationActivity.this.mapboxMap = mapboxMap;

            mapboxMap.addOnMapClickListener(PropertyIconDeterminationActivity.this);

            Toast.makeText(PropertyIconDeterminationActivity.this, R.string.tap_on_marker_instruction,
              Toast.LENGTH_SHORT).show();
          }
      });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
  }

  /**
   * This method handles click events for SymbolLayer symbols.
   *
   * @param screenPoint the point on screen clicked
   */
  private boolean handleClickIcon(PointF screenPoint) {
    List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
    if (!features.isEmpty()) {
      // Show the Feature in the TextView to show that the icon is based on the ICON_PROPERTY key/value
      TextView featureInfoTextView = findViewById(R.id.feature_info);
      featureInfoTextView.setText(features.get(0).toJson());
      return true;
    } else {
      return false;
    }
  }

  private List<Feature> initCoordinateData() {
    Feature singleFeatureOne = Feature.fromGeometry(
      Point.fromLngLat(72.88055419921875,
        19.05822387777432));
    singleFeatureOne.addStringProperty(ICON_PROPERTY, RED_ICON_ID);

    Feature singleFeatureTwo = Feature.fromGeometry(
      Point.fromLngLat(77.22015380859375,
        28.549544699103865));

    singleFeatureTwo.addStringProperty(ICON_PROPERTY, YELLOW_ICON_ID);

    Feature singleFeatureThree = Feature.fromGeometry(
      Point.fromLngLat(88.36647033691406,
        22.52016858599439));

    singleFeatureThree.addStringProperty(ICON_PROPERTY, RED_ICON_ID);

    // Not adding a ICON_PROPERTY property to fourth and fifth features in order to show off the default
    // nature of the match expression used in the example up above
    Feature singleFeatureFour = Feature.fromGeometry(
      Point.fromLngLat(78.42315673828125,
        17.43320034474222));

    Feature singleFeatureFive = Feature.fromGeometry(
      Point.fromLngLat(80.16448974609375,
        12.988500396985364));

    List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
    symbolLayerIconFeatureList.add(singleFeatureOne);
    symbolLayerIconFeatureList.add(singleFeatureTwo);
    symbolLayerIconFeatureList.add(singleFeatureThree);
    symbolLayerIconFeatureList.add(singleFeatureFour);
    symbolLayerIconFeatureList.add(singleFeatureFive);
    return symbolLayerIconFeatureList;
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