package com.mapbox.mapboxandroiddemo.examples.labs;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

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
  private static final Point ORIGIN_COFFEE_SHOP = Point.fromLngLat(38.7508, 9.0309);
  private static final Point DESTINATION_AIRPORT = Point.fromLngLat(38.795902, 8.984467);
  private List<Point> routeCoordinateList;
  private List<Point> markerLinePointList;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private DirectionsRoute currentRoute;
  private Handler handler;
  private Runnable runnable;
  private GeoJsonSource dotGeoJsonSource;
  private ValueAnimator markerIconAnimator;
  private LatLng markerIconCurrentLocation;
  private int indexCounterForMovingDotAndLine = 0;

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

            markerLinePointList = new ArrayList<>();

            // Use the Mapbox Directions API to get a directions route
            getRoute(style, ORIGIN_COFFEE_SHOP, DESTINATION_AIRPORT);
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
  private void initData(@NonNull FeatureCollection featureCollection) {
    LineString lineString = (LineString) featureCollection.features().get(0).geometry();
    routeCoordinateList = lineString.coordinates();
    if (mapboxMap != null) {
      Style style = mapboxMap.getStyle();
      if (style != null) {
        initSources(style, featureCollection);
        initSymbolLayer(style);
        initDotLinePath(style);
        initRunnable();
      }
    }
  }

  /**
   * Set up the repeat logic for moving the icon along the route.
   */
  private void initRunnable() {
    // Animating the marker requires the use of both the ValueAnimator and a handler.
    // The ValueAnimator is used to move the marker between the GeoJSON points, this is
    // done linearly. The handler is used to move the marker along the GeoJSON points.
    handler = new Handler();
    runnable = new Runnable() {
      @Override
      public void run() {
        // Check if we are at the end of the points list, if so we want to stop using
        // the handler.
        if ((routeCoordinateList.size() - 1 > indexCounterForMovingDotAndLine)) {

          Point nextLocationPoint = routeCoordinateList.get(indexCounterForMovingDotAndLine + 1);

          if (markerIconAnimator != null && markerIconAnimator.isStarted()) {
            markerIconCurrentLocation = (LatLng) markerIconAnimator.getAnimatedValue();
            markerIconAnimator.cancel();
          }

          if (latLngEvaluator != null) {
            markerIconAnimator = ObjectAnimator
              .ofObject(latLngEvaluator, indexCounterForMovingDotAndLine == 0 ? new LatLng(
                ORIGIN_COFFEE_SHOP.latitude(), ORIGIN_COFFEE_SHOP.longitude())
                  : markerIconCurrentLocation,
                new LatLng(nextLocationPoint.latitude(), nextLocationPoint.longitude()))
              .setDuration(150);
            markerIconAnimator.setInterpolator(new LinearInterpolator());
            markerIconAnimator.addUpdateListener(animatorUpdateListener);
            markerIconAnimator.start();

            // Keeping the current point indexCounterForMovingDotAndLine we are on.
            indexCounterForMovingDotAndLine++;

            // Once we finish we need to repeat the entire process by executing the
            // handler again once the ValueAnimator is finished.
            handler.postDelayed(this, 150);
          }
        }
      }
    };
    handler.post(runnable);
  }

  /**
   * Make a request to the Mapbox Directions API. Once successful, pass the route to the
   * route layer.
   *
   * @param origin      the starting point of the route
   * @param destination the desired finish point of the route
   */
  private void getRoute(@NonNull final Style style, final Point origin, final Point destination) {

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
        currentRoute = response.body().routes().get(0);

        if (style.isFullyLoaded()) {
          mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
            new LatLngBounds.Builder()
              .include(new LatLng(origin.latitude(), origin.longitude()))
              .include(new LatLng(destination.latitude(), destination.longitude()))
              .build(), 50), 5000);

          initData(FeatureCollection.fromFeature(
            Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
        }
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Timber.e("Error: " + throwable.getMessage());
        Toast.makeText(MovingIconWithTrailingLineActivity.this, "Error: " + throwable.getMessage(),
          Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Listener interface for when the ValueAnimator provides an updated value
   */
  private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
    new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
        Point newPoint = Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude());
        if (dotGeoJsonSource != null) {
          dotGeoJsonSource.setGeoJson(newPoint);
          if (mapboxMap.getStyle() != null) {
            GeoJsonSource lineSource = mapboxMap.getStyle().getSourceAs(LINE_SOURCE_ID);
            markerLinePointList.add(newPoint);
            if (lineSource != null) {
              lineSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(markerLinePointList)));
            }
          }
        }
      }
    };

  /**
   * Add various sources to the map.
   */
  private void initSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
    dotGeoJsonSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection);
    loadedMapStyle.addSource(dotGeoJsonSource);
    loadedMapStyle.addSource(new GeoJsonSource(LINE_SOURCE_ID));
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
    // When the activity is resumed we restart the marker animating.
    if (handler != null && runnable != null) {
      handler.post(runnable);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    // Check if the marker is currently animating and if so, we pause the animation so we aren't
    // using resources when the activities not in view.
    if (handler != null && runnable != null) {
      handler.removeCallbacksAndMessages(null);
    }
    if (markerIconAnimator != null) {
      markerIconAnimator.end();
    }
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  /**
   * Method is used to interpolate the SymbolLayer icon animation.
   */
  private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

    private final LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude()
        + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude()
        + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  };
}

