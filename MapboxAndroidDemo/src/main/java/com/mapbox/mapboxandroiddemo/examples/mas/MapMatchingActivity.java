package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.mapmatching.v5.MapMatchingCriteria;
import com.mapbox.services.api.mapmatching.v5.MapboxMapMatching;
import com.mapbox.services.api.mapmatching.v5.models.MapMatchingResponse;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.PolylineUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Match raw GPS points to the map so they align with roads and pathways.
 */
public class MapMatchingActivity extends AppCompatActivity {

  private static final String TAG = "MapMatchingActivity";

  private MapView mapView;
  private MapboxMap map;
  private Polyline mapMatchedRoute;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
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
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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
        InputStream inputStream = getAssets().open("trace.geojson");
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }

        inputStream.close();
        FeatureCollection featureCollection = FeatureCollection.fromJson(sb.toString());
        LineString lineString = (LineString) featureCollection.getFeatures().get(0).getGeometry();
        points = lineString.getCoordinates();
      } catch (Exception exception) {
        Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
      }

      return points;
    }

    @Override
    protected void onPostExecute(List<Position> points) {
      super.onPostExecute(points);
      drawBeforeMapMatching(points);

      Position[] coordinates = new Position[points.size()];
      drawMapMatched(points.toArray(coordinates));
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
            .alpha(0.65f)
            .width(4));
  }

  private void drawMapMatched(Position[] coordinates) {
    try {
      // Setup the request using a client.
      MapboxMapMatching client = new MapboxMapMatching.Builder()
              .setAccessToken(Mapbox.getAccessToken())
              .setProfile(MapMatchingCriteria.PROFILE_DRIVING)
              .setCoordinates(coordinates)
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
            // By default, the SDK uses MapMatchingCriteria.GEOMETRY_POLYLINE_6, therefore
            // you need Constants.PRECISION_6 for the decode to be right
            String geometry = response.body().getMatchings().get(0).getGeometry();
            List<Position> positions = PolylineUtils.decode(geometry, Constants.PRECISION_6);
            if (positions == null) {
              return;
            }

            for (int i = 0; i < positions.size(); i++) {
              mapMatchedPoints.add(new LatLng(
                      positions.get(i).getLatitude(),
                      positions.get(i).getLongitude()));
            }

            if (mapMatchedRoute != null) {
              map.removeAnnotation(mapMatchedRoute);
            }

            // Add the map matched route to the Mapbox map.
            mapMatchedRoute = map.addPolyline(new PolylineOptions()
                    .addAll(mapMatchedPoints)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(4));
          } else {
            // If the response code does not response "OK" an error has occurred.
            Log.e(TAG, "Too many coordinates, profile not found, invalid input, or no match.");
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