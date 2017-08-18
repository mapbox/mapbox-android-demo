package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.optimizedtrips.v1.MapboxOptimizedTrips;
import com.mapbox.services.api.optimizedtrips.v1.models.OptimizedTripsResponse;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.services.Constants.PRECISION_6;

/**
 * Use Mapbox Android Services to request and compare normal directions with time optimized directions.
 */
public class OptimizationActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private DirectionsRoute optimizedRoute;
  private MapboxOptimizedTrips optimizedClient;
  private Polyline optimizedPolyline;
  private List<Position> stops;
  private Position origin;

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
    setContentView(R.layout.activity_mas_optimization);

    // Initialize list of Position objects and add the origin Position to the list
    initializeListOfStops();

    // Setup the MapView
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        // Add origin and destination to the map
        mapboxMap.addMarker(new MarkerOptions()
          .position(new LatLng(origin.getLatitude(), origin.getLongitude()))
          .title(getString(R.string.origin)));

        map.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
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
        });
        Toast.makeText(OptimizationActivity.this, R.string.click_instructions, Toast.LENGTH_SHORT).show();
      }
    });
  }

  private boolean alreadyTwelveMarkersOnMap() {
    if (stops.size() == 12) {
      return true;
    } else {
      return false;
    }
  }

  private void addDestinationMarker(LatLng point) {
    map.addMarker(new MarkerOptions()
      .position(new LatLng(point.getLatitude(), point.getLongitude()))
      .title(getString(R.string.destination)));
  }

  private void addPointToStopsList(LatLng point) {
    stops.add(Position.fromCoordinates(point.getLongitude(), point.getLatitude()));
  }

  private void initializeListOfStops() {
    // Set up stop list
    stops = new ArrayList<>();
    // Set first stop
    origin = Position.fromCoordinates(30.335098600000038, 59.9342802);
    stops.add(origin);
  }

  private void getOptimizedRoute(List<Position> coordinates) {
    optimizedClient = new MapboxOptimizedTrips.Builder()
      .setSource(FIRST)
      .setDestination(ANY)
      .setCoordinates(coordinates)
      .setOverview(DirectionsCriteria.OVERVIEW_FULL)
      .setProfile(DirectionsCriteria.PROFILE_DRIVING)
      .setAccessToken(Mapbox.getAccessToken())
      .build();

    optimizedClient.enqueueCall(new Callback<OptimizedTripsResponse>() {
      @Override
      public void onResponse(Call<OptimizedTripsResponse> call, Response<OptimizedTripsResponse> response) {
        if (!response.isSuccessful()) {
          Log.d("DirectionsActivity", getString(R.string.no_success));
          Toast.makeText(OptimizationActivity.this, R.string.no_success, Toast.LENGTH_SHORT).show();
          return;
        } else {
          if (response.body().getTrips().isEmpty()) {
            Log.d("DirectionsActivity", getString(R.string.successful_but_no_routes) + " size = "
              + response.body().getTrips().size());
            Toast.makeText(OptimizationActivity.this, R.string.successful_but_no_routes,
              Toast.LENGTH_SHORT).show();
            return;
          }
        }

        // Get most optimized route from API response
        optimizedRoute = response.body().getTrips().get(0);
        drawOptimizedRoute(optimizedRoute);
      }

      @Override
      public void onFailure(Call<OptimizedTripsResponse> call, Throwable throwable) {
        Log.d("DirectionsActivity", "Error: " + throwable.getMessage());
      }
    });
  }

  private void drawOptimizedRoute(DirectionsRoute route) {
    // Remove old polyline
    if (optimizedPolyline != null) {
      map.removePolyline(optimizedPolyline);
    }
    // Draw points on MapView
    LatLng[] pointsToDraw = convertLineStringToLatLng(route);
    optimizedPolyline = map.addPolyline(new PolylineOptions()
      .add(pointsToDraw)
      .color(Color.parseColor(TEAL_COLOR))
      .width(POLYLINE_WIDTH));
  }

  private LatLng[] convertLineStringToLatLng(DirectionsRoute route) {
    // Convert LineString coordinates into LatLng[]
    LineString lineString = LineString.fromPolyline(route.getGeometry(), PRECISION_6);
    List<Position> coordinates = lineString.getCoordinates();
    LatLng[] points = new LatLng[coordinates.size()];
    for (int i = 0; i < coordinates.size(); i++) {
      points[i] = new LatLng(
        coordinates.get(i).getLatitude(),
        coordinates.get(i).getLongitude());
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
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
