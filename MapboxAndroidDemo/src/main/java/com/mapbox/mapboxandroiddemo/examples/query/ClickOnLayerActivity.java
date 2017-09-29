package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;

import java.net.URL;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

/**
 * Detect click events on a polygon that was added as a GeoJsonSource.
 */
public class ClickOnLayerActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private static final String geoJsonSourceId = "geoJsonData";
  private static final String geoJsonLayerId = "polygonFillLayer";
  private FillLayer layer;
  private GeoJsonSource source;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_click_on_layer);

    Toast.makeText(ClickOnLayerActivity.this, R.string.click_on_polygon_toast_instruction,
      Toast.LENGTH_SHORT).show();

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        ClickOnLayerActivity.this.mapboxMap = mapboxMap;

        addGeoJsonSourceToMap();

        // Create FillLayer with GeoJSON source and add the FillLayer to the map
        layer = new FillLayer(geoJsonLayerId, geoJsonSourceId);
        layer.setProperties(fillOpacity(0.5f));
        mapboxMap.addLayer(layer);

        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
          @Override
          public void onMapClick(@NonNull LatLng point) {
            PointF pointf = mapboxMap.getProjection().toScreenLocation(point);
            RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);

            List<Feature> featureList = mapboxMap.queryRenderedFeatures(rectF, geoJsonLayerId);

            for (com.mapbox.services.commons.geojson.Feature feature : featureList) {
              Log.d("Feature found with %1$s", feature.toJson());

              Toast.makeText(ClickOnLayerActivity.this, R.string.click_on_polygon_toast,
                Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
    });
  }

  private void addGeoJsonSourceToMap() {
    try {
      // Load GeoJSONSource
      source = new GeoJsonSource(geoJsonSourceId, new URL("https://gist.githubusercontent"
        + ".com/tobrun/cf0d689c8187d42ebe62757f6d0cf137/raw/4d8ac3c8333f1517df9d303"
        + "d58f20f4a1d8841e8/regions.geojson"));

      // Add GeoJsonSource to map
      mapboxMap.addSource(source);

    } catch (Throwable throwable) {
      Log.e("ClickOnLayerActivity", "Couldn't add GeoJsonSource to map", throwable);
    }
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}