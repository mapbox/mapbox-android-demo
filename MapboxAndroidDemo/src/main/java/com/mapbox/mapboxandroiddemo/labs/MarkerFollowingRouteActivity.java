package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Use a map matched GeoJSON route to show a marker travels along the route at consistent speed.
 */
public class MarkerFollowingRouteActivity extends AppCompatActivity {

  private static final String TAG = "MarkerFollowingRoute";

  private MapView mapView;
  private MapboxMap map;
  private Handler handler;
  private Runnable runnable;

  private static int count = 0;
  private long distance;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_marker_following_route);

    // Initialize the map view
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        // Load and Draw the GeoJSON. The marker animation is also handled here.
        new DrawGeoJson().execute();

      }
    });
  } // End onCreate

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
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
    // Check if the marker is currently animating and if so, we pause the animation so we aren't
    // using resources when the activities not in view.
    if (handler != null && runnable != null) {
      handler.removeCallbacks(runnable);
    }
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

  // We want to load in the GeoJSON file asynchronous so the UI thread isn't handling the file
  // loading. The GeoJSON file we are using is stored in the assets folder, you could also get
  // this information from the Mapbox map matching API during runtime.
  private class DrawGeoJson extends AsyncTask<Void, Void, List<LatLng>> {
    @Override
    protected List<LatLng> doInBackground(Void... voids) {

      // Store the route LatLng points in a list so we can query them.
      List<LatLng> points = new ArrayList<>();

      try {
        // Load GeoJSON file from the assets folder.
        InputStream inputStream = getAssets().open("matched_route.geojson");
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }

        inputStream.close();

        // Parse JSON
        JSONObject json = new JSONObject(sb.toString());
        JSONArray features = json.getJSONArray("features");
        JSONObject feature = features.getJSONObject(0);
        JSONObject geometry = feature.getJSONObject("geometry");
        if (geometry != null) {
          String type = geometry.getString("type");

          // Our GeoJSON only has one feature: a line string.
          if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

            // Get the Coordinates and add them to the points list we created above.
            JSONArray coords = geometry.getJSONArray("coordinates");
            for (int lc = 0; lc < coords.length(); lc++) {
              JSONArray coord = coords.getJSONArray(lc);
              LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
              points.add(latLng);
            }
          }
        }
      } catch (Exception exception) {
        // If an error occurs loading in the GeoJSON file or adding the points to the list,
        // we log the error.
        Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
      }

      // Lastly we return the list containing the route points.
      return points;
    } // End doInBackground

    @Override
    protected void onPostExecute(final List<LatLng> points) {
      super.onPostExecute(points);

      // Make sure our list isn't empty.
      if (points.size() > 0) {
        LatLng[] pointsArray = points.toArray(new LatLng[points.size()]);

        // Draw a polyline showing the route the marker will be taking.
        map.addPolyline(new PolylineOptions()
            .add(pointsArray)
            .color(Color.parseColor("#F13C6E"))
            .width(4));

        // We are using a custom marker icon.
        Icon icon = IconFactory.getInstance(MarkerFollowingRouteActivity.this).fromResource(R.drawable.pink_dot);

        // Using a view marker, we place it at the first point in the points list.
        final Marker marker = map.addMarker(new MarkerViewOptions()
            .position(points.get(count))
            .icon(icon)
            .anchor(0.5f, 0.5f)
            .flat(true));

        // Animating the marker requires the use of both the ValueAnimator and a handler.
        // The ValueAnimator is used to move the marker between the GeoJSON points, this is
        // done linearly. The handler is used to move the marker along the GeoJSON points.
        handler = new Handler();
        runnable = new Runnable() {
          @Override
          public void run() {

            // Check if we are at the end of the points list, if so we want to stop using
            // the handler.
            if ((points.size() - 1) > count) {

              // Calculating the distance is done between the current point and next.
              // This gives us the duration we will need to execute the ValueAnimator.
              // Multiplying by ten is done to slow down the marker speed. Adjusting
              // this value will result in the marker traversing faster or slower along
              // the line
              distance = (long) marker.getPosition().distanceTo(points.get(count)) * 10;

              // animate the marker from it's current position to the next point in the
              // points list.
              ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position",
                  new LatLngEvaluator(), marker.getPosition(), points.get(count));
              markerAnimator.setDuration(distance);
              markerAnimator.setInterpolator(new LinearInterpolator());
              markerAnimator.start();

              // This line will make sure the marker appears when it is being animated
              // and starts outside the current user view. Without this, the user must
              // intentionally execute a gesture before the view marker reappears on
              // the map.
              map.getMarkerViewManager().update();

              // Keeping the current point count we are on.
              count++;

              // Once we finish we need to repeat the entire process by executing the
              // handler again once the ValueAnimator is finished.
              handler.postDelayed(this, distance);
            }
          }
        };
        handler.post(runnable);
      }
    } // End onPostExecute
  } // End DrawGeoJson

  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
    // Method is used to interpolate the marker animation.

    private LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude()
          + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude()
          + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  }
}