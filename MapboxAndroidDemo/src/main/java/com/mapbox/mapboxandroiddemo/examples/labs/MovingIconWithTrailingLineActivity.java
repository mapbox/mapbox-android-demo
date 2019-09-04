package com.mapbox.mapboxandroiddemo.examples.labs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;
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
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Make a directions request with the Mapbox Directions API and then draw a line behind a moving
 * SymbolLayer icon which moves along the Directions response route.
 */
public class MovingIconWithTrailingLineActivity extends AppCompatActivity {

  private static final String DOT_SOURCE_ID = "dot-source-id";
  private static final String LINE_SOURCE_ID = "line-source-id";

  private MapView mapView;
  private MapboxMap mapboxMap;

  private GeoJsonSource pointSource;
  private GeoJsonSource lineSource;
  private List<Point> routeCoordinateList;
  private List<Point> markerLinePointList = new ArrayList<>();
  private int routeIndex;
  private Point originPoint = Point.fromLngLat(38.7508, 9.0309);
  private Point destinationPoint = Point.fromLngLat(38.795902, 8.984467);
  private Animator currentAnimator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_moving_icon_with_trailing_line);

    // Initialize the mapboxMap view
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        MovingIconWithTrailingLineActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            // Use the Mapbox Directions API to get a directions route
            getRoute(originPoint, destinationPoint);
          }
        });
      }
    });
  }

  /**
   * Add data to the map once the GeoJSON has been loaded
   *
   * @param featureCollection returned GeoJSON FeatureCollection from the Directions API route request
   */
  private void initData(Style fullyLoadedStyle, @NonNull FeatureCollection featureCollection) {
    if (featureCollection.features() != null) {
      LineString lineString = ((LineString) featureCollection.features().get(0).geometry());
      if (lineString != null) {
        routeCoordinateList = lineString.coordinates();
        initSources(fullyLoadedStyle, featureCollection);
        initSymbolLayer(fullyLoadedStyle);
        initDotLinePath(fullyLoadedStyle);
        animate();
      }
    }
  }

  /**
   * Set up the repeat logic for moving the icon along the route.
   */
  private void animate() {
    // Check if we are at the end of the points list
    if ((routeCoordinateList.size() - 1 > routeIndex)) {
      Point indexPoint = routeCoordinateList.get(routeIndex);
      Point newPoint = Point.fromLngLat(indexPoint.longitude(), indexPoint.latitude());
      currentAnimator = createLatLngAnimator(indexPoint, newPoint);
      currentAnimator.start();
      routeIndex++;
    }
  }

  private static class PointEvaluator implements TypeEvaluator<Point> {

    @Override
    public Point evaluate(float fraction, Point startValue, Point endValue) {
      return Point.fromLngLat(
          startValue.longitude() + ((endValue.longitude() - startValue.longitude()) * fraction),
          startValue.latitude() + ((endValue.latitude() - startValue.latitude()) * fraction)
      );
    }
  }

  private Animator createLatLngAnimator(Point currentPosition, Point targetPosition) {
    ValueAnimator latLngAnimator = ValueAnimator.ofObject(new PointEvaluator(), currentPosition, targetPosition);
    latLngAnimator.setDuration((long) TurfMeasurement.distance(currentPosition, targetPosition, "meters"));
    latLngAnimator.setInterpolator(new LinearInterpolator());
    latLngAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        animate();
      }
    });
    latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        Point point = (Point) animation.getAnimatedValue();
        pointSource.setGeoJson(point);
        markerLinePointList.add(point);
        lineSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(markerLinePointList)));
      }
    });

    return latLngAnimator;
  }

  /**
   * Make a request to the Mapbox Directions API. Once successful, pass the route to the
   * route layer.
   *
   * @param origin      the starting point of the route
   * @param destination the desired finish point of the route
   */
  private void getRoute(final Point origin, final Point destination) {
    MapboxDirections client = MapboxDirections.builder()
        .origin(origin)
        .destination(destination)
        .overview(DirectionsCriteria.OVERVIEW_FULL)
        .profile(DirectionsCriteria.PROFILE_WALKING)
        .accessToken(getString(R.string.access_token))
        .build();

    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        System.out.println(call.request().url().toString());

        // You can get the generic HTTP info about the response
        Timber.d("Response code: %s", response.code());
        if (response.body() == null) {
          Timber.e("No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().routes().size() < 1) {
          Timber.e("No routes found");
          return;
        }

        // Get the directions route
        DirectionsRoute currentRoute = response.body().routes().get(0);
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
              new LatLngBounds.Builder()
                .include(new LatLng(origin.latitude(), origin.longitude()))
                .include(new LatLng(destination.latitude(), destination.longitude()))
                .build(), 50), 5000);

            initData(style,FeatureCollection.fromFeature(
              Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
          }
        });
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Timber.e("Error: %s", throwable.getMessage());
        Toast.makeText(MovingIconWithTrailingLineActivity.this, "Error: " + throwable.getMessage(),
            Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Add various sources to the map.
   */
  private void initSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
    loadedMapStyle.addSource(pointSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection));
    loadedMapStyle.addSource(lineSource = new GeoJsonSource(LINE_SOURCE_ID));
  }

  /**
   * Add the marker icon SymbolLayer.
   */
  private void initSymbolLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage("moving-red-marker", BitmapFactory.decodeResource(
        getResources(), R.drawable.pink_dot));
    loadedMapStyle.addLayer(new SymbolLayer("symbol-layer-id", DOT_SOURCE_ID).withProperties(
        iconImage("moving-red-marker"),
        iconSize(1f),
        iconOffset(new Float[] {5f, 0f}),
        iconIgnorePlacement(true),
        iconAllowOverlap(true)
    ));
  }

  /**
   * Add the LineLayer for the marker icon's travel route. Adding it under the "road-label" layer, so that the
   * this LineLayer doesn't block the street name.
   */
  private void initDotLinePath(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addLayerBelow(new LineLayer("line-layer-id", LINE_SOURCE_ID).withProperties(
        lineColor(Color.parseColor("#F13C6E")),
        lineCap(Property.LINE_CAP_ROUND),
        lineJoin(Property.LINE_JOIN_ROUND),
        lineWidth(4f)), "road-label");
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
    if (currentAnimator != null) {
      currentAnimator.cancel();
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}

