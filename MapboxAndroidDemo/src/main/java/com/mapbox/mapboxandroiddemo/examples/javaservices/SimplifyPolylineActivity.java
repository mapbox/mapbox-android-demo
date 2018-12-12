package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        mapboxMap.setStyle(Style.LIGHT);

        new DrawGeoJson().execute();

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

    }
  }

  private void drawBeforeSimplify(List<Point> points) {

    LatLng[] pointsArray = new LatLng[points.size()];
    for (int i = 0; i < points.size(); i++) {
      pointsArray[i] = new LatLng(points.get(i).latitude(), points.get(i).longitude());
    }

    map.addPolyline(new PolylineOptions()
      .add(pointsArray)
      .color(Color.parseColor("#8a8acb"))
      .width(4));
  }

  private void drawSimplify(List<Point> points) {

    List<Point> before = new ArrayList<>();
    for (int i = 0; i < points.size(); i++) {
      before.add(points.get(i));
    }

    List<Point> after = PolylineUtils.simplify(before, 0.001);

    LatLng[] result = new LatLng[after.size()];
    for (int i = 0; i < after.size(); i++) {
      result[i] = new LatLng(after.get(i).latitude(), after.get(i).longitude());
    }

    map.addPolyline(new PolylineOptions()
      .add(result)
      .color(Color.parseColor("#3bb2d0"))
      .width(4));
  }
}
