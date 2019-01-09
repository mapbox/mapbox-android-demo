package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Draw a polyline by parsing a GeoJSON file with the Mapbox Android SDK.
 */
public class DrawGeojsonLineActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String TAG = "DrawGeojsonLineActivity";

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_annotation_geojson_line);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        initSourceAndLayer();
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
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  private void initSourceAndLayer() {
    // Create the LineString from the list of coordinates and then make a GeoJSON

    Source geoJsonSource = new GeoJsonSource("line-source");
    mapboxMap.getStyle().addSource(geoJsonSource);
    LineLayer lineLayer = new LineLayer("linelayer", "line-source");
    // The layer properties for our line. This is where we make the line dotted, set the
    // color, etc.
    lineLayer.setProperties(
      PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
      PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
      PropertyFactory.lineOpacity(.7f),
      PropertyFactory.lineWidth(7f),
      PropertyFactory.lineColor(Color.parseColor("#3bb2d0"))
    );
    mapboxMap.getStyle().addLayer(lineLayer);
  }

  private class DrawGeoJson extends AsyncTask<Void, Void, List<Point>> {
    @Override
    protected List<Point> doInBackground(Void... voids) {

      ArrayList<Point> points = new ArrayList<>();

      try {
        // Load GeoJSON file
        InputStream inputStream = getAssets().open("example.geojson");
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
              Point singlePoint = Point.fromLngLat(coord.getDouble(0), coord.getDouble(1));
              points.add(singlePoint);
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

      if (points.size() > 0) {

        // FeatureCollection so we can add the line to our map as a layer.
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("line-source");
        if (source != null) {
          source.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(points)));
        }
      }
    }
  }
}
