package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.step;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Display 2014 census data by state or county, depending on the map's zoom level.
 */
public class ChoroplethZoomChangeActivity extends AppCompatActivity {

  private static final int ZOOM_THRESHOLD = 4;

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_choropleth_zoom_change);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        VectorSource vectorSource = new VectorSource(
          "population",
          "http://api.mapbox.com/v4/mapbox.660ui7x6.json?access_token=" + Mapbox.getAccessToken()
        );
        mapboxMap.addSource(vectorSource);

        FillLayer statePopulationLayer = new FillLayer("state-population", "population");
        statePopulationLayer.withSourceLayer("state_county_population_2014_cen");
        statePopulationLayer.setFilter(Expression.eq(get("isState"), literal(true)));
        statePopulationLayer.withProperties(
          fillColor(step((get("population")), color(Color.BLACK),
            stop(0, color(Color.parseColor("#F2F12D"))),
            stop(750000, color(Color.parseColor("#EED322"))),
            stop(1000000, color(Color.parseColor("#DA9C20"))),
            stop(2500000, color(Color.parseColor("#CA8323"))),
            stop(5000000, color(Color.parseColor("#B86B25"))),
            stop(7500000, color(Color.parseColor("#A25626"))),
            stop(10000000, color(Color.parseColor("#8B4225"))),
            stop(25000000, color(Color.parseColor("#723122")))
          )),
          fillOpacity(0.75f)
        );

        mapboxMap.addLayerBelow(statePopulationLayer, "waterway-label");

        FillLayer countyPopulationLayer = new FillLayer("county-population", "population");
        countyPopulationLayer.withSourceLayer("state_county_population_2014_cen");
        countyPopulationLayer.setFilter(Expression.eq(get("isCounty"), literal(true)));
        countyPopulationLayer.withProperties(
          fillColor(step(get("population"), color(Color.TRANSPARENT),
            stop(0, color(Color.parseColor("#F2F12D"))),
            stop(100, color(Color.parseColor("#EED322"))),
            stop(1000, color(Color.parseColor("#E6B71E"))),
            stop(5000, color(Color.parseColor("#DA9C20"))),
            stop(10000, color(Color.parseColor("#CA8323"))),
            stop(50000, color(Color.parseColor("#B86B25"))),
            stop(100000, color(Color.parseColor("#A25626"))),
            stop(500000, color(Color.parseColor("#8B4225"))),
            stop(1000000, color(Color.parseColor("#723122")))
          )),
          fillOpacity(0.75f),
          visibility(NONE)
        );

        mapboxMap.addLayerBelow(countyPopulationLayer, "waterway-label");

        mapboxMap.addOnCameraMoveListener(new MapboxMap.OnCameraMoveListener() {
          @Override
          public void onCameraMove() {
            Layer stateLayer = mapboxMap.getLayer("state-population");
            Layer countyLayer = mapboxMap.getLayer("county-population");
            if (mapboxMap.getCameraPosition().zoom > ZOOM_THRESHOLD) {
              if (stateLayer != null && countyLayer != null) {
                countyLayer.setProperties(visibility(VISIBLE));
              }
            } else {
              if (stateLayer != null && countyLayer != null) {
                countyLayer.setProperties(visibility(NONE));
              }
            }
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
}
