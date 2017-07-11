package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.functions.Function.property;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Style a rainfall map by get data from url
 */
public class AddRainFallStyleActivity extends AppCompatActivity implements OnMapReadyCallback {

  public static final String MAPBOX_ACCESS_TOKEN="pk.eyJ1Ijoic2hlbmhvbmdpc3NreSIsImEiOiJjaXlzanRtNGswMDB3MzNvNDh3NzJqNmNnIn0.8LvCg1s5Qb88lwItbSFOzg";
  public  static final String ID_SOURCE="moji-source";
  public  static final String ID_LAYER="moji-layer";
  public static final String SOURCE_URL="mapbox://shenhongissky.6vm8ssjm";

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Handler handler;
  private FillLayer layer;
  private int index =1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_style_rainfall);
    handler = new Handler();
    mapView = (MapView) findViewById(R.id.mapview);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
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

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    addRadar(mapboxMap);
//    for (int i = 1; i < 40; i++) {
    RefreshGeoJsonRunnable refreshGeoJsonRunnable = new RefreshGeoJsonRunnable();
    do {
      handler.postDelayed(refreshGeoJsonRunnable, 1000);
    }while (index==39);
  }

  private class RefreshGeoJsonRunnable implements Runnable {
    RefreshGeoJsonRunnable() {
    }
    @Override
    public void run() {
      layer.setFilter(Filter.eq("idx", index));
      index++;
      if(index>39) {
        index = 0;
      }
      handler.postDelayed(this, 1000);
    }
  }

  private void addRadar(MapboxMap mapboxMap) {
    VectorSource vectorSource = new VectorSource(
      ID_SOURCE,
      SOURCE_URL
    );
    mapboxMap.addSource(vectorSource);
    // add layer
    layer = (FillLayer) mapboxMap.getLayer(ID_LAYER);
    if (layer == null) {
      layer = new FillLayer(ID_LAYER,ID_SOURCE);
      layer.withSourceLayer("whole");
      layer.setFilter(Filter.eq("idx", 0));
      layer.setProperties(PropertyFactory.visibility(VISIBLE),
        fillColor(
          property(
            "value",
            exponential(
              stop(8, fillColor(Color.argb(1, 20, 160, 240))),
              stop(18, fillColor(Color.argb(1, 20, 190, 240))),
              stop(36, fillColor(Color.argb(1, 20, 220, 240))),
              stop(54, fillColor(Color.argb(1, 20, 250, 240))),
              stop(72, fillColor(Color.argb(1, 20, 250, 160))),
              stop(90, fillColor(Color.argb(1, 135, 250, 80))),
              stop(108, fillColor(Color.argb(1, 250, 250, 0))),
              stop(126, fillColor(Color.argb(1, 250, 180, 0))),
              stop(144, fillColor(Color.argb(1, 250, 110, 0))),
              stop(162, fillColor(Color.argb(1, 250, 40, 0))),
              stop(180, fillColor(Color.argb(1, 180, 40, 40))),
              stop(198, fillColor(Color.argb(1, 110, 40, 80))),
              stop(216, fillColor(Color.argb(1, 80, 40, 110))),
              stop(234, fillColor(Color.argb(1, 50, 40, 140))),
              stop(252, fillColor(Color.argb(1, 20, 40, 170)))
            )
          )
        ),
        PropertyFactory.fillOpacity(0.7f));
      mapboxMap.addLayer(layer);
    }
  }
}
