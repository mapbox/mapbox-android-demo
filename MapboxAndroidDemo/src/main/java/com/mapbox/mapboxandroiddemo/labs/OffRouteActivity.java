package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.turf.TurfException;
import com.mapbox.services.commons.turf.TurfMeasurement;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.navigation.v5.RouteUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OffRouteActivity extends AppCompatActivity {

  private static final String TAG = "OffRouteDetection";

  // Map variables
  private MapView mapView;
  private MapboxMap map;
  private MarkerView car;
  private Marker destinationMarker;
  private Polyline routePolyline;
  private Position destination;

  // Direction variables
  private DirectionsRoute currentRoute;
  private List<LatLng> routePoints;
  private List<LatLng> newRoutePoints;
  private int count = 0;
  private long distance;
  private Handler handler;
  private Runnable runnable;
  private boolean routeFinished = false;
  private boolean reRoute = false;
  private RouteUtils routeUtils;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lab_off_route);

    mapView = (MapView) findViewById(R.id.mapview);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        Toast.makeText(OffRouteActivity.this, "Press map to add destination", Toast.LENGTH_LONG).show();

        // origin used for starting point of car.
        Position origin = Position.fromCoordinates(2.35166, 48.84659);

        // Use the default 100 meter tolerance for off route.
        routeUtils = new RouteUtils();

        addCar(new LatLng(origin.getLatitude(), origin.getLongitude()));

        map.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
          @Override
          public void onMapClick(@NonNull LatLng point) {

            if (destinationMarker != null) {
              map.removeMarker(destinationMarker);
              reRoute = true;
            }
            destinationMarker = map.addMarker(new MarkerViewOptions().position(point));

            destination = Position.fromCoordinates(point.getLongitude(), point.getLatitude());

            try {
              getRoute(
                  Position.fromCoordinates(car.getPosition().getLongitude(), car.getPosition().getLatitude()),
                  Position.fromCoordinates(point.getLongitude(), point.getLatitude())
              );
            } catch (ServicesException e) {
              e.printStackTrace();
              Log.e(TAG, "onMapReady: " + e.getMessage());
            }

          }
        });

      }// End onMapReady
    });
  }// End onCreate

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
    if (handler != null && runnable != null) {
      handler.post(runnable);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
    stopSimulation();
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_off_route, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_default:
        routeUtils = new RouteUtils(0.1);
        break;
      case R.id.action_1:
        routeUtils = new RouteUtils(0.05);
        break;
      case R.id.action_2:
        routeUtils = new RouteUtils(0.5);
        break;
      case R.id.action_3:
        routeUtils = new RouteUtils(1);
        break;
    }


    return super.onOptionsItemSelected(item);
  }

  private void addCar(LatLng position) {
    // Using a custom car icon for marker.
    IconFactory iconFactory = IconFactory.getInstance(OffRouteActivity.this);
    Drawable iconDrawable = ContextCompat.getDrawable(OffRouteActivity.this, R.drawable.ic_car_top);
    Icon icon = iconFactory.fromDrawable(iconDrawable);

    // Add the car marker to the map.
    car = map.addMarker(new MarkerViewOptions()
        .position(position)
        .anchor(0.5f, 0.5f)
        .flat(true)
        .icon(icon)
    );
  }

  private void getRoute(Position origin, Position destination) throws ServicesException {
    ArrayList<Position> positions = new ArrayList<>();
    positions.add(origin);
    positions.add(destination);

    MapboxDirections client = new MapboxDirections.Builder()
        .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
        .setCoordinates(positions)
        .setProfile(DirectionsCriteria.PROFILE_DRIVING)
        .setSteps(true)
        .setOverview(DirectionsCriteria.OVERVIEW_FULL)
        .build();

    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        // You can get generic HTTP info about the response
        Log.d(TAG, "Response code: " + response.code());
        if (response.body() == null) {
          Log.e(TAG, "No routes found, make sure you set the right user and access token.");
          return;
        }

        // Print some info about the route
        currentRoute = response.body().getRoutes().get(0);
        Log.d(TAG, "Distance: " + currentRoute.getDistance());

        // Draw the route on the map
        drawRoute(currentRoute);
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable t) {
        Log.e(TAG, "Error: " + t.getMessage());
      }
    });
  }


  private void drawRoute(DirectionsRoute route) {

    // Convert the route to latlng values and add to list.
    LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
    List<Position> coordinates = lineString.getCoordinates();
    newRoutePoints = new ArrayList<>();
    for (int j = 0; j < coordinates.size(); j++) {
      newRoutePoints.add(new LatLng(
          coordinates.get(j).getLatitude(),
          coordinates.get(j).getLongitude()));
    }

    // Remove the route line if it exist on map.
    if (routePolyline != null) {
      map.removePolyline(routePolyline);
    }

    // Draw Points on map
    routePolyline = map.addPolyline(new PolylineOptions()
        .addAll(newRoutePoints)
        .color(Color.parseColor("#56b881"))
        .width(5));

    // If car's already at the routes end, we need to start our runnable back up.
    if (routeFinished) {
      routeFinished = false;
      routePoints = newRoutePoints;
      count = 0;
      handler.post(runnable);
    }

    if (!reRoute) {
      routePoints = newRoutePoints;
      startSimulation();
    }
  }

  private void checkIfOffRoute() throws ServicesException, TurfException {

    Position carCurrentPosition = Position.fromCoordinates(
        car.getPosition().getLongitude(),
        car.getPosition().getLatitude()
    );

    // TODO currently making the assumption that only 1 leg in route exist.
    if (routeUtils.isOffRoute(carCurrentPosition, currentRoute.getLegs().get(0))) {

      // Display message to user and stop simulation.
      Toast.makeText(OffRouteActivity.this, "Off route", Toast.LENGTH_LONG).show();
      stopSimulation();

      // Reset our variables
      reRoute = false;
      count = 0;

      // Get the route from car position to destination and begin simulating.
      getRoute(carCurrentPosition, destination);

    }
  } // End checkIfOffRoute

  private void startSimulation() {
    // Typically you wouldn't need this method but since we want to simulate movement along a
    // route, we use a handler to animate the car (similar behaviour to driving).
    handler = new Handler();
    runnable = new Runnable() {
      @Override
      public void run() {

        // Check if we are at the end of the routePoints list, if so we want to stop using
        // the handler.
        if ((routePoints.size() - 1) > count) {

          // Calculating the distance is done between the current point and next.
          // This gives us the duration we will need to execute the ValueAnimator.
          // Multiplying by ten is done to slow down the marker speed. Adjusting
          // this value will result in the marker traversing faster or slower along
          // the line
          distance = (long) car.getPosition().distanceTo(routePoints.get(count)) * 10;

          // animate the marker from it's current position to the next point in the
          // points list.
          ValueAnimator markerAnimator = ObjectAnimator.ofObject(car, "position",
              new LatLngEvaluator(), car.getPosition(), routePoints.get(count));
          markerAnimator.setDuration(distance);
          markerAnimator.setInterpolator(new LinearInterpolator());
          markerAnimator.start();

          // This line will make sure the marker appears when it is being animated
          // and starts outside the current user view. Without this, the user must
          // intentionally execute a gesture before the view marker reappears on
          // the map.
          map.getMarkerViewManager().scheduleViewMarkerInvalidation();

          // Rotate the car (marker) to the correct orientation.
          car.setRotation((float) computeHeading(car.getPosition(), routePoints.get(count)));

          // Check that the vehicles off route or not. If you aren't simulating the car,
          // and want to use this example in the real world, the checkingIfOffRoute method
          // should go in a locationListener.
          try {
            checkIfOffRoute();
          } catch (ServicesException | TurfException e) {
            e.printStackTrace();
            Log.e(TAG, "check if off route error: " + e.getMessage());
          }

          // Keeping the current point count we are on.
          count++;

          // Once we finish we need to repeat the entire process by executing the
          // handler again once the ValueAnimator is finished.
          handler.postDelayed(this, distance);
        } else {
          // Car's at the end of route so notify that we are finished.
          routeFinished = true;
        }
      }
    };
    handler.post(runnable);
  }// End startSimulation

  private void stopSimulation() {
    if (handler != null || runnable != null) {
      handler.removeCallbacks(runnable);
    }
  }

  public static double computeHeading(LatLng from, LatLng to) {
    // Compute bearing/heading using Turf and return the value.
    return TurfMeasurement.bearing(
        Position.fromCoordinates(from.getLongitude(), from.getLatitude()),
        Position.fromCoordinates(to.getLongitude(), to.getLatitude())
    );
  }

  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
    // Method is used to interpolate the marker animation.
    private LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude() + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude() + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  }
}