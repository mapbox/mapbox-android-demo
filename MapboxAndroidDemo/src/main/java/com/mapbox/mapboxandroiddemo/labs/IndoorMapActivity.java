package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.Polygon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.eq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillAntialias;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOutlineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.symbolPlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class IndoorMapActivity extends AppCompatActivity {

  private MapView mapView;
  private com.mapbox.mapboxsdk.annotations.Polygon selectedBuilding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lab_indoor_map);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        GeoJsonSource indoorBuildingSource = new GeoJsonSource("indoor-building", loadJSONFromAsset());
        mapboxMap.addSource(indoorBuildingSource);




        FillLayer indoorBuildingLayer = new FillLayer("indoor-building-fill", "indoor-building");

        //indoorBuildingLayer.setFilter(eq("type", "room"));

        indoorBuildingLayer.setProperties(
          fillColor(Color.parseColor("#f8f8f8")),
          fillOpacity(0.6f)

        );

        mapboxMap.addLayer(indoorBuildingLayer);

        LineLayer indoorBuildingLineLayer = new LineLayer("indoor-building-line", "indoor-building");

        indoorBuildingLineLayer.setProperties(

          lineColor(Color.BLACK),
          lineWidth(2f)

        );

        mapboxMap.addLayer(indoorBuildingLineLayer);

//        SymbolLayer indoorBuildingSymbolLayer = new SymbolLayer("indoor-building-labels", "indoor-building");
//        indoorBuildingSymbolLayer.setProperties(
//          symbolPlacement(Property.SYMBOL_PLACEMENT_POINT),
//          textField("101")
//        );
//
//
//
//        mapboxMap.addLayer(indoorBuildingSymbolLayer);

//        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
//          @Override
//          public void onMapClick(@NonNull LatLng latLng) {
//
//            if (selectedBuilding != null) {
//              mapboxMap.removePolygon(selectedBuilding);
//            }
//
//            final PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
//            List<Feature> features = mapboxMap.queryRenderedFeatures(point, "indoor-building-fill");
//
//            if (features.size() > 0) {
//              String featureId = features.get(0).getId();
//
//              for (int a = 0; a < features.size(); a++) {
//                if (featureId.equals(features.get(a).getId())) {
//                  if (features.get(a).getGeometry() instanceof Polygon) {
//
//                    List<LatLng> list = new ArrayList<>();
//                    for (int i = 0; i < ((Polygon) features.get(a).getGeometry()).getCoordinates().size(); i++) {
//                      for (int j = 0;
//                           j < ((Polygon) features.get(a).getGeometry()).getCoordinates().get(i).size(); j++) {
//                        list.add(new LatLng(
//                          ((Polygon) features.get(a).getGeometry()).getCoordinates().get(i).get(j).getLatitude(),
//                          ((Polygon) features.get(a).getGeometry()).getCoordinates().get(i).get(j).getLongitude()
//                        ));
//                      }
//                    }
//
//                    Random rnd = new Random();
//                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//
//                    selectedBuilding = mapboxMap.addPolygon(new PolygonOptions()
//                      .addAll(list)
//                      .fillColor(color)
//                    );
//                  }
//                }
//              }
//            }
//          }
//        });

      }
    });


  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  public String loadJSONFromAsset() {
    String json = null;
    try {

      InputStream is = getAssets().open("indoor.geojson");

      int size = is.available();

      byte[] buffer = new byte[size];

      is.read(buffer);

      is.close();

      json = new String(buffer, "UTF-8");


    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
    return json;

  }

//  private class DrawGeoJson extends AsyncTask<Void, Void, String> {
//    @Override
//    protected String doInBackground(Void... voids) {
//
//      ArrayList<LatLng> points = new ArrayList<>();
//
//      try {
//        // Load GeoJSON file
//        InputStream inputStream = getAssets().open("indoor.geojson");
//        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
//        StringBuilder sb = new StringBuilder();
//        int cp;
//        while ((cp = rd.read()) != -1) {
//          sb.append((char) cp);
//        }
//
//        inputStream.close();
//
//        // Parse JSON
//        JSONObject json = new JSONObject(sb.toString());
//        JSONArray features = json.getJSONArray("features");
//        JSONObject feature = features.getJSONObject(0);
//        JSONObject geometry = feature.getJSONObject("geometry");
//        if (geometry != null) {
//          String type = geometry.getString("type");
//
//          // Our GeoJSON only has one feature: a line string
//          if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {
//
//            // Get the Coordinates
//            JSONArray coords = geometry.getJSONArray("coordinates");
//            for (int lc = 0; lc < coords.length(); lc++) {
//              JSONArray coord = coords.getJSONArray(lc);
//              LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
//              points.add(latLng);
//            }
//          }
//        }
//      } catch (Exception exception) {
//        Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
//      }
//
//      return points;
//    }
//
//    @Override
//    protected void onPostExecute(String geojson) {
//      super.onPostExecute(geojson);
//
//    }
//  }
}
