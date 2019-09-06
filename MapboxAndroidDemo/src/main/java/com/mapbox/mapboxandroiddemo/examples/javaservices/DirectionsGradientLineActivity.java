package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.graphics.Color.parseColor;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lineProgress;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineGradient;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Make a directions request with the Mapbox Directions API and then draw a line behind a moving
 * SymbolLayer icon which moves along the Directions response route.
 */
public class DirectionsGradientLineActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

  private static final String ORIGIN_ICON_ID = "origin-icon-id";
  private static final String DESTINATION_ICON_ID = "destination-icon-id";
  private static final String ROUTE_LAYER_ID = "route-layer-id";
  private static final String ROUTE_LINE_SOURCE_ID = "route-source-id";
  private static final String ICON_LAYER_ID = "icon-layer-id";
  private static final String ICON_SOURCE_ID = "icon-source-id";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private DirectionsRoute currentRoute;
  private MapboxDirections client;

  // Adjust variables below to change the example's UI
  private static Point ORIGIN_POINT = Point.fromLngLat(106.7140180, -6.149120972);
  private static Point DESTINATION_POINT = Point.fromLngLat(106.9687635, -6.304752436);
  private static final float LINE_WIDTH = 6f;
  private static final String ORIGIN_COLOR = "#2096F3";
  private static final String DESTINATION_COLOR = "#F84D4D";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_directions_gradient);

    // Setup the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        DirectionsGradientLineActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.DARK)
          .withImage(ORIGIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
            getResources().getDrawable(R.drawable.blue_marker)))
          .withImage(DESTINATION_ICON_ID, BitmapUtils.getBitmapFromDrawable(
            getResources().getDrawable(R.drawable.red_marker))), new Style.OnStyleLoaded() {
              @Override
                public void onStyleLoaded(@NonNull Style style) {
                  initSources(style);
                  initLayers(style);

                  // Get the directions route from the Mapbox Directions API
                  getRoute(mapboxMap, ORIGIN_POINT, DESTINATION_POINT);

                  mapboxMap.addOnMapClickListener(DirectionsGradientLineActivity.this);

                  Toast.makeText(DirectionsGradientLineActivity.this,
                    getString(R.string.tap_map_instruction_gradient_directions), Toast.LENGTH_SHORT).show();
              }
            });
      }
    });
  }

  /**
   * Add the route and marker sources to the map
   */
  private void initSources(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource(new GeoJsonSource(ROUTE_LINE_SOURCE_ID, new GeoJsonOptions().withLineMetrics(true)));
    loadedMapStyle.addSource(new GeoJsonSource(ICON_SOURCE_ID, getOriginAndDestinationFeatureCollection()));
  }

  /**
   * Util method that returns a {@link FeatureCollection} with the latest origin and destination locations.
   *
   * @return a {@link FeatureCollection} to be used for creating a new {@link GeoJsonSource} or
   * updating a source's GeoJSON.
   */
  private FeatureCollection getOriginAndDestinationFeatureCollection() {
    Feature originFeature = Feature.fromGeometry(ORIGIN_POINT);
    originFeature.addStringProperty("originDestination", "origin");
    Feature destinationFeature = Feature.fromGeometry(DESTINATION_POINT);
    destinationFeature.addStringProperty("originDestination", "destination");
    return FeatureCollection.fromFeatures(new Feature[] {originFeature, destinationFeature});
  }

  /**
   * Add the route and marker icon layers to the map
   */
  private void initLayers(@NonNull Style loadedMapStyle) {
    // Add the LineLayer to the map. This layer will display the directions route.
    loadedMapStyle.addLayer(new LineLayer(ROUTE_LAYER_ID, ROUTE_LINE_SOURCE_ID).withProperties(
      lineCap(Property.LINE_CAP_ROUND),
      lineJoin(Property.LINE_JOIN_ROUND),
      lineWidth(LINE_WIDTH),
      lineGradient(interpolate(
        linear(), lineProgress(),

        // This is where the gradient color effect is set. 0 -> 1 makes it a two-color gradient
        // See LineGradientActivity for an example of a 2+ multiple color gradient line.
        stop(0f, color(parseColor(ORIGIN_COLOR))),
        stop(1f, color(parseColor(DESTINATION_COLOR)))
      ))));

    // Add the SymbolLayer to the map to show the origin and destination pin markers
    loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
      iconImage(match(get("originDestination"), literal("origin"),
        stop("origin", ORIGIN_ICON_ID),
        stop("destination", DESTINATION_ICON_ID))),
      iconIgnorePlacement(true),
      iconAllowOverlap(true),
      iconOffset(new Float[] {0f, -4f})));
  }

  /**
   * Make a request to the Mapbox Directions API. Once successful, pass the route to the
   * route layer.
   *
   * @param mapboxMap   the Mapbox map object that the route will be drawn on
   * @param origin      the starting point of the route
   * @param destination the desired finish point of the route
   */
  private void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
    client = MapboxDirections.builder()
      .origin(origin)
      .destination(destination)
      .overview(DirectionsCriteria.OVERVIEW_FULL)
      .profile(DirectionsCriteria.PROFILE_WALKING)
      .accessToken(getString(R.string.access_token))
      .build();
    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        // You can get the generic HTTP info about the response
        Timber.d("Response code: %s", response.code());

        if (response.body() == null) {
          Timber.e("No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().routes().size() < 1) {
          Timber.e("No routes found");
          return;
        }

        // Get the Direction API response's route
        currentRoute = response.body().routes().get(0);

        if (currentRoute != null) {
          if (mapboxMap != null) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                // Retrieve and update the source designated for showing the directions route
                GeoJsonSource originDestinationPointGeoJsonSource = style.getSourceAs(ICON_SOURCE_ID);

                if (originDestinationPointGeoJsonSource != null) {
                  originDestinationPointGeoJsonSource.setGeoJson(getOriginAndDestinationFeatureCollection());
                }

                // Retrieve and update the source designated for showing the directions route
                GeoJsonSource lineLayerRouteGeoJsonSource = style.getSourceAs(ROUTE_LINE_SOURCE_ID);

                // Create a LineString with the directions route's geometry and
                // reset the GeoJSON source for the route LineLayer source
                if (lineLayerRouteGeoJsonSource != null) {
                  // Create the LineString from the list of coordinates and then make a GeoJSON
                  // FeatureCollection so we can add the line to our map as a layer.
                  LineString lineString = LineString.fromPolyline(currentRoute.geometry(), PRECISION_6);
                  lineLayerRouteGeoJsonSource.setGeoJson(Feature.fromGeometry(lineString));
                }
              }
            });
          }
        } else {
          Timber.d("Directions route is null");
          Toast.makeText(DirectionsGradientLineActivity.this,
            getString(R.string.route_can_not_be_displayed), Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Toast.makeText(DirectionsGradientLineActivity.this,
            getString(R.string.route_call_failure), Toast.LENGTH_SHORT).show();
      }
    });
  }


  @Override
  public boolean onMapClick(@NonNull LatLng mapClickPoint) {
    // Move the destination point to wherever the map was tapped
    DESTINATION_POINT = Point.fromLngLat(mapClickPoint.getLongitude(), mapClickPoint.getLatitude());

    // Get a new Directions API route to that new destination and eventually re-draw the
    // gradient route line.
    getRoute(mapboxMap, ORIGIN_POINT, DESTINATION_POINT);
    return true;
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
    // Cancel the Directions API request
    if (client != null) {
      client.cancelCall();
    }
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}


