package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.api.utils.turf.TurfJoins;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TurfInsideActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener, OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap map;
  private Polygon polygon;
  private Marker withinMarker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_turf_inside);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);

    Toast.makeText(this, getString(R.string.tap_on_map_turf_inside_instruction), Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    map = mapboxMap;
    map.setOnMapClickListener(TurfInsideActivity.this);

    // Draw and display GeoJSON polygon on map
    new DrawGeoJson().execute();
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {

    // Remove marker if already on map
    if (withinMarker != null) {
      map.removeMarker(withinMarker);
    }

    if (polygon != null) {
      // Draw marker where map was clicked on
      withinMarker = map.addMarker(new MarkerOptions().position(point));

      List<Position> polygonPositions = new ArrayList<>();
      for (LatLng latLng : polygon.getPoints()) {
        polygonPositions.add(Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude()));
      }

      // Use TurfJoins.inside() to check whether marker is inside of polygon area
      boolean pointWithin = TurfJoins.inside(Position.fromCoordinates(
        withinMarker.getPosition().getLongitude(), withinMarker.getPosition().getLatitude()), polygonPositions);

      // Create actions depending on whether the marker is inside polygon area
      if (pointWithin) {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.turf_inside_marker_status_inside),
          Snackbar.LENGTH_SHORT).show();

      } else {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.turf_inside_marker_status_outside),
          Snackbar.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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

  // Async task which uses GeoJSON file to draw polygon on map
  private class DrawGeoJson extends AsyncTask<Void, Void, List<LatLng>> {
    @Override
    protected List<LatLng> doInBackground(Void... voids) {

      ArrayList<LatLng> points = new ArrayList<>();

      try {

        // Load GeoJSON file
        InputStream inputStream = getAssets().open("fenway_park_geofence.geojson");
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

          if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("Polygon")) {

            // Get the array of coordinates
            JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);

            for (int lc = 0; lc < coords.length(); lc++) {
              JSONArray coord = coords.getJSONArray(lc);

              LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));

              points.add(latLng);

            }
          }
        }

      } catch (Exception exception) {
        Log.e("TurfInsideActivity", "Exception Loading GeoJSON: " + exception.toString());
      }
      return points;
    }

    @Override
    protected void onPostExecute(List<LatLng> points) {
      super.onPostExecute(points);

      if (points.size() > 0) {
        LatLng[] pointsArray = points.toArray(new LatLng[points.size()]);

        // Draw points on MapView
        polygon = map.addPolygon(new PolygonOptions()
          .add(pointsArray)
          .fillColor(Color.parseColor("#FF8DD0FF"))
          .alpha(0.50f));
      }
    }
  }
}
