package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.IntervalStops;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.mapbox.services.commons.geojson.FeatureCollection;

import static com.mapbox.mapboxsdk.style.functions.Function.zoom;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.interval;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class HexabinExtrusionActivity extends AppCompatActivity implements
  MapView.OnMapChangedListener/*, MapboxMap.OnMapLongClickListener */ {

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
  private String activeDds = "totalDensity";
  private CameraPosition previousCamera;
  private FeatureCollection empty;
  private FeatureCollection gridActive;
  private FeatureCollection pointActive;



  /*private maxColor =max[activeDds];
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
      public void onMapReady(MapboxMap mapboxMap) {
       /* addGrids3dLayer();
        setUpActiveGrid();*/

        for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
          Log.d("HexabinActivity", "onMapReady: layer name = " + mapboxMap.getLayers().get(x).getId());
        }

        mapboxMap.setLatLngBoundsForCameraTarget(new LatLngBounds.Builder()
          .include(new LatLng(40.609614478818855, -74.09692544272578))
          .include(new LatLng(40.846999364699144, -73.77487016324935))
          .build());

        setUpComplaints();
        setUpBusinesses();


        /*setUpGridsCountLayer();
        setUpPointsActiveLayer();*/
      }
    });
  }

  @Override
  public void onMapChanged(int change) {
    if (!activeCamera.equals("inspector")) {
      activeCamera = mapboxMap.getCameraPosition().zoom > 14 ? "dotted" : "hexbin";
      setLayers();
    }
    ;
  }
  /*@Override
  public void onMapLongClick(@NonNull LatLng point) {
    LatLng coordinates = new LatLng(point.getLatitude(), point.getLongitude());
    String html = "";
    Feature[] queryComplaints;
    if (activeCamera.equals("hexbin")) {
      Feature[] query = mapboxMap.queryRenderedFeatures(point., "grids-3d");
      if (query.) {
        html = query[0].properties.total + " complaints here with " + query[0].properties.businesses + " restaurants/cafes/bars. ";
        html = activeType == = "total" ? html : html + query[0].properties[activeType] + " of them are about " + activeType + ". ";
        html += "Click to see the incidents.";

        gridActive.features = query[0]];
        mapboxMap.getSource("grid-active").setData(gridActive);
      } else {
        mapboxMap.getSource("grid-active").setData(gridActive);
      }
      // else: "dotted" or "inspector"
    } else {
      queryComplaints = mapboxMap.queryRenderedFeatures(point., "points-complaints");

    }
    if (queryComplaints.length()) {
      html += "<h2>" + queryComplaints.length + " complaint(s):</h2>";

      // show top 5 and hide the others
      int max = 3;
      int length = queryComplaints.length() <= max ? queryComplaints.length : max;
      for (int index = 0; index < length; index++) {
        String complaint = "<p>" + queryComplaints[0].getStringProperty("Complaint Type")
          + " : " + queryComplaints[0].getStringProperty("Descriptor") + "</p>";
        html += complaint;
      }
      ;
      if (queryComplaints.length > max) {
        html += "<p>...</p>";
      }
      pointActive.getFeatures() = queryComplaints;
      mapboxMap.getSource("point-active").setData(pointActive);
    } else {
      mapboxMap.getSource("point-active").setData(empty);
    }

    if (html.equals("")) {
      popup.remove();
      $(".mapboxgl-canvas-container").css("cursor", "-webkit-grab");
    } else {
      $(".mapboxgl-canvas-container").css("cursor", "none");
      popup.setLngLat(coordinates)
        .setHTML(html)
        .addTo(map);
    }
  }*/


  private void setLayers() {
    if (activeCamera.equals("hexbin")) {
      mapboxMap.getLayer("points-complaints").setProperties(circleOpacity(0f));
      mapboxMap.getLayer("points-businesses").setProperties(circleOpacity(0f));
      mapboxMap.getLayer("grids-3d").setProperties(fillExtrusionOpacity(0.6f));
      mapboxMap.getLayer("grids-active").setProperties(fillExtrusionOpacity(0.6f));
      mapboxMap.getLayer("grids-count").setProperties(textOpacity(0f));
      mapboxMap.getSource("point-active");
    } else {
      if (activeCamera.equals("dotted")) {
        mapboxMap.getLayer("points-complaints").setProperties(circleOpacity(0.3f));
        mapboxMap.getLayer("points-businesses").setProperties(circleOpacity(0.2f));
        mapboxMap.getLayer("grids-3d").setProperties(fillExtrusionOpacity(0f));
        mapboxMap.getLayer("grids-active").setProperties(fillExtrusionOpacity(0f));
        mapboxMap.getLayer("grids-count").setProperties(textOpacity(0.8f));
        mapboxMap.getSource("grid-active");

      } else {
        if (activeCamera.equals("inspector")) {
          mapboxMap.getLayer("points-complaints").setProperties(circleOpacity(0.3f));
          mapboxMap.getLayer("points-businesses").setProperties(circleOpacity(0.2f));
          mapboxMap.getLayer("grids-3d").setProperties(fillExtrusionOpacity(0.0f));
          mapboxMap.getLayer("grids-active").setProperties(fillExtrusionOpacity(0.2f));
          mapboxMap.getLayer("grids-active").setProperties(fillExtrusionHeight(0f));
          mapboxMap.getLayer("grids-count").setProperties(textOpacity(0.8f));
        }
      }
    }
  }

  private void addGrids3dLayer() {
    GeoJsonSource gridSource = new GeoJsonSource("grids", "grids");
    mapboxMap.addSource(gridSource);

    /*FillExtrusionLayer fillExtrusionLayer3dGrid = new FillExtrusionLayer("grids-3d", "grids");
    fillExtrusionLayer3dGrid.withProperties(
      fillExtrusionColor(Function.property("population",
        IntervalStops.interval(
          stop(0, fillColor(Color.parseColor(colorStops[1])),
            stop(maxColor * .2, fillColor(Color.parseColor(colorStops[1])),
              stop(maxColor * .5, fillColor(Color.parseColor(colorStops[2])),
                stop(maxColor * .8, fillColor(Color.parseColor(colorStops[3])),
                  stop(maxColor * .2, fillColor(Color.parseColor(colorStops[4])),
                    stop(maxColor, fillColor(Color.parseColor(colorStops[5]))),
                    fillExtrusionHeight(Function.property("activeDds", IntervalStops.interval(
                      stop(0, fillExtrusionHeight(0f),
                        fillExtrusionOpacity(0.9f));

    FillExtrusionLayer fillExtrusionLayer3dGrid = new FillExtrusionLayer("grids-3d", "grids");
    fillExtrusionLayer3dGrid.withProperties(
      fillExtrusionOpacity(0.9f),
      fillExtrusionHeight(Function.property("activeDds", IntervalStops.interval(
        stop(0, fillExtrusionHeight(0f)))
      ))),
      fillExtrusionColor(Function.property("population",
        IntervalStops.interval(
          stop(0, fillColor(Color.parseColor(colorStops[1])),
            stop(maxColor * .2, fillColor(Color.parseColor(colorStops[2]),
              stop(maxColor * .5, fillColor(Color.parseColor(colorStops[3])),
                stop(maxColor * .8, fillColor(Color.parseColor(colorStops[4]),
                  stop(maxColor, fillColor(Color.parseColor(colorStops[5]))))))))))))))))

    mapboxMap.addLayerAbove(fillExtrusionLayer3dGrid, "admin-2-boundaries-dispute");*/

  }

  private void setUpActiveGrid() {
    GeoJsonSource gridActiveSource = new GeoJsonSource("grid-active", "gridActive");
    mapboxMap.addSource(gridActiveSource);

    FillExtrusionLayer fillExtrusionLayerActiveGridLayer = new FillExtrusionLayer("grid-active", "grid-active");
    fillExtrusionLayerActiveGridLayer.withProperties(
      fillExtrusionColor(colorActive),
      fillExtrusionHeight(Function.property("activeDds", IntervalStops.interval(
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
          interval(
            stop(12, circleRadius(1f)),
            stop(15, circleRadius(5f))
          )
        )
      ),
      circleColor(colorStops[2]),
      circleOpacity(0f)
    );
    mapboxMap.addLayer(complaintCirclesLayer);
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
    mapboxMap.addLayer(businessCircleLayer);
  }

  private void setUpGridsCountLayer() {
    SymbolLayer gridsCountLayer = new SymbolLayer("grids-count", "grids");
    gridsCountLayer.withProperties(
      textOpacity(0f),
      textSize(14f),
      textField("{" + activeDds + "}"),
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

/*
  private void getCamera() {
    // if pitch==0, don't update Camera
    if (mapboxMap.getCameraPosition().tilt == 0) {
      previousCamera.target = mapboxMap.getCameraPosition().target;
      previousCamera.zoom = mapboxMap.getCameraPosition().zoom;
      previousCamera.tilt = mapboxMap.getCameraPosition().tilt;
      previousCamera.bearing = mapboxMap.getCameraPosition().bearing;
    }
  }
*/
}
