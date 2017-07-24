package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.PolylineUtils;

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

    mapView = (MapView) findViewById(R.id.mapview);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

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

  private class DrawGeoJson extends AsyncTask<Void, Void, List<Position>> {
    @Override
    protected List<Position> doInBackground(Void... voids) {

      List<Position> points = new ArrayList<>();

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
              Position position = Position.fromCoordinates(coord.getDouble(0), coord.getDouble(1));
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
    protected void onPostExecute(List<Position> points) {
      super.onPostExecute(points);

      drawBeforeSimplify(points);
      drawSimplify(points);

    }
  }

  private void drawBeforeSimplify(List<Position> points) {

    LatLng[] pointsArray = new LatLng[points.size()];
    for (int i = 0; i < points.size(); i++) {
      pointsArray[i] = new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude());
    }

    map.addPolyline(new PolylineOptions()
      .add(pointsArray)
      .color(Color.parseColor("#8a8acb"))
      .width(4));
  }

  private void drawSimplify(List<Position> points) {

    Position[] before = new Position[points.size()];
    for (int i = 0; i < points.size(); i++) {
      before[i] = points.get(i);
    }

    Position[] after = PolylineUtils.simplify(before, 0.001);

    LatLng[] result = new LatLng[after.length];
    for (int i = 0; i < after.length; i++) {
      result[i] = new LatLng(after[i].getLatitude(), after[i].getLongitude());
    }

    map.addPolyline(new PolylineOptions()
      .add(result)
      .color(Color.parseColor("#3bb2d0"))
      .width(4));
  }
}