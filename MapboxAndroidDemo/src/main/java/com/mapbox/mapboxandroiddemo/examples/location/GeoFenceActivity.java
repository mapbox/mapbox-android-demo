package com.mapbox.mapboxandroiddemo.examples.location;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.api.utils.turf.TurfJoins;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class GeoFenceActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private Polygon polygon;
  private Marker withinMarker;
  private TextView markerLocationTextView;
  private String TAG = "GeoFenceActivity";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_geo_fence);
    markerLocationTextView = (TextView) findViewById(R.id.geofenced_marker_status);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        map = mapboxMap;

        //Drawing and display geofence on map
        new DrawGeoJson().execute();

        onMapClick();

      }
    });

    Toast.makeText(this, getString(R.string.tap_on_map_geofence_instruction), Toast.LENGTH_SHORT).show();

  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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

  public void onMapClick() {

    map.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
      @Override
      public void onMapClick(@NonNull LatLng point) {

        Log.d(TAG, "onMapClick: ");

        //remove marker if already on map
        if (withinMarker != null) {
          Log.d(TAG, "onMapClick: withinMarker != null");
          map.removeMarker(withinMarker);
        }

        if (polygon != null) {

          Log.d(TAG, "onMapClick: polygon != null");

          //draw marker wherever map was clicked
          withinMarker = map.addMarker(new MarkerViewOptions().position(point));
          Log.d(TAG, "onMapClick: withinMarker = map.addMarker(new MarkerViewOptions().position(point));");


          //check whether marker is within geofence polygon area
          List<Position> polygonPositions = new ArrayList<>();
          for (LatLng latLng : polygon.getPoints()) {
            polygonPositions.add(Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude()));
          }

          //create true/false actions depending on whether marker is inside polygon area
          boolean pointWithin = TurfJoins.inside(Position.fromCoordinates(
            withinMarker.getPosition().getLongitude(), withinMarker.getPosition().getLatitude()), polygonPositions);

          if (pointWithin) {

            Log.d("GeoFence", "onMapClick: pointWithin == true");

            markerLocationTextView.setText(getString(R.string.geofence_marker_status, getString(R.string.geofence_marker_status_true)));

          } else {

            Log.d("GeoFence", "onMapClick: pointWithin == false");

            markerLocationTextView.setText(getString(R.string.geofence_marker_status, getString(R.string.geofence_marker_status_false)));

          }


        }


      }
    });


  }


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

            // Get the Coordinates
            JSONArray coords = geometry.getJSONArray("coordinates");
            Log.d(TAG, "doInBackground: coords = " + coords);

            for (int lc = 0; lc < coords.length(); lc++) {
              JSONArray coord = coords.getJSONArray(lc);
              Log.d(TAG, "doInBackground: coord in loop = " + coord);
              LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
              points.add(latLng);

            }
          }
        }

      } catch (Exception exception) {
        Log.e("GeoFenceActivity", "Exception Loading GeoJSON: " + exception.toString());
      }

      return points;
    }

    @Override
    protected void onPostExecute(List<LatLng> points) {
      super.onPostExecute(points);

      if (points.size() > 0) {
        LatLng[] pointsArray = points.toArray(new LatLng[points.size()]);

        // Draw Points on MapView
        polygon = map.addPolygon(new PolygonOptions()
          .add(pointsArray)
          .fillColor(Color.parseColor("#e55e5e"))
          .alpha(0.50f));
      }
    }
  }


}
