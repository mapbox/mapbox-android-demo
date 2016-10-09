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
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Function;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.NoSuchLayerException;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
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
import static com.mapbox.mapboxsdk.style.layers.Function.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillAntialias;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOutlineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.symbolPlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class IndoorMapActivity extends AppCompatActivity {

  private static final String TAG = "IndoorMapActivity";
  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MapboxAccountManager.start(this, getString(R.string.access_token));
    setContentView(R.layout.activity_lab_indoor_map);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {
        map = mapboxMap;

        GeoJsonSource indoorBuildingSource = new GeoJsonSource("indoor-building", loadJSONFromAsset("tech_annex.geojson"));
        mapboxMap.addSource(indoorBuildingSource);

        GeoJsonSource mainBuildingSource = new GeoJsonSource("main-building-source", loadJSONFromAsset("college_of_tech.geojson"));
        mapboxMap.addSource(mainBuildingSource);

        GeoJsonSource mainBuildingSourceLvlTwo = new GeoJsonSource("main-building-source-lvl-two", loadJSONFromAsset("college_of_tech_lvl_one.geojson"));
        mapboxMap.addSource(mainBuildingSourceLvlTwo);

        // Add the building layers since we know zoom levels in range
        loadBuildingLayers();
        loadMainBuildingLvlOneLayer();

        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
          @Override
          public void onMapClick(@NonNull LatLng latLng) {


            try {
              mapboxMap.removeLayer("main-building-level-two-fill-layer");
              mapboxMap.removeLayer("main-building-level-two-line-layer");
              loadMainBuildingLvlOneLayer();
            } catch (NoSuchLayerException noSuchLayerException) {
              noSuchLayerException.printStackTrace();
            }

            try {
              mapboxMap.removeLayer("main-building-level-one-fill-layer");
              mapboxMap.removeLayer("main-building-level-one-line-layer");
              loadMainBuildingLvlTwoLayer();
            } catch (NoSuchLayerException noSuchLayerException) {
              noSuchLayerException.printStackTrace();
            }

            final PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
            List<Feature> features = mapboxMap.queryRenderedFeatures(point, "indoor-building-fill");

            //remove layer / source if already added
            try {
              mapboxMap.removeSource("highlighted-shapes-source");
              mapboxMap.removeLayer("highlighted-shapes-layer");
            } catch (Exception exception) {
              //that's ok
            }

            if (mapboxMap.getCameraPosition().zoom > 17) {
              //Add layer / source
              mapboxMap.addSource(new GeoJsonSource("highlighted-shapes-source", FeatureCollection.fromFeatures(features)));
              mapboxMap.addLayer(new FillLayer("highlighted-shapes-layer", "highlighted-shapes-source")
                .withProperties(fillColor(Color.parseColor("#50667f"))));

              if (features.size() > 0) {
                if (features.get(0).getProperty("tags").getAsJsonObject().has("ref")) {
                  Toast.makeText(IndoorMapActivity.this, features.get(0).getProperty("tags").getAsJsonObject().get("ref").toString(), Toast.LENGTH_LONG).show();
                }
              }
            }
          }
        });
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

  private void loadMainBuildingLvlOneLayer() {
    FillLayer mainBuildingLevelOneFillLayer = new FillLayer("main-building-level-one-fill-layer", "main-building-source");

//    mainBuildingLevelOneFillLayer.setFilter(eq("type", "room"));

    mainBuildingLevelOneFillLayer.setProperties(
      fillColor(Color.parseColor("#eeeeee")),
      fillOpacity(Function.zoom(0.8f,
        stop(18, fillOpacity(1f)),
        stop(17.5f, fillOpacity(0.5f)),
        stop(17, fillOpacity(0f))
      ))

    );

    map.addLayer(mainBuildingLevelOneFillLayer);

    LineLayer mainBuildingLevelOneLineLayer = new LineLayer("main-building-level-one-line-layer", "main-building-source");

    mainBuildingLevelOneLineLayer.setProperties(
      lineColor(Color.parseColor("#50667f")),
      lineWidth(0.5f),
      lineOpacity(Function.zoom(0.8f,
        stop(18, lineOpacity(1f)),
        stop(17.5f, lineOpacity(0.5f)),
        stop(17, lineOpacity(0f))
      ))
    );

    map.addLayer(mainBuildingLevelOneLineLayer);
  }

  private void loadMainBuildingLvlTwoLayer() {
    FillLayer mainBuildingLevelTwoFillLayer = new FillLayer("main-building-level-two-fill-layer", "main-building-source-lvl-two");

    mainBuildingLevelTwoFillLayer.setProperties(
      fillColor(Color.parseColor("#eeeeee")),
      fillOpacity(Function.zoom(0.8f,
        stop(18, fillOpacity(1f)),
        stop(17.5f, fillOpacity(0.5f)),
        stop(17, fillOpacity(0f))
      ))

    );

    map.addLayer(mainBuildingLevelTwoFillLayer);

    LineLayer mainBuildingLevelTwoLineLayer = new LineLayer("main-building-level-two-line-layer", "main-building-source-lvl-two");

    mainBuildingLevelTwoLineLayer.setProperties(
      lineColor(Color.parseColor("#50667f")),
      lineWidth(0.5f),
      lineOpacity(Function.zoom(0.8f,
        stop(18, lineOpacity(1f)),
        stop(17.5f, lineOpacity(0.5f)),
        stop(17, lineOpacity(0f))
      ))
    );

    map.addLayer(mainBuildingLevelTwoLineLayer);
  }

  private void loadBuildingLayers() {
    FillLayer indoorBuildingLayer = new FillLayer("indoor-building-fill", "indoor-building");

    //indoorBuildingLayer.setFilter(eq("type", "room"));

    indoorBuildingLayer.setProperties(
      fillColor(Color.parseColor("#eeeeee")),
      fillOpacity(Function.zoom(0.8f,
        stop(18, fillOpacity(1f)),
        stop(17.5f, fillOpacity(0.5f)),
        stop(17, fillOpacity(0f))
      ))

    );

    map.addLayer(indoorBuildingLayer);

    LineLayer indoorBuildingLineLayer = new LineLayer("indoor-building-line", "indoor-building");

    indoorBuildingLineLayer.setProperties(

      lineColor(Color.parseColor("#50667f")),
      lineWidth(0.5f),
      lineOpacity(Function.zoom(0.8f,
        stop(18, lineOpacity(1f)),
        stop(17.5f, lineOpacity(0.5f)),
        stop(17, lineOpacity(0f))
      ))

    );

    map.addLayer(indoorBuildingLineLayer);


//    SymbolLayer indoorBuildingSymbolLayer = new SymbolLayer("indoor-building-labels", "indoor-building");
//
//    indoorBuildingSymbolLayer.setFilter(eq("type", "way"));
//
//    indoorBuildingSymbolLayer.setProperties(
//      symbolPlacement(Property.SYMBOL_PLACEMENT_POINT),
//      textField("101")
//    );
//
//    map.addLayer(indoorBuildingSymbolLayer);
  }

  private String loadJSONFromAsset(String filename) {
    String json = null;
    try {

      InputStream is = getAssets().open(filename);

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
}
