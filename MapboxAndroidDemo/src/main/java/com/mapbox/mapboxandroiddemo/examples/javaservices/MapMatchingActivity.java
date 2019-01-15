package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.api.matching.v5.MapboxMapMatching;
import com.mapbox.api.matching.v5.models.MapMatchingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_DRIVING;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


/**
 * Match raw GPS points to the map so they align with roads and pathways.
 */
public class MapMatchingActivity extends AppCompatActivity {

  private static final String TAG = "MapMatchingActivity";

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_map_matching);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            new DrawGeoJson().execute();
          }
        });
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

  private class DrawGeoJson extends AsyncTask<Void, Void, List<Point>> {
    @Override
    protected List<Point> doInBackground(Void... voids) {

      List<Point> points = new ArrayList<>();

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
        LineString lineString = (LineString) featureCollection.features().get(0).geometry();
        for (Point singlePosition : lineString.coordinates()) {
          points.add(Point.fromLngLat(singlePosition.longitude(),
            singlePosition.latitude()));
        }
      } catch (Exception exception) {
        Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
      }
      return points;
    }

    @Override
    protected void onPostExecute(List<Point> points) {
      super.onPostExecute(points);
      drawBeforeMapMatching(points);
      drawMapMatched(points);
    }
  }

  private void drawBeforeMapMatching(List<Point> points) {
    Log.d(TAG, "drawBeforeMapMatching: starting");
    Log.d(TAG, "drawBeforeMapMatching: points size " + points.size());

    List<Feature> featureList = new ArrayList<>();
    for (int i = 0; i < points.size(); i++) {
      featureList.add(Feature.fromGeometry(Point.fromLngLat(points.get(i).longitude(), points.get(i).latitude())));
    }
    GeoJsonSource geoJsonSource = new GeoJsonSource("pre-matched-source-id",
      FeatureCollection.fromFeatures(featureList));
    map.getStyle().addSource(geoJsonSource);
    LineLayer lineLayer = new LineLayer("pre-matched-layer-id", "pre-matched-source-id");
    lineLayer.setProperties(
      lineColor(Color.parseColor("#8a8acb")),
      lineWidth(6f),
      lineOpacity(0.65f)
    );
    map.getStyle().addLayer(lineLayer);
    Log.d(TAG, "drawBeforeMapMatching: done");
  }

  private void drawMapMatched(List<Point> coordinates) {
    try {
      // Setup the request using a client.
      MapboxMapMatching client = MapboxMapMatching.builder()
        .accessToken(Mapbox.getAccessToken())
        .profile(PROFILE_DRIVING)
        .coordinates(coordinates)
        .build();

      // Execute the API call and handle the response.
      client.enqueueCall(new Callback<MapMatchingResponse>() {
        @Override
        public void onResponse(Call<MapMatchingResponse> call,
                               Response<MapMatchingResponse> response) {
          if (response.code() == 200) {
            GeoJsonSource geoJsonSource = new GeoJsonSource("matched-source-id",
              Feature.fromGeometry(LineString.fromPolyline(
                response.body().matchings().get(0).geometry(), PRECISION_6)));
            map.getStyle().addSource(geoJsonSource);
            LineLayer matchedLineLayer = new LineLayer("matched-layer-id", "matched-source-id");
            matchedLineLayer.setProperties(
              lineColor(Color.parseColor("#3bb2d0")),
              lineWidth(6f),
              lineOpacity(0.65f)
            );
            map.getStyle().addLayer(matchedLineLayer);
            Log.d(TAG, "onResponse: added matched line");
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
