package com.mapbox.mapboxandroiddemo.examples.dds;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Style a rainfall map by get data from url
 */
public class AddRainFallStyleActivity extends AppCompatActivity implements OnMapReadyCallback {
  public static final String ID_SOURCE = "source-id";
  public static final String ID_LAYER = "layer-id";
  public static final String SOURCE_URL = "http://mapbox.cn/bites/201903051700";
  private MapView mapView;
  private Handler handler;
  private FillLayer[] layer = new FillLayer[3];
  FillLayer currentLayer;
  private int index = 1;
  private RefreshGeoJsonRunnable refreshGeoJsonRunnable;
  int[] idIndex = new int[] {35, 74, 117};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_style_rainfall);

    handler = new Handler();
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        addRadarData(style);
        refreshGeoJsonRunnable = new RefreshGeoJsonRunnable();
//        do {
          handler.postDelayed(refreshGeoJsonRunnable, 1000);
//        }
//        while (index == 37);
      }
    });
  }

  private class RefreshGeoJsonRunnable implements Runnable {
    @Override
    public void run() {
      currentLayer.setFilter(eq((Expression.get("idx")), literal(index)));
      index++;
      if (index == 35) {
        currentLayer = layer[1];
      } else if (index == 74) {
        currentLayer = layer[2];
      } else if (index == 117) {
        index = 0;
        currentLayer = layer[0];
      }
      handler.postDelayed(this, 1000);
    }
  }

  private void addRadarData(@NonNull Style loadedMapStyle) {
    for (int i = 0 ;i < idIndex.length;i++) {
      TileSet tileSet = new TileSet("tileSet", "http://mapbox.cn/bites/201903051700/idx" + idIndex[i] + "/{z}/{x}/{y}.pbf");
      VectorSource vectorSource = new VectorSource(
        ID_SOURCE + idIndex[i],
        tileSet
        );
      loadedMapStyle.addSource(vectorSource);
      layer[i] = loadedMapStyle.getLayerAs(ID_LAYER + idIndex[i]);

      if (layer[i] == null) {
        layer[i] = new FillLayer(ID_LAYER + idIndex[i], ID_SOURCE + idIndex[i]);
        layer[i].setFilter(eq((get("idx")), literal(0)));
        layer[i].withSourceLayer("201903051700");
        layer[i].setProperties(PropertyFactory.visibility(VISIBLE),
          fillColor(interpolate(Expression.exponential(1f),
            get("value"),
            stop(8, Expression.rgb(20, 160, 240)),
            stop(18, Expression.rgb(20, 190, 240)),
            stop(36, Expression.rgb(20, 220, 240)),
            stop(54, Expression.rgb(20, 250, 240)),
            stop(72, Expression.rgb(20, 250, 160)),
            stop(90, Expression.rgb(135, 250, 80)),
            stop(108, Expression.rgb(250, 250, 0)),
            stop(126, Expression.rgb(250, 180, 0)),
            stop(144, Expression.rgb(250, 110, 0)),
            stop(162, Expression.rgb(250, 40, 0)),
            stop(180, Expression.rgb(180, 40, 40)),
            stop(198, Expression.rgb(110, 40, 80)),
            stop(216, Expression.rgb(80, 40, 110)),
            stop(234, Expression.rgb(50, 40, 140)),
            stop(252, Expression.rgb(20, 40, 170))
            )
          ),
          PropertyFactory.fillOpacity(0.7f));
        loadedMapStyle.addLayer(layer[i]);
      }

      currentLayer = layer[0];
    }

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
    handler.removeCallbacks(refreshGeoJsonRunnable);
    refreshGeoJsonRunnable = null;
    handler = null;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
