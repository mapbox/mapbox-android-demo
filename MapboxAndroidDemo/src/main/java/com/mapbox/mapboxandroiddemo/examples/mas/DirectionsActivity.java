package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.services.Constants.PRECISION_6;

/**
 * Use Mapbox Android Services to request directions
 */
public class DirectionsActivity extends AppCompatActivity {

  private static final String TAG = "DirectionsActivity";

  private MapView mapView;
  private MapboxMap map;
  private DirectionsRoute currentRoute;
  private MapboxDirections client;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_mas_directions);

    // Alhambra landmark in Granada, Spain.
    final Position origin = Position.fromCoordinates(-3.588098, 37.176164);

    // Plaza del Triunfo in Granada, Spain.
    final Position destination = Position.fromCoordinates(-3.601845, 37.184080);


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
          .title(getString(R.string.directions_activity_marker_options_origin_title))
          .snippet(getString(R.string.directions_activity_marker_options_origin_snippet)));
        mapboxMap.addMarker(new MarkerOptions()
          .position(new LatLng(destination.getLatitude(), destination.getLongitude()))
          .title(getString(R.string.directions_activity_marker_options_destination_title))
          .snippet(getString(R.string.directions_activity_marker_options_destination_snippet)));

        // Get route from API
        getRoute(origin, destination);
      }
    });
  }

  private void getRoute(Position origin, Position destination) {

    client = new MapboxDirections.Builder()
      .setOrigin(origin)
      .setDestination(destination)
      .setOverview(DirectionsCriteria.OVERVIEW_FULL)
      .setProfile(DirectionsCriteria.PROFILE_CYCLING)
      .setAccessToken(getString(R.string.access_token))
      .build();

    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        System.out.println(call.request().url().toString());

        // You can get the generic HTTP info about the response
        Log.d(TAG, "Response code: " + response.code());
        if (response.body() == null) {
          Log.e(TAG, "No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().getRoutes().size() < 1) {
          Log.e(TAG, "No routes found");
          return;
        }

        // Print some info about the route
        currentRoute = response.body().getRoutes().get(0);
        Log.d(TAG, "Distance: " + currentRoute.getDistance());
        Toast.makeText(DirectionsActivity.this, String.format(getString(R.string.directions_activity_toast_message),
          currentRoute.getDistance()), Toast.LENGTH_SHORT).show();

        // Draw the route on the map
        drawRoute(currentRoute);
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Log.e(TAG, "Error: " + throwable.getMessage());
        Toast.makeText(DirectionsActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void drawRoute(DirectionsRoute route) {
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
    map.addPolyline(new PolylineOptions()
      .add(points)
      .color(Color.parseColor("#009688"))
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