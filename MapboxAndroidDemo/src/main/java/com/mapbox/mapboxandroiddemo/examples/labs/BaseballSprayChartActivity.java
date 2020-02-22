package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.graphics.Color.parseColor;
import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;


/**
 * Use the Maps SDK's layer filters to explore baseball "spray chart" hit data. Filter by
 * the result of the hit (single, home run, etc.) and/or by the type of
 * hit (line drive, pop fly, etc.)
 */
public class BaseballSprayChartActivity extends AppCompatActivity {

  private static final String HIT_LINE_LAYER_ID = "HIT_LINE_LAYER_ID";
  private static final String BASEBALL_ICON_GEOJSON_SOURCE_ID = "BASEBALL_ICON_GEOJSON_SOURCE_ID";
  private static final String BASEBALL_SYMBOL_LAYER_ID = "BASEBALL_SYMBOL_LAYER_ID";
  private static final String BASEBALL_ICON_ID = "BASEBALL_ICON_ID";
  private static final String VECTOR_SOURCE_ID = "VECTOR_SOURCE_ID";
  private static final LatLng BOUND_CORNER_NW = new LatLng(37.77942401073674, -122.3878240585327);
  private static final LatLng BOUND_CORNER_SE = new LatLng(37.77709202770888, -122.3908495903015);
  private static final LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder()
      .include(BOUND_CORNER_NW)
      .include(BOUND_CORNER_SE)
      .build();
  private MapView mapView;
  private MapboxMap mapboxMap;
  private VectorSource vectorSource;
  private LineLayer hitLineLayer;
  private SymbolLayer baseballSymbolLayer;
  private ArrayList<String> hitResultSpinnerOptionList;
  private boolean hitTypeHasBeenSelected = false;
  private int selectedHitResult = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_labs_baseball_spray_chart);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.SATELLITE,
            new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {
                BaseballSprayChartActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);
                initHitLineLayer(style);
                initBaseTypeSpinner();
                initBaseballSymbolLayer(style);
                initBaseballIconCheck();
              }
            });
      }
    });
  }

  /**
   * Set up the {@link LineLayer} to show the direction of the hits.
   *
   * @param loadedStyle a loaded {@link Style} object
   */
  private void initHitLineLayer(Style loadedStyle) {

    /*
    This example loads data from a Mapbox Tileset.
    A tileset is a collection of raster or vector data broken
    up into a uniform grid of square tiles. A tileset can have
    data within it.

    If you use a VectorSource, make sure you use the `setSourceLayer()`
    method, as is done below. This tells the Maps SDK to drill one level
    deeper into the tileset and reference the correct data for the layer.
    If you've created your own Mapbox account, you can find tilesets at
    https://studio.mapbox.com/tilesets. You can make a GeoJSON-based
    dataset at https://studio.mapbox.com/datasets, have Mapbox Studio
    export the dataset to a tileset, and then use the tileset in your
    project.

    https://docs.mapbox.com/mapbox-gl-js/style-spec/sources/#vector
    has more about a VectorSource. VectorSource objects are also used
    in other examples in this app.
    */
    vectorSource = new VectorSource(VECTOR_SOURCE_ID,
        "mapbox://appsatmapboxcom.ck6ybxaey1hue2lnozejkugfz-1n5cb");
    loadedStyle.addSource(vectorSource);
    hitLineLayer = new LineLayer(HIT_LINE_LAYER_ID, VECTOR_SOURCE_ID);
    hitLineLayer.setSourceLayer("baseball_spray_chart_example_dat");
    hitLineLayer.setProperties(
        lineColor(
            match(get("result"), rgb(0, 0, 0),
                stop(1, color(parseColor("#ff3d3d"))),
                stop(2, color(parseColor("#3dd8ff"))),
                stop(3, color(parseColor("#66fa75"))),
                stop(4, color(parseColor("#f0e400"))))
        ),
        lineWidth(1.9f)
    );
    loadedStyle.addLayer(hitLineLayer);
  }

  private void initBaseballSymbolLayer(Style loadedStyle) {
    GeoJsonSource geoJsonSource = new GeoJsonSource(BASEBALL_ICON_GEOJSON_SOURCE_ID);
    loadedStyle.addSource(geoJsonSource);
    loadedStyle.addImage(BASEBALL_ICON_ID, BitmapFactory.decodeResource(getResources(),
        R.drawable.baseball));
    baseballSymbolLayer = new SymbolLayer(
        BASEBALL_SYMBOL_LAYER_ID, BASEBALL_ICON_GEOJSON_SOURCE_ID).withProperties(
        iconImage(BASEBALL_ICON_ID),
        iconAllowOverlap(true),
        iconIgnorePlacement(true));
    loadedStyle.addLayer(baseballSymbolLayer);
  }

  private void initBaseballIconCheck() {
    CheckBox checkBox = findViewById(R.id.show_baseball_icon_checkbox);
    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            if (isChecked) {
              List<Feature> featuresFromVectorSource = vectorSource.querySourceFeatures(
                  new String[]{"baseball_spray_chart_example_dat"}, null);

              List<Feature> pointFeatures = new ArrayList<>();
              for (Feature singleLineFeature : featuresFromVectorSource) {
                LineString lineString = (LineString) singleLineFeature.geometry();
                if (lineString != null) {
                  Feature pointFeature = Feature.fromGeometry(lineString.coordinates().get(1));
                  pointFeature.addNumberProperty("result",
                      singleLineFeature.getNumberProperty("result"));
                  pointFeatures.add(pointFeature);
                }
              }

              GeoJsonSource geoJsonSource = style.getSourceAs(BASEBALL_ICON_GEOJSON_SOURCE_ID);
              if (geoJsonSource != null) {
                geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(pointFeatures));
              }

              if (baseballSymbolLayer != null) {
                baseballSymbolLayer.setFilter(selectedHitResult == 0 ? all() :
                    eq(get("result"), literal(selectedHitResult)));
              }
            }

            if (baseballSymbolLayer != null) {
              baseballSymbolLayer.withProperties(visibility(isChecked ? VISIBLE : NONE));
            }

            if (hitLineLayer != null) {
              hitLineLayer.withProperties(visibility(isChecked ? NONE : VISIBLE));
            }
          }
        });
      }
    });
  }

  private void initBaseTypeSpinner() {
    hitResultSpinnerOptionList = new ArrayList<>();
    hitResultSpinnerOptionList.add("All");
    hitResultSpinnerOptionList.add("Single");
    hitResultSpinnerOptionList.add("Double");
    hitResultSpinnerOptionList.add("Triple");
    hitResultSpinnerOptionList.add("Home run");
    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_dropdown_item, hitResultSpinnerOptionList);
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    Spinner baseTypeSpinner = findViewById(R.id.base_type_spinner_menu);
    baseTypeSpinner.setAdapter(spinnerArrayAdapter);
    baseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (hitTypeHasBeenSelected) {
          mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              switch (hitResultSpinnerOptionList.get(position)) {
                case "All":
                  selectedHitResult = 0;
                  break;
                case "Single":
                  selectedHitResult = 1;
                  break;
                case "Double":
                  selectedHitResult = 2;
                  break;
                case "Triple":
                  selectedHitResult = 3;
                  break;
                case "Home run":
                  selectedHitResult = 4;
                  break;
                default:
                  break;
              }

              if (hitLineLayer != null) {
                hitLineLayer.setFilter(selectedHitResult == 0 ? all() : eq(get("result"),
                    literal(selectedHitResult)));
              }

              SymbolLayer baseballIconSymbolLayer =
                  style.getLayerAs(BASEBALL_SYMBOL_LAYER_ID);
              if (baseballIconSymbolLayer != null) {
                baseballIconSymbolLayer.setFilter(selectedHitResult == 0 ? all() :
                    eq(get("result"), literal(selectedHitResult)));
              }
            }

          });
        } else {
          hitTypeHasBeenSelected = true;
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        // Empty because not needed in this example
      }
    });
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
