package com.mapbox.mapboxandroiddemo.examples.labs;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTranslate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Use a map matched GeoJSON route to show a marker travels along the route at consistent speed.
 */
public class MarkerFollowingRouteActivity extends AppCompatActivity {

  private static final String TAG = "MarkerFollowingRoute";

  private MapView mapView;
  private MapboxMap map;
  private Handler handler;
  private Runnable runnable;
  private List<Feature> featureList;

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
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            new LoadGeojson(MarkerFollowingRouteActivity.this).execute();
            initDotSource();
            initLineSource();
            initSymbolLayer();
            initDotLinePath();
            initRunnable();
          }
        });
      }
    });
  }

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
        if ((featureList.size() - 1) > count) {

          // Calculating the distance is done between the current point and next.
          // This gives us the duration we will need to execute the ValueAnimator.
          // Multiplying by ten is done to slow down the marker speed. Adjusting
          // this value will result in the marker traversing faster or slower along
          // the line
          distance = (long) marker.getPosition().distanceTo(featureList.get(count)) * 10;

          // animate the marker from it's current position to the next point in the
          // points list.
          ValueAnimator animator = ObjectAnimator.ofObject(marker, "position",
            new LatLngEvaluator(), marker.getPosition(), featureList.get(count));
          animator.setDuration(distance);
          animator.setInterpolator(new LinearInterpolator());
          animator.start();

          // This line will make sure the marker appears when it is being animated
          // and starts outside the current user view. Without this, the user must
          // intentionally execute a gesture before the view marker reappears on
          // the map.
          pinSymbolLayer.setProperties();


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

  private void initDotSource() {
    GeoJsonSource geoJsonSource = new GeoJsonSource("dot-source-id");
    map.getStyle().addSource(geoJsonSource);
  }

  private void initLineSource() {
    GeoJsonSource geoJsonSource = new GeoJsonSource("line-source-id");
    map.getStyle().addSource(geoJsonSource);
  }

  private void initSymbolLayer() {
    map.getStyle().addImage("moving-pink-dot", BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.pink_dot)));

    SymbolLayer symbolLayer = new SymbolLayer("symbol-layer-id", "dot-source-id");
    symbolLayer.withProperties(
      iconImage("moving-pink-dot"),
      iconSize(1.4f)
    );
    map.getStyle().addLayer(symbolLayer);
  }

  private void initDotLinePath() {
    LineLayer lineLayer = new LineLayer("line-layer-id", "line-source-id");
    lineLayer.withProperties(
      lineColor(Color.parseColor("#F13C6E")),
      lineWidth(4f)
    );
    map.getStyle().addLayer(lineLayer);
  }
/*
  *//**
   * Updates the display of data on the map after the FeatureCollection has been modified
   *//*
  private void refreshSource(List<Feature> featureList) {
    this.featureList = featureList;
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(featureList);
    GeoJsonSource source = map.getStyle().getSourceAs("source-id");
    if (source != null && featureCollection != null) {
      source.setGeoJson(featureCollection);
    }
  }*/

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

  /**
   * We want to load in the GeoJSON file asynchronous so the UI thread isn't handling the file
   *    loading. The GeoJSON file we are using is stored in the assets folder, you could also get
   *   this information from the Mapbox map matching API during runtime.
   */
  private class LoadGeojson extends AsyncTask<Void, Void, List<Feature>> {

    private final WeakReference<MarkerFollowingRouteActivity> activityRef;

    LoadGeojson(MarkerFollowingRouteActivity activity) {
      this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected List<Feature> doInBackground(Void... voids) {

      // Store the route Feature in a list so we can query them.
      List<Feature> featureList = new ArrayList<>();

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

          // Our GeoJSON only has one feature: a LineString
          if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

            // Get the Coordinates and add them to the featureList we created above.
            JSONArray coords = geometry.getJSONArray("coordinates");
            for (int lc = 0; lc < coords.length(); lc++) {
              JSONArray coord = coords.getJSONArray(lc);
              featureList.add(Feature.fromGeometry(Point.fromLngLat(coord.getDouble(0),
                coord.getDouble(1))));
            }
          }
        }
      } catch (Exception exception) {
        // If an error occurs loading in the GeoJSON file or adding the points to the list,
        // we log the error.
        Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
      }

      // Lastly we return the list containing the route Features.
      return featureList;
    }

    @Override
    protected void onPostExecute(final List<Feature> features) {
      super.onPostExecute(features);

      // Make sure our list isn't empty.
      if (features.size() > 0) {
        MarkerFollowingRouteActivity activity = activityRef.get();
        if (activity != null) {

          // Update the LineLayer with LineString Feature
          FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
          GeoJsonSource source = activity.map.getStyle().getSourceAs("line-source-id");
          if (source != null && featureCollection != null) {
            source.setGeoJson(featureCollection);
          }
        }
      }
    }
  }

  /**
   * Method is used to interpolate the SymbolLayer icon animation.
   */
  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
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
