package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.models.Position;

import java.io.InputStream;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class StyleLineIdentityPropertyActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private String TAG = "StyleLineActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_style_line);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        map = mapboxMap;

        // Add GeoJSON from file and add to map

        GeoJsonSource linesSource = new GeoJsonSource("lines", loadJsonFromAsset("golden_gate_lines.geojson"));
        mapboxMap.addSource(linesSource);

        // Draw red and blue lines on map
//        drawLines();

       /* LineLayer redLine = new LineLayer("redLine", "line-source");

        redLine.setProperties(
          PropertyFactory.lineColor(Color.parseColor("#FFDE3030")),
          PropertyFactory.visibility(Property.VISIBLE),
          PropertyFactory.lineWidth(3f)
        );

        LineLayer blueLine = new LineLayer("blueLine", "line-source");

        blueLine.setProperties(
          PropertyFactory.lineColor(Color.parseColor("#FF3FA3E8")),
          PropertyFactory.visibility(Property.VISIBLE),
          PropertyFactory.lineWidth(3f)

        );

        map.addLayer(redLine);
        map.addLayer(blueLine);
*/

        LineLayer linesLayer = new LineLayer("finalLines", "lines").withProperties(
          fillColor(Color.parseColor("#FFDE3030")),
          PropertyFactory.visibility(Property.VISIBLE),
          PropertyFactory.lineWidth(3f)
        );

        mapboxMap.addLayer(linesLayer);


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

  private String loadJsonFromAsset(String filename) {


    try {
      // Load GeoJSON file
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();

      return new String(buffer, "UTF-8");

    } catch (Exception exception) {
      Log.e("StyleLineActivity", "Exception Loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
    }

  }


  private void drawLines() {


  }


}

