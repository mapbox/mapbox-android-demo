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
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.mapmatching.v4.MapMatchingCriteria;
import com.mapbox.services.api.mapmatching.v4.MapboxMapMatching;
import com.mapbox.services.api.mapmatching.v4.models.MapMatchingResponse;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

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
  private MapboxMapMatching client;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_mas_map_matching);

    mapView = (MapView) findViewById(R.id.mapView);
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
    // Cancel the MapMatching API request
    if (client != null) {
      client.cancelCall();
    }
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
      } catch (Exception exception) {
        Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
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
    for (int i = 0; i < points.size(); i++) {
      pointsArray[i] = new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude());
    }

    map.addPolyline(new PolylineOptions()
      .add(pointsArray)
      .color(Color.parseColor("#8a8acb"))
      .width(4));
  }

  private void drawMapMatched(LineString lineString, int precision) {
    try {
      // Setup the request using a client.
      client = new MapboxMapMatching.Builder()
        .setAccessToken(Mapbox.getAccessToken())
        .setProfile(MapMatchingCriteria.PROFILE_DRIVING)
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
        public void onFailure(Call<MapMatchingResponse> call, Throwable throwable) {
          Log.e(TAG, "MapboxMapMatching error: " + throwable.getMessage());
        }
      });
    } catch (ServicesException servicesException) {
      Log.e(TAG, "MapboxMapMatching error: " + servicesException.getMessage());
      servicesException.printStackTrace();
    }
  }
}