package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngBounds;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.CIRCLE_PITCH_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.Property.CIRCLE_PITCH_ALIGNMENT_VIEWPORT;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circlePitchAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Use the Mapbox Directions API to request and retrieve a Directions route. Show the route line and
 * place a circle where each of the route's step maneuver locations are.
 */
public class MultipleGeometriesDirectionsRouteActivity extends AppCompatActivity
    implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private static final String CIRCLE_GEOJSON_SOURCE_ID = "CIRCLE_GEOJSON_SOURCE_ID";
  private static final String LINE_GEOJSON_SOURCE_ID = "LINE_GEOJSON_SOURCE_ID";
  private static final String STEPS_CIRCLE_LAYER_ID = "steps-circle-layer";
  private static final String STEPS_BACKGROUND_CIRCLE_LAYER_ID = "steps-background-circle-layer";
  private static final String DIRECTIONS_ROUTE_LINE_LAYER_ID = "directions-line-layer";
  private static final String SETTLEMENT_LABEL_LAYER_ID = "settlement-label";

  // Adjust the following private static final variables to style this example's UI
  private static final String LINE_COLOR = "#EE2E23";
  private static final float LINE_WIDTH = 8f;
  private static final float CIRCLE_RADIUS = 8f;
  private static final float RADIUS_DIFFERENCE_BETWEEN_WAYPOINT_CIRCLES_AND_BACKGROUND_CIRCLES = 3f;
  private static final int CIRCLE_COLOR = Color.WHITE;
  private static final int BACKGROUND_CIRCLE_COLOR = Color.parseColor(LINE_COLOR);
  private static final boolean ALIGN_CIRCLES_WITH_MAP = true;

  private MapView mapView;
  private MapboxMap mapboxMap;
  private DirectionsRoute currentRoute;
  private Point origin = Point.fromLngLat(-122.39648, 37.7914277);
  private Point destination = Point.fromLngLat(-88.0430, 30.6944);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_multiple_geometries_from_directions_route);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(new Style.Builder().fromUri(Style.SATELLITE_STREETS)
      .withSource(new GeoJsonSource(CIRCLE_GEOJSON_SOURCE_ID))
      .withSource(new GeoJsonSource(LINE_GEOJSON_SOURCE_ID)), new Style.OnStyleLoaded() {
        @Override
          public void onStyleLoaded(@NonNull Style style) {
            initLineLayerForDirectionsRoute(style);
            initStepManeuverCircleLayer(style);
            initStepManeuverBackgroundCircleLayer(style);
            getRoute(origin, destination);
            mapboxMap.addOnMapClickListener(MultipleGeometriesDirectionsRouteActivity.this);
          Toast.makeText(MultipleGeometriesDirectionsRouteActivity.this,
              getString(R.string.tap_on_map_to_change_destination), Toast.LENGTH_SHORT).show();
          }
      });
  }

  /**
   * Create and style a LineLayer that will draw the Mapbox Directions API route line.
   *
   * @param loadedMapStyle the map's {@link Style} object
   */
  private void initLineLayerForDirectionsRoute(@NonNull Style loadedMapStyle) {
    LineLayer directionsRouteLineLayer = new LineLayer(DIRECTIONS_ROUTE_LINE_LAYER_ID, LINE_GEOJSON_SOURCE_ID);
    directionsRouteLineLayer.setProperties(
      lineColor(Color.parseColor(LINE_COLOR)),
      lineCap(Property.LINE_CAP_ROUND),
      lineWidth(LINE_WIDTH)
    );

    directionsRouteLineLayer.setFilter(eq(literal("$type"), literal("LineString")));

    // Add the layer below the "settlement-label" layer (city name labels, etc.)
    if (loadedMapStyle.getLayer(SETTLEMENT_LABEL_LAYER_ID) != null) {
      loadedMapStyle.addLayerBelow(directionsRouteLineLayer, SETTLEMENT_LABEL_LAYER_ID);
    } else {
      loadedMapStyle.addLayer(directionsRouteLineLayer);
    }
  }

  /**
   * Create and style a CircleLayer that will place circles for each of the Mapbox Directions API
   * route's step maneuver locations.
   * @param loadedMapStyle the map's {@link Style} object
   */
  private void initStepManeuverCircleLayer(@NonNull Style loadedMapStyle) {
    CircleLayer individualCirclesLayer = new CircleLayer(STEPS_CIRCLE_LAYER_ID, CIRCLE_GEOJSON_SOURCE_ID);
    individualCirclesLayer.setProperties(
      circleColor(CIRCLE_COLOR),
      circlePitchAlignment(ALIGN_CIRCLES_WITH_MAP ? CIRCLE_PITCH_ALIGNMENT_MAP : CIRCLE_PITCH_ALIGNMENT_VIEWPORT),
      circleRadius(CIRCLE_RADIUS));
    loadedMapStyle.addLayer(individualCirclesLayer);
  }

  /**
   * Create and style a CircleLayer that will place circles beneath the STEPS_CIRCLE_LAYER_ID layer
   * for each of the Mapbox Directions API route's step maneuver locations.
   * @param loadedMapStyle the map's {@link Style} object
   */
  private void initStepManeuverBackgroundCircleLayer(@NonNull Style loadedMapStyle) {
    CircleLayer individualCirclesLayer = new CircleLayer(STEPS_BACKGROUND_CIRCLE_LAYER_ID, CIRCLE_GEOJSON_SOURCE_ID);
    individualCirclesLayer.setProperties(
      circleColor(BACKGROUND_CIRCLE_COLOR),
      circlePitchAlignment(ALIGN_CIRCLES_WITH_MAP ? CIRCLE_PITCH_ALIGNMENT_MAP : CIRCLE_PITCH_ALIGNMENT_VIEWPORT),
      circleRadius(CIRCLE_RADIUS + RADIUS_DIFFERENCE_BETWEEN_WAYPOINT_CIRCLES_AND_BACKGROUND_CIRCLES));
    loadedMapStyle.addLayerBelow(individualCirclesLayer, STEPS_CIRCLE_LAYER_ID);
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
    getRoute(origin, destination);
    return true;
  }

  /**
   * Make a request to the Mapbox Directions API. Once successful, pass the route to the
   * route layer.
   *
   * @param origin      the starting point of the route
   * @param destination the desired finish point of the route
   */
  private void getRoute(Point origin, Point destination) {
    MapboxDirections directionsApiClient = MapboxDirections.builder()
      .origin(origin)
      .destination(destination)
      .overview(DirectionsCriteria.OVERVIEW_FULL)
      .profile(DirectionsCriteria.PROFILE_DRIVING)
      .steps(true)
      .accessToken(getString(R.string.access_token))
      .build();

    directionsApiClient.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        Timber.d("Response code: %s", response.code());
        if (response.body() == null) {
          Timber.e("Response body null. Try again later.");
          Toast.makeText(MultipleGeometriesDirectionsRouteActivity.this,
              getString(R.string.response_body_null), Toast.LENGTH_SHORT).show();
        } else if (response.body().routes().size() < 1) {
          Timber.e("No routes found");
          Toast.makeText(MultipleGeometriesDirectionsRouteActivity.this,
              getString(R.string.no_routes_found), Toast.LENGTH_SHORT).show();
        } else {
          // Get the directions route
          if (response.body() != null) {
            // Get the directions route
            currentRoute = response.body().routes().get(0);
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {
                // Retrieve the sources from the map
                GeoJsonSource circleLayerSource = style.getSourceAs(CIRCLE_GEOJSON_SOURCE_ID);
                GeoJsonSource lineLayerSource = style.getSourceAs(LINE_GEOJSON_SOURCE_ID);
                if (circleLayerSource != null && response.body() != null) {

                  List<Feature> featureList = new ArrayList<>();

                  // Use each step maneuver's location to create a Point Feature.
                  // The Feature is then added to the list.
                  if (currentRoute.legs().size() > 0) {
                    for (LegStep singleRouteLeg : currentRoute.legs().get(0).steps()) {
                      Point stepManeuverLocationPoint = singleRouteLeg.maneuver().location();
                      featureList.add(Feature.fromGeometry(stepManeuverLocationPoint));
                    }
                  } else {
                    Timber.d(getString(R.string.no_legs_toast));
                  }

                  // Update the CircleLayer's source with the Feature list.
                  circleLayerSource.setGeoJson(FeatureCollection.fromFeatures(featureList));

                  // Update the LineLayer's source with the Polyline route from the Directions API response.
                  if (lineLayerSource != null && currentRoute.geometry() != null) {
                    lineLayerSource.setGeoJson(Feature.fromGeometry(LineString.fromPolyline(
                        currentRoute.geometry(), PRECISION_6)));
                  }

                  // Ease the camera to fit to the Directions route.
                  easeCameraToShowEntireDirectionsRoute(new LatLng(origin.latitude(), origin.longitude()),
                      new LatLng(destination.latitude(), destination.longitude()));
                }
              }
            });
          }
        }
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Timber.e(throwable);
        Toast.makeText(MultipleGeometriesDirectionsRouteActivity.this,
          String.format("Error: %s", throwable.getMessage()),
          Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void easeCameraToShowEntireDirectionsRoute(LatLng origin, LatLng destination) {
    mapboxMap.easeCamera(newLatLngBounds(new LatLngBounds.Builder()
      .include(origin)
      .include(destination)
      .build(), 75), 2000);
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
