package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v4.DirectionsCriteria;
import com.mapbox.services.mapmatching.v4.MapboxMapMatching;
import com.mapbox.services.mapmatching.v4.models.MapMatchingResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapMatchingActivity extends AppCompatActivity {

  private static final String TAG = "MapMatchingActivity";

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mas_map_matching);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        map = mapboxMap;

        new DrawGeoJSON().execute();

      }
    });
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

  private class DrawGeoJSON extends AsyncTask<Void, Void, List<Position>> {
    @Override
    protected List<Position> doInBackground(Void... voids) {

      List<Position> points = new ArrayList<>();

      try {
        // Load GeoJSON file
        InputStream inputStream = getAssets().open("trace.geojson");
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
      } catch (Exception e) {
        Log.e(TAG, "Exception Loading GeoJSON: " + e.toString());
      }

      return points;
    }

    @Override
    protected void onPostExecute(List<Position> points) {
      super.onPostExecute(points);

      drawBeforeMapMatching(points);

      // Convert the route to a linestring
      LineString lineString = LineString.fromCoordinates(points);
      drawMapMatched(lineString, 8);

    }
  }

  private void drawBeforeMapMatching(List<Position> points) {

    LatLng[] pointsArray = new LatLng[points.size()];
    for (int i = 0; i < points.size(); i++)
      pointsArray[i] = new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude());

    map.addPolyline(new PolylineOptions()
        .add(pointsArray)
        .color(Color.parseColor("#8a8acb"))
        .width(4));
  }

  private void drawMapMatched(LineString lineString, int precision) {
    try {
      // Setup the request using a client.
      MapboxMapMatching client = new MapboxMapMatching.Builder()
          .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
          .setProfile(DirectionsCriteria.PROFILE_DRIVING)
          .setGpsPrecison(precision)
          .setTrace(lineString)
          .build();

      // Execute the API call and handle the response.
      client.enqueueCall(new Callback<MapMatchingResponse>() {
        @Override
        public void onResponse(Call<MapMatchingResponse> call, Response<MapMatchingResponse> response) {

          // Create a new list to store the map matched coordinates.
          List<LatLng> mapMatchedPoints = new ArrayList<>();

          // Check that the map matching API response is "OK".
          if (response.code() == 200) {
            // Convert the map matched response list from position to latlng coordinates.
            for (int i = 0; i < response.body().getMatchedPoints().length; i++) {
              mapMatchedPoints.add(new LatLng(
                  response.body().getMatchedPoints()[i].getLatitude(),
                  response.body().getMatchedPoints()[i].getLongitude()));
            }

            // Add the map matched route to the Mapbox map.
            map.addPolyline(new PolylineOptions()
                .addAll(mapMatchedPoints)
                .color(Color.parseColor("#3bb2d0"))
                .width(4));
          } else {
            // If the response code does not response "OK" an error has occurred.
            Log.e(TAG, "Too many coordinates, Profile not found, invalid input, or no match");
          }
        }

        @Override
        public void onFailure(Call<MapMatchingResponse> call, Throwable t) {
          Log.e(TAG, "MapboxMapMatching error: " + t.getMessage());
        }
      });
    } catch (ServicesException e) {
      Log.e(TAG, "MapboxMapMatching error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}