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
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.functions.Function.zoom;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class HexabinExtrusionActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private int maxColor;
  private String[] colorStops = {"#151515", "#222", "#ffc300", "#ff8d19", "#ff5733", "#ff2e00"};
  private int heightStop = 5000;
  private String colorActive = "#3cc";
  private String[] typeList = {"total", "noise", "establishment", "poisoning", "drinking", "smoking", "others"};
  // active filter for each of the filter session
  private String activeCamera = "hexbin";
  private String activeType = "total";
  // result data field of camera, type, method combined
  private String activeDDS = "totalDensity";
  /*private maxColor =max[activeDDS];
  private maxHeight =max["totalDensity"];
*/
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
        setUpActiveGrid();

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
      fillExtrusionOpacity(0.6f));

    mapboxMap.addLayerAbove(fillExtrusionLayerActiveGridLayer, "admin-2-boundaries-dispute");
  }

  private void setUpComplaints() {
    VectorSource complaintVector = new VectorSource("points-complaints", "mapbox://yunjieli.7l1fqjio");
    mapboxMap.addSource(complaintVector);

    CircleLayer complaintCirclesLayer = new CircleLayer("points-complaints", "points-complaints");
    complaintCirclesLayer.setSourceLayer("data_complaints-1emuz6");
    complaintCirclesLayer.withProperties(
      circleRadius(
        zoom(
          exponential(
            stop(12, circleRadius(1f)),
            stop(15, circleRadius(5f))
          )
        )
      ),
      circleColor(colorStops[2]),
      circleOpacity(0f)
    );
    mapboxMap.addLayerAbove(complaintCirclesLayer, "admin-2-boundaries-dispute");
  }

  private void setUpBusinesses() {
    VectorSource businessVector = new VectorSource("points-businesses", "mapbox://yunjieli.3i12h479");
    mapboxMap.addSource(businessVector);

    CircleLayer businessCircleLayer = new CircleLayer("points-businesses", "points-businesses");
    businessCircleLayer.setSourceLayer("data_businesses-0lvzk6");
    businessCircleLayer.withProperties(
      circleRadius(
        zoom(
          exponential(
            stop(12, circleRadius(3f)),
            stop(15, circleRadius(8f))
          )
        )
      ),
      circleColor(colorStops[5]),
      circleOpacity(0f)
    );
    mapboxMap.addLayerAbove(businessCircleLayer, "admin-2-boundaries-dispute");
  }

  private void setUpGridsCountLayer() {
    SymbolLayer gridsCountLayer = new SymbolLayer("grids-count", "grids");
    gridsCountLayer.withProperties(
      textOpacity(0f),
      textSize(14f),
      textField("{" + activeDDS + "}"),
      textColor(colorStops[2])
    );
  }

  private void setUpPointsActiveLayer() {
    GeoJsonSource pointActiveSource = new GeoJsonSource("point-active", "pointActive");
    mapboxMap.addSource(pointActiveSource);

    CircleLayer activePointCircleLayer = new CircleLayer("point-active", "point-active");
    activePointCircleLayer.withProperties(
      circleRadius(15f),
      circleColor(colorStops[2]),
      circleOpacity(.3f),
      circleBlur(1f)
    );

    mapboxMap.addLayerAbove(activePointCircleLayer, "points-businesses");
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
