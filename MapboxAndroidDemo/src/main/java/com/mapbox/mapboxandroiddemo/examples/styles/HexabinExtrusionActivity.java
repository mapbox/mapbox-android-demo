package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.IntervalStops;
import com.mapbox.mapboxsdk.style.functions.stops.Stops;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;

public class HexabinExtrusionActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private int maxColor;
  private String[] colorStops = {"#151515", "#222", "#ffc300", "#ff8d19", "#ff5733", "#ff2e00"};
  private int heightStop = 5000;
  private String colorActive = "#3cc";
  private String[] typeList = {"total", "noise", "establishment", "poisoning", "drinking", "smoking", "others"};

  // for DDS threshholds, [total, density]
  /*private max =

  {
    "businesses":46,
    "total":283,
    "noise":278,
    "establishment":60,
    "poisoning":15,
    "drinking":8,
    "smoking":10,
    "others":9,
    "totalDensity":141.5,
    "noiseDensity":139,
    "establishmentDensity":20,
    "poisoningDensity":8,
    "drinkingDensity":5,
    "smokingDensity":10,
    "othersDensity":1.3,
  }*/


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_hexabin_extrusion);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap map) {

        mapboxMap = map;

        addGrids3dLayer();

        // Add grids GeoJSON source
        GeoJsonSource activePointSource = new GeoJsonSource("point-active", "pointActive");
        VectorSource complaintsSource = new VectorSource("complaints", "mapbox://yunjieli.7l1fqjio");
        VectorSource businessesSource = new VectorSource("businesses", "mapbox://yunjieli.3i12h479");

      }
    });
  }

  private void addGrids3dLayer() {
    GeoJsonSource gridSource = new GeoJsonSource("grids", "grids");
    mapboxMap.addSource(gridSource);

    FillExtrusionLayer fillExtrusionLayer3dGrid = new FillExtrusionLayer("grids-3d", "grids");
    fillExtrusionLayer3dGrid.withProperties(
      fillExtrusionColor(Function.property("population",
        IntervalStops.interval(
          stop(0, fillColor(Color.parseColor(colorStops[1])),
            stop(maxColor * .2, fillColor(Color.parseColor(colorStops[1])),
              stop(maxColor * .5, fillColor(Color.parseColor(colorStops[2])),
                stop(maxColor * .8, fillColor(Color.parseColor(colorStops[3])),
                  stop(maxColor * .2, fillColor(Color.parseColor(colorStops[4])),
                    stop(maxColor, fillColor(Color.parseColor(colorStops[5]))),
                    fillExtrusionHeight(Function.property("activeDDS", IntervalStops.interval(
                      stop(0, fillExtrusionHeight(0f),
                        fillExtrusionOpacity(0.9f));

    mapboxMap.addLayerAbove(fillExtrusionLayer3dGrid, "admin-2-boundaries-dispute");
  }

  private void setUpActiveGrid() {
    GeoJsonSource gridActiveSource = new GeoJsonSource("grid-active", "gridActive");
    mapboxMap.addSource(gridActiveSource);

    FillExtrusionLayer fillExtrusionLayerActiveGridLayer = new FillExtrusionLayer("grid-active", "grid-active");
    fillExtrusionLayerActiveGridLayer.withProperties(
      fillExtrusionColor(colorActive),
      fillExtrusionHeight(Function.property("activeDDS", IntervalStops.interval(
        stop(0, fillExtrusionHeight(0f))))),
      fillExtrusionOpacity(0.6f))

    mapboxMap.addLayerAbove(fillExtrusionLayerActiveGridLayer, "admin-2-boundaries-dispute");

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

}
