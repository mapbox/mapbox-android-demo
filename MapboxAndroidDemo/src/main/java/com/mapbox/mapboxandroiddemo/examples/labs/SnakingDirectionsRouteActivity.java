package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.directions.v5.models.StepIntersection;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.api.directions.v5.DirectionsCriteria.GEOMETRY_POLYLINE;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Rather than showing the directions all at once, have it "snake" from the origin to destination
 */

public class SnakingDirectionsRouteActivity extends AppCompatActivity
  implements OnMapReadyCallback {

  private static final float NAVIGATION_LINE_WIDTH = 6;
  private static final String DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID = "DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID";
  private static final String DRIVING_ROUTE_POLYLINE_SOURCE_ID = "DRIVING_ROUTE_POLYLINE_SOURCE_ID";
  private static final String TAG = "SnakingRouteActivity";
  private MapView mapView;
  private MapboxMap map;
  private MapboxDirections client;
  // Origin point in Paris, France
  private static final Point origin = Point.fromLngLat(2.35222, 48.856614);

  // Destination point in Lyon, France
  private static  Point destination = Point.fromLngLat(4.83565, 45.76404);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_snaking_directions_route);

    // Setup the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.map = mapboxMap;

    map.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        initDrivingRouteSourceAndLayer(style);

        addMarkerIconsToMap(style);

        // Get route from API
        getDirectionsRoute(origin, destination);
      }
    });
  }

  private void addMarkerIconsToMap(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage("icon-id", BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.red_marker)));

    loadedMapStyle.addSource(new GeoJsonSource("source-id",
      FeatureCollection.fromFeatures(new Feature[] {
        Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
        Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude())),
      })));

    loadedMapStyle.addLayer(new SymbolLayer("layer-id",
      "source-id").withProperties(
      iconImage("icon-id"),
      iconOffset(new Float[]{0f,-8f})
    ));
  }


  private void initDrivingRouteSourceAndLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource( new GeoJsonSource(DRIVING_ROUTE_POLYLINE_SOURCE_ID,
      FeatureCollection.fromFeatures(new Feature[] {})));
    loadedMapStyle.addLayer(new LineLayer(DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID,
      DRIVING_ROUTE_POLYLINE_SOURCE_ID)
      .withProperties(
        lineWidth(NAVIGATION_LINE_WIDTH),
        lineOpacity(.3f),
        lineCap(LINE_CAP_ROUND),
        lineJoin(LINE_JOIN_ROUND),
        lineColor(Color.parseColor("#d742f4"))
      ));
  }

  /**
   * Build the Mapbox Directions API request
   *
   * @param origin      The starting point for the directions route
   * @param destination The final point for the directions route
   */
  private void getDirectionsRoute(Point origin, Point destination) {
    client = MapboxDirections.builder()
      .origin(origin)
      .destination(destination)
      .overview(DirectionsCriteria.OVERVIEW_FULL)
      .profile(DirectionsCriteria.PROFILE_DRIVING)
      .geometries(GEOMETRY_POLYLINE)
      .alternatives(true)
      .steps(true)
      .accessToken(getString(R.string.access_token))
      .build();

    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

        // Create log messages in case no response or routes are present
        if (response.body() == null) {
          Log.d(TAG, "No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().routes().size() < 1) {
          Log.d(TAG, "No routes found");
          return;
        }

        // Get the route from the Mapbox Directions API
        DirectionsRoute currentRoute = response.body().routes().get(0);
        List<Point> directionsPointsForLineLayer = new ArrayList<>();
        for (int i = 0; i < currentRoute.legs().size(); i++) {
          RouteLeg leg = currentRoute.legs().get(i);
          List<LegStep> steps = leg.steps();
          for (int j = 0; j < steps.size(); j++) {
            LegStep step = steps.get(j);
            List<StepIntersection> intersections = step.intersections();
            for (int k = 0; k < intersections.size(); k++) {
              Point location = intersections.get(k).location();
              directionsPointsForLineLayer.add(Point.fromLngLat(location.longitude(), location.latitude()));
              List<Feature> drivingRoutePolyLineFeatureList = new ArrayList<>();
              LineString lineString = LineString.fromLngLats(directionsPointsForLineLayer);
              List<Point> coordinates = lineString.coordinates();
              for (int x = 0; x < coordinates.size(); x++) {
                drivingRoutePolyLineFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(coordinates)));
              }

              // Update the GeoJSON source
              GeoJsonSource source = map.getStyle().getSourceAs(DRIVING_ROUTE_POLYLINE_SOURCE_ID);
              if (source != null) {
                source.setGeoJson(FeatureCollection.fromFeatures(drivingRoutePolyLineFeatureList));
              }
            }
          }
        }
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Log.d("SnakingDirections", "Error: " + throwable.getMessage());
        Toast.makeText(SnakingDirectionsRouteActivity.this,
          R.string.snaking_directions_activity_error, Toast.LENGTH_SHORT).show();
      }
    });
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
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Cancel the directions API request
    if (client != null) {
      client.cancelCall();
    }
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
