package com.mapbox.mapboxandroiddemo.examples.javaservices;
// #-code-snippet: optimization-activity full-java
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;


/**
 * Use Mapbox Java Services to request and compare normal directions with time optimized directions.
 */
public class OptimizationActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private DirectionsRoute optimizedRoute;
  private MapboxOptimization optimizedClient;
  private Polyline optimizedPolyline;
  private List<Point> stops;
  private Point origin;

  private static final String FIRST = "first";
  private static final String ANY = "any";
  private static final String TEAL_COLOR = "#23D2BE";
  private static final int POLYLINE_WIDTH = 5;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_optimization);

    stops = new ArrayList<>();

    // Add the origin Point to the list
    addFirstStopToStopsList();

    // Setup the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    // Add origin and destination to the mapboxMap
    mapboxMap.addMarker(new MarkerOptions()
      .position(new LatLng(origin.latitude(), origin.longitude()))
      .title(getString(R.string.origin)));
    Toast.makeText(OptimizationActivity.this, R.string.click_instructions, Toast.LENGTH_SHORT).show();
    mapboxMap.addOnMapClickListener(this);
    mapboxMap.addOnMapLongClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    // Optimization API is limited to 12 coordinate sets
    if (alreadyTwelveMarkersOnMap()) {
      Toast.makeText(OptimizationActivity.this, R.string.only_twelve_stops_allowed, Toast.LENGTH_LONG).show();
    } else {
      addDestinationMarker(point);
      addPointToStopsList(point);
      getOptimizedRoute(stops);
    }
  }

  @Override
  public void onMapLongClick(@NonNull LatLng point) {
    mapboxMap.clear();
    stops.clear();
    addFirstStopToStopsList();
  }

  private boolean alreadyTwelveMarkersOnMap() {
    if (stops.size() == 12) {
      return true;
    } else {
      return false;
    }
  }

  private void addDestinationMarker(LatLng point) {
    mapboxMap.addMarker(new MarkerOptions()
      .position(new LatLng(point.getLatitude(), point.getLongitude()))
      .title(getString(R.string.destination)));
  }

  private void addPointToStopsList(LatLng point) {
    stops.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
  }

  private void addFirstStopToStopsList() {
    // Set first stop
    origin = Point.fromLngLat(30.335098600000038, 59.9342802);
    stops.add(origin);
  }

  private void getOptimizedRoute(List<Point> coordinates) {
    optimizedClient = MapboxOptimization.builder()
      .source(FIRST)
      .destination(ANY)
      .coordinates(coordinates)
      .overview(DirectionsCriteria.OVERVIEW_FULL)
      .profile(DirectionsCriteria.PROFILE_DRIVING)
      .accessToken(Mapbox.getAccessToken())
      .build();

    optimizedClient.enqueueCall(new Callback<OptimizationResponse>() {
      @Override
      public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
        if (!response.isSuccessful()) {
          Log.d("DirectionsActivity", getString(R.string.no_success));
          Toast.makeText(OptimizationActivity.this, R.string.no_success, Toast.LENGTH_SHORT).show();
          return;
        } else {
          if (response.body().trips().isEmpty()) {
            Log.d("DirectionsActivity", getString(R.string.successful_but_no_routes) + " size = "
              + response.body().trips().size());
            Toast.makeText(OptimizationActivity.this, R.string.successful_but_no_routes,
              Toast.LENGTH_SHORT).show();
            return;
          }
        }

        // Get most optimized route from API response
        optimizedRoute = response.body().trips().get(0);
        drawOptimizedRoute(optimizedRoute);
      }

      @Override
      public void onFailure(Call<OptimizationResponse> call, Throwable throwable) {
        Log.d("DirectionsActivity", "Error: " + throwable.getMessage());
      }
    });
  }

  private void drawOptimizedRoute(DirectionsRoute route) {
    // Remove old polyline
    if (optimizedPolyline != null) {
      mapboxMap.removePolyline(optimizedPolyline);
    }
    // Draw points on MapView
    LatLng[] pointsToDraw = convertLineStringToLatLng(route);
    optimizedPolyline = mapboxMap.addPolyline(new PolylineOptions()
      .add(pointsToDraw)
      .color(Color.parseColor(TEAL_COLOR))
      .width(POLYLINE_WIDTH));
  }

  private LatLng[] convertLineStringToLatLng(DirectionsRoute route) {
    // Convert LineString coordinates into LatLng[]
    LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
    List<Point> coordinates = lineString.coordinates();
    LatLng[] points = new LatLng[coordinates.size()];
    for (int i = 0; i < coordinates.size(); i++) {
      points[i] = new LatLng(
        coordinates.get(i).latitude(),
        coordinates.get(i).longitude());
    }
    return points;
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
// #-end-code-snippet: optimization-activity full-java