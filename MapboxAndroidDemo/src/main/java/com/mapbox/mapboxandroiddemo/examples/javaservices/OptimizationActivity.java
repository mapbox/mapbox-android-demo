package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
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
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


/**
 * Use Mapbox Java Services to request and compare normal directions with time optimized directions.
 */
public class OptimizationActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener {

  private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
  private static final String FIRST = "first";
  private static final String ANY = "any";
  private static final String TEAL_COLOR = "#23D2BE";
  private static final float POLYLINE_WIDTH = 5;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private DirectionsRoute optimizedRoute;
  private MapboxOptimization optimizedClient;
  private List<Point> stops = new ArrayList<>();
  private Point origin;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_optimization);

    // Add the origin Point to the list
    addFirstStopToStopsList();

    // Setup the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Add origin and destination to the mapboxMap
        initMarkerIconSymbolLayer(style);
        initOptimizedRouteLineLayer(style);
        Toast.makeText(OptimizationActivity.this, R.string.click_instructions, Toast.LENGTH_SHORT).show();
        mapboxMap.addOnMapClickListener(OptimizationActivity.this);
        mapboxMap.addOnMapLongClickListener(OptimizationActivity.this);
      }
    });
  }

  private void initMarkerIconSymbolLayer(@NonNull Style loadedMapStyle) {
    // Add the marker image to map
    loadedMapStyle.addImage("icon-image", BitmapFactory.decodeResource(
      this.getResources(), R.drawable.red_marker));

    // Add the source to the map

    loadedMapStyle.addSource(new GeoJsonSource(ICON_GEOJSON_SOURCE_ID,
      Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude()))));

    loadedMapStyle.addLayer(new SymbolLayer("icon-layer-id", ICON_GEOJSON_SOURCE_ID).withProperties(
      iconImage("icon-image"),
      iconSize(1f),
      iconAllowOverlap(true),
      iconIgnorePlacement(true),
      iconOffset(new Float[] {0f, -7f})
    ));
  }

  private void initOptimizedRouteLineLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource(new GeoJsonSource("optimized-route-source-id"));
    loadedMapStyle.addLayerBelow(new LineLayer("optimized-route-layer-id", "optimized-route-source-id")
      .withProperties(
        lineColor(Color.parseColor(TEAL_COLOR)),
        lineWidth(POLYLINE_WIDTH)
      ), "icon-layer-id");
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    // Optimization API is limited to 12 coordinate sets
    if (alreadyTwelveMarkersOnMap()) {
      Toast.makeText(OptimizationActivity.this, R.string.only_twelve_stops_allowed, Toast.LENGTH_LONG).show();
    } else {
      Style style = mapboxMap.getStyle();
      if (style != null) {
        addDestinationMarker(style, point);
        addPointToStopsList(point);
        getOptimizedRoute(style, stops);
      }
    }
    return true;
  }

  @Override
  public boolean onMapLongClick(@NonNull LatLng point) {
    stops.clear();
    if (mapboxMap != null) {
      Style style = mapboxMap.getStyle();
      if (style != null) {
        resetDestinationMarkers(style);
        removeOptimizedRoute(style);
        addFirstStopToStopsList();
        return true;
      }
    }
    return false;
  }

  private void resetDestinationMarkers(@NonNull Style style) {
    GeoJsonSource optimizedLineSource = style.getSourceAs(ICON_GEOJSON_SOURCE_ID);
    if (optimizedLineSource != null) {
      optimizedLineSource.setGeoJson(Point.fromLngLat(origin.longitude(), origin.latitude()));
    }
  }

  private void removeOptimizedRoute(@NonNull Style style) {
    GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
    if (optimizedLineSource != null) {
      optimizedLineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {}));
    }
  }

  private boolean alreadyTwelveMarkersOnMap() {
    return stops.size() == 12;
  }

  private void addDestinationMarker(@NonNull Style style, LatLng point) {
    List<Feature> destinationMarkerList = new ArrayList<>();
    for (Point singlePoint : stops) {
      destinationMarkerList.add(Feature.fromGeometry(
        Point.fromLngLat(singlePoint.longitude(), singlePoint.latitude())));
    }
    destinationMarkerList.add(Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude())));
    GeoJsonSource iconSource = style.getSourceAs(ICON_GEOJSON_SOURCE_ID);
    if (iconSource != null) {
      iconSource.setGeoJson(FeatureCollection.fromFeatures(destinationMarkerList));
    }
  }

  private void addPointToStopsList(LatLng point) {
    stops.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
  }

  private void addFirstStopToStopsList() {
    // Set first stop
    origin = Point.fromLngLat(30.335098600000038, 59.9342802);
    stops.add(origin);
  }

  private void getOptimizedRoute(@NonNull final Style style, List<Point> coordinates) {
    optimizedClient = MapboxOptimization.builder()
      .source(FIRST)
      .destination(ANY)
      .coordinates(coordinates)
      .overview(DirectionsCriteria.OVERVIEW_FULL)
      .profile(DirectionsCriteria.PROFILE_DRIVING)
      .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
      .build();

    optimizedClient.enqueueCall(new Callback<OptimizationResponse>() {
      @Override
      public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
        if (!response.isSuccessful()) {
          Timber.d(getString(R.string.no_success));
          Toast.makeText(OptimizationActivity.this, R.string.no_success, Toast.LENGTH_SHORT).show();
        } else {
          if (response.body() != null) {
            List<DirectionsRoute> routes = response.body().trips();
            if (routes != null) {
              if (routes.isEmpty()) {
                Timber.d("%s size = %s", getString(R.string.successful_but_no_routes), routes.size());
                Toast.makeText(OptimizationActivity.this, R.string.successful_but_no_routes,
                  Toast.LENGTH_SHORT).show();
              } else {
                // Get most optimized route from API response
                optimizedRoute = routes.get(0);
                drawOptimizedRoute(style, optimizedRoute);
              }
            } else {
              Timber.d("list of routes in the response is null");
              Toast.makeText(OptimizationActivity.this, String.format(getString(R.string.null_in_response),
                "The Optimization API response's list of routes"), Toast.LENGTH_SHORT).show();
            }
          } else {
            Timber.d("response.body() is null");
            Toast.makeText(OptimizationActivity.this, String.format(getString(R.string.null_in_response),
              "The Optimization API response's body"), Toast.LENGTH_SHORT).show();
          }
        }
      }

      @Override
      public void onFailure(Call<OptimizationResponse> call, Throwable throwable) {
        Timber.d("Error: %s", throwable.getMessage());
      }
    });
  }

  private void drawOptimizedRoute(@NonNull Style style, DirectionsRoute route) {
    GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
    if (optimizedLineSource != null) {
      optimizedLineSource.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
        LineString.fromPolyline(route.geometry(), PRECISION_6))));
    }
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
    if (optimizedClient != null) {
      optimizedClient.cancelCall();
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
