package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Using the polylines utility, simplify a polyline at a
 * given tolerance to reduce the number of coordinates in that polyline.
 */
public class SimplifyPolylineActivity extends AppCompatActivity {

  private static final String TAG = "SimplifyLineActivity";

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_simplify_polyline);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            new DrawGeoJson().execute();
          }
        });
      }
    });
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private class DrawGeoJson extends AsyncTask<Void, Void, List<Point>> {
    @Override
    protected List<Point> doInBackground(Void... voids) {

      List<Point> points = new ArrayList<>();

      try {
        // Load GeoJSON file
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

          // Our GeoJSON only has one feature: a line string
          if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

            // Get the Coordinates
            JSONArray coords = geometry.getJSONArray("coordinates");
            for (int lc = 0; lc < coords.length(); lc++) {
              JSONArray coord = coords.getJSONArray(lc);
              Point position = Point.fromLngLat(coord.getDouble(0), coord.getDouble(1));
              points.add(position);
            }
          }
        }
      } catch (Exception exception) {
        Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
      }
      return points;
    }

    @Override
    protected void onPostExecute(List<Point> points) {
      super.onPostExecute(points);
      drawBeforeSimplify(points);
      drawSimplify(points);
      Log.d(TAG, "onPostExecute: done");
    }
  }

  private void drawBeforeSimplify(List<Point> points) {
    Log.d(TAG, "drawBeforeSimplify: starting");
    List<Feature> featureList = new ArrayList<>();
    for (int i = 0; i < points.size(); i++) {
      featureList.add(Feature.fromGeometry(Point.fromLngLat(points.get(i).longitude(), points.get(i).latitude())));
    }
    addLineLayer("rawLine", featureList, "#8a8acb");
  }

  private void drawSimplify(List<Point> points) {
    Log.d(TAG, "drawSimplify: starting");
    List<Point> before = new ArrayList<>();
    for (int i = 0; i < points.size(); i++) {
      before.add(points.get(i));
    }
    List<Point> after = PolylineUtils.simplify(before, 0.001);
    List<Feature> result = new ArrayList<>();
    for (int i = 0; i < after.size(); i++) {
      result.add(Feature.fromGeometry(Point.fromLngLat(after.get(i).longitude(), after.get(i).latitude())));
    }
    addLineLayer("simplifiedLine", result, "#3bb2d0");
  }

  private void addLineLayer(String layerId, List<Feature> features, String lineColorHex) {
    Log.d(TAG, "addLineLayer: layerId layer being added for " + layerId);
    String sourceId = "source for " + layerId;
    GeoJsonSource geoJsonSource = new GeoJsonSource(sourceId,
      FeatureCollection.fromFeatures(features));
    map.getStyle().addSource(geoJsonSource);
    LineLayer lineLayer = new LineLayer(layerId, sourceId);
    lineLayer.setProperties(
      lineColor(Color.parseColor(lineColorHex)),
      lineWidth(4f)
    );
    map.getStyle().addLayer(lineLayer);
  }
}
