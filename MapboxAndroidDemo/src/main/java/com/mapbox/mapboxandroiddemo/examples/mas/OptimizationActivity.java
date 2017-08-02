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
* Use Mapbox Android Services to request and compare normal directions with time optimized directions
 */
public class OptimizationActivity extends AppCompatActivity {
  private static final String TAG = "DirectionsActivity";

  private MapView mapView;
  private MapboxMap map;
  private DirectionsRoute optimizedRoute;
  private MapboxOptimizedTrips optimizedClient;
  private Polyline optimizedPolyline;
  private List<Position> stops;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_mas_optimization);

    //Set up stop list
    stops = new ArrayList<Position>();

    // Set First Stop
    final Position origin = Position.fromCoordinates(-122.408818, 37.784015);
    stops.add(origin);

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
          .title("1"));

        map.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
          @Override
          public void onMapClick(@NonNull LatLng point) {
            if (stops.size() == 12) {
              Toast.makeText(OptimizationActivity.this, "Only 12 stops allowed on route", Toast.LENGTH_LONG).show();
            } else {
              map.addMarker(new MarkerOptions()
                .position(new LatLng(point.getLatitude(), point.getLongitude()))
                .title("1"));

              stops.add(Position.fromCoordinates(point.getLongitude(), point.getLatitude()));

              getOptimized(stops);
            }
          }
        });
      }
    });
  }

  private void getOptimized(List<Position> coordinates) {

    optimizedClient = new MapboxOptimizedTrips.Builder()
      .setSource("first")
      .setDestination("any")
      .setCoordinates(coordinates)
      .setOverview(DirectionsCriteria.OVERVIEW_FULL)
      .setProfile(DirectionsCriteria.PROFILE_DRIVING)
      .setAccessToken(Mapbox.getAccessToken())
      .build();

    optimizedClient.enqueueCall(new Callback<OptimizedTripsResponse>() {
      @Override
      public void onResponse(Call<OptimizedTripsResponse> call, Response<OptimizedTripsResponse> response) {
        System.out.println(call.request().url().toString());

        // You can get the generic HTTP info about the response
        Log.d(TAG, "Response code: " + response.body().getTrips());
        Log.d(TAG, "response: " + call.request().url().toString());

        if (response.body() == null) {
          Log.e(TAG, "No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().getTrips().size() < 1) {
          Log.e(TAG, "No routes found" + response.body().getTrips().size());
          return;
        }

        // Print some info about the route
        optimizedRoute = response.body().getTrips().get(0);

        // Draw the route on the map
        drawOptimized(optimizedRoute);
      }

      @Override
      public void onFailure(Call<OptimizedTripsResponse> call, Throwable throwable) {
        Log.e(TAG, "Error: " + throwable.getMessage());
      }
    });
  }

  private void drawOptimized(DirectionsRoute route) {
    //remove old polyline
    if (optimizedPolyline != null) {
      map.removePolyline(optimizedPolyline);
    }

    // Convert LineString coordinates into LatLng[]
    LineString lineString = LineString.fromPolyline(route.getGeometry(), PRECISION_6);
    List<Position> coordinates = lineString.getCoordinates();
    LatLng[] points = new LatLng[coordinates.size()];
    for (int i = 0; i < coordinates.size(); i++) {
      points[i] = new LatLng(
        coordinates.get(i).getLatitude(),
        coordinates.get(i).getLongitude());
    }

    // Draw Points on MapView
    optimizedPolyline = map.addPolyline(new PolylineOptions()
      .add(points)
      .color(Color.parseColor("#23D2BE"))
      .width(5));
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
