package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineTranslate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Drag the map to automatically draw a directions route to wherever the map is centered.
 */
public class DashedLineDirectionsPickerActivity extends AppCompatActivity
  implements OnMapReadyCallback, MapboxMap.OnCameraIdleListener {

  private static final String DIRECTIONS_LAYER_ID = "DIRECTIONS_LAYER_ID";
  private static final String LAYER_BELOW_ID = "road-label-small";
  private static final String SOURCE_ID = "SOURCE_ID";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private FeatureCollection dashedLineDirectionsFeatureCollection;
  private Point directionsOriginPoint = Point.fromLngLat(24.9383791, 60.1698556);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_place_picker_dashed_directions_route);

    // Initialize the mapboxMap view
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @SuppressWarnings( {"MissingPermission"})
  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    DashedLineDirectionsPickerActivity.this.mapboxMap = mapboxMap;

    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Add a marker on the map's center/"target" for the place picker UI
        ImageView hoveringMarker = new ImageView(DashedLineDirectionsPickerActivity.this);
        hoveringMarker.setImageResource(R.drawable.red_marker);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        hoveringMarker.setLayoutParams(params);
        mapView.addView(hoveringMarker);

        // Add the layer for the dashed directions route line
        initDottedLineSourceAndLayer(style);

        // Add the camera idle listener
        mapboxMap.addOnCameraIdleListener(DashedLineDirectionsPickerActivity.this);

        Toast.makeText(
          DashedLineDirectionsPickerActivity.this,
          getString(R.string.move_map_around_instruction),
          Toast.LENGTH_LONG
        ).show();
      }
    });
  }

  @Override
  public void onCameraIdle() {
    if (mapboxMap != null) {
      Point destinationPoint = Point.fromLngLat(
        mapboxMap.getCameraPosition().target.getLongitude(),
        mapboxMap.getCameraPosition().target.getLatitude());
      getRoute(destinationPoint);
    }
  }

  /**
   * Set up a GeoJsonSource and LineLayer in order to show the directions route from the device location
   * to the place picker location
   */
  private void initDottedLineSourceAndLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource(new GeoJsonSource(SOURCE_ID));
    loadedMapStyle.addLayerBelow(
      new LineLayer(
          DIRECTIONS_LAYER_ID, SOURCE_ID).withProperties(
        lineWidth(4.5f),
        lineColor(Color.BLACK),
        lineTranslate(new Float[] {0f, 4f}),
        lineDasharray(new Float[] {1.2f, 1.2f})
      ), LAYER_BELOW_ID);
  }

  /**
   * Make a call to the Mapbox Directions API to get the route from the device location to the
   * place picker location
   *
   * @param destination the location chosen by moving the map to the desired destination location
   */
  @SuppressWarnings( {"MissingPermission"})
  private void getRoute(Point destination) {
    MapboxDirections client = MapboxDirections.builder()
      .origin(directionsOriginPoint)
      .destination(destination)
      .overview(DirectionsCriteria.OVERVIEW_FULL)
      .profile(DirectionsCriteria.PROFILE_WALKING)
      .accessToken(getString(R.string.access_token))
      .build();
    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        if (response.body() == null) {
          Timber.d( "No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().routes().size() < 1) {
          Timber.d( "No routes found");
          return;
        }
        drawNavigationPolylineRoute(response.body().routes().get(0));
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Timber.d("Error: %s", throwable.getMessage());
        if (!throwable.getMessage().equals("Coordinate is invalid: 0,0")) {
          Toast.makeText(DashedLineDirectionsPickerActivity.this,
            "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  /**
   * Update the GeoJson data that's part of the LineLayer.
   *
   * @param route The route to be drawn in the map's LineLayer that was set up above.
   */
  private void drawNavigationPolylineRoute(final DirectionsRoute route) {
    if (mapboxMap != null) {
      mapboxMap.getStyle(new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          List<Feature> directionsRouteFeatureList = new ArrayList<>();
          LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
          List<Point> coordinates = lineString.coordinates();
          for (int i = 0; i < coordinates.size(); i++) {
            directionsRouteFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(coordinates)));
          }
          dashedLineDirectionsFeatureCollection = FeatureCollection.fromFeatures(directionsRouteFeatureList);
          GeoJsonSource source = style.getSourceAs(SOURCE_ID);
          if (source != null) {
            source.setGeoJson(dashedLineDirectionsFeatureCollection);
          }
        }
      });
    }
  }

  @Override
  @SuppressWarnings( {"MissingPermission"})
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
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
