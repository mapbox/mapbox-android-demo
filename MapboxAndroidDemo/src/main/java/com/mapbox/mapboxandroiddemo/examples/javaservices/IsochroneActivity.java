package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.api.isochrone.IsochroneCriteria;
import com.mapbox.api.isochrone.MapboxIsochrone;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.concat;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toColor;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.SYMBOL_PLACEMENT_LINE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.symbolPlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textLetterSpacing;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textMaxAngle;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textPadding;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * See how far a car can travel in certain time periods by requesting information from the Mapbox
 * Isochrone API (https://www.mapbox.com/api-documentation/#isochrone)
 */
public class IsochroneActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

  private static final String ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID = "ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID";
  private static final String ISOCHRONE_FILL_LAYER = "ISOCHRONE_FILL_LAYER";
  private static final String ISOCHRONE_LINE_LAYER = "ISOCHRONE_LINE_LAYER";
  private static final String TIME_LABEL_LAYER_ID = "TIME_LABEL_LAYER_ID";
  private static final String MAP_CLICK_SOURCE_ID = "MAP_CLICK_SOURCE_ID";
  private static final String MAP_CLICK_MARKER_ICON_ID = "MAP_CLICK_MARKER_ICON_ID";
  private static final String MAP_CLICK_MARKER_LAYER_ID = "MAP_CLICK_MARKER_LAYER_ID";
  private static final String[] contourColors = new String[]{"80f442", "41f4f1", "bc404c"};
  private static final int[] contourMinutes = new int[]{14, 35, 53};
  private MapView mapView;
  private MapboxMap mapboxMap;
  private LatLng lastSelectedLatLng;
  private boolean usePolygon = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_isochrone);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.DARK)

            //Add a SymbolLayer to the map so that the map click point has a visual marker. This is where the
            // Isochrone API information radiates from.
            .withImage(MAP_CLICK_MARKER_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.red_marker)))
            .withSource(new GeoJsonSource(MAP_CLICK_SOURCE_ID))
            .withSource(new GeoJsonSource(ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID))
            .withLayer(new SymbolLayer(MAP_CLICK_MARKER_LAYER_ID, MAP_CLICK_SOURCE_ID).withProperties(
                iconImage(MAP_CLICK_MARKER_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -4f})
            )), new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {

                  IsochroneActivity.this.mapboxMap = mapboxMap;

                  initFillLayer(style);

                  initLineLayer(style);

                  // Set the click listener for the floating action button
                  findViewById(R.id.switch_isochrone_style_fab).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      usePolygon = !usePolygon;
                      if (lastSelectedLatLng != null) {
                        makeIsochroneApiCall(style, lastSelectedLatLng);
                      }
                    }
                  });

                  // Set the click listener for the floating action button
                  findViewById(R.id.toggle_isochrone_time_label_fab).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

                          SymbolLayer timeSymbolLayer = style.getLayerAs(TIME_LABEL_LAYER_ID);

                          if (timeSymbolLayer != null) {
                            timeSymbolLayer.setProperties(
                                VISIBLE.equals(timeSymbolLayer.getVisibility().getValue())
                                    ? visibility(NONE) : visibility(VISIBLE));

                            if (VISIBLE.equals(timeSymbolLayer.getVisibility().getValue())) {
                              Toast.makeText(IsochroneActivity.this,
                                  getString(R.string.showing_time_labels_on_line_toast),
                                  Toast.LENGTH_SHORT).show();
                            }
                          }
                        }
                      });
                    }
                  });

                  mapboxMap.addOnMapClickListener(IsochroneActivity.this);

                  Toast.makeText(IsochroneActivity.this,
                      getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
                }
              });
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    // Update the click Symbol Layer to move the red marker to wherever the map was clicked on
    lastSelectedLatLng = point;

    // Move the red marker to the map click location
    if (mapboxMap != null) {
      Style style = mapboxMap.getStyle();
      if (style != null) {
        GeoJsonSource source = style.getSourceAs(MAP_CLICK_SOURCE_ID);
        if (source != null) {
          source.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        }
        // Request and redraw the Isochrone API response information based on the map click point
        makeIsochroneApiCall(style, point);
      }
    }
    return true;
  }

  /**
   * Make a request to the Mapbox Isochrone API
   *
   * @param mapClickPoint The center point of the isochrone. It is part of the API request.
   */
  private void makeIsochroneApiCall(@NonNull Style style, @NonNull LatLng mapClickPoint) {

    MapboxIsochrone mapboxIsochroneRequest = MapboxIsochrone.builder()
      .accessToken(getString(R.string.access_token))
      .profile(IsochroneCriteria.PROFILE_DRIVING)
      .addContoursMinutes(contourMinutes[0], contourMinutes[1], contourMinutes[2])
      .polygons(usePolygon)
      .addContoursColors(contourColors[0], contourColors[1], contourColors[2])
      .generalize(2f)
      .denoise(.4f)
      .coordinates(Point.fromLngLat(mapClickPoint.getLongitude(), mapClickPoint.getLatitude()))
      .build();

    mapboxIsochroneRequest.enqueueCall(new Callback<FeatureCollection>() {
      @Override
      public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
        // Redraw Isochrone information based on response body
        if (response.body() != null && response.body().features() != null) {
          GeoJsonSource source = style.getSourceAs(ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
          if (source != null && response.body().features().size() > 0) {
            source.setGeoJson(response.body());
          }

          if (!usePolygon) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                SymbolLayer timeLabelSymbolLayer;

                // Check to see whether the LineLayer for time labels has already been created
                if (style.getLayerAs(TIME_LABEL_LAYER_ID) == null) {
                  timeLabelSymbolLayer = new SymbolLayer(TIME_LABEL_LAYER_ID,
                    ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);

                  styleLineLayer(timeLabelSymbolLayer);

                  // Add the time label LineLayer to the map
                  style.addLayer(timeLabelSymbolLayer);

                } else {
                  styleLineLayer(style.getLayerAs(TIME_LABEL_LAYER_ID));
                }
              }
            });
          }

          // Move the camera from the map in case it's too zoomed in.
          // This is here so that the isochrone information
          // can be seen if the camera is too close to the map.
          if (mapboxMap.getCameraPosition().zoom > 14) {
            CameraPosition zoomOut = new CameraPosition.Builder()
              .zoom(mapboxMap.getCameraPosition().zoom - 4.5)
              .build();
            mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(zoomOut), 1500);
          }
        }
      }

      @Override
      public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
        Timber.d("Request failed: %s", throwable.getMessage());
      }
    });
  }

  private SymbolLayer styleLineLayer(@NonNull SymbolLayer timeLabelLayerToStyle) {
    // Use the Maps SDK's data-driven styling properties to style the
    // the time label LineLayer
    timeLabelLayerToStyle.setProperties(
        visibility(Property.VISIBLE),
        textField(concat(get("contour"), literal(" MIN"))),
        textFont(new String[]{"DIN Offc Pro Bold", "Roboto Black"}),
        symbolPlacement(SYMBOL_PLACEMENT_LINE),
        textAllowOverlap(true),
        textPadding(1f),
        textMaxAngle(90f),
        textSize(interpolate(linear(), literal(1.2f),
            stop(2, 14),
            stop(8, 18),
            stop(22, 30)
        )),
        textLetterSpacing(0.1f),
        textHaloColor(Color.parseColor("#343332")),
        textColor(toColor(get("color"))),
        textHaloWidth(4f)
    );
    return timeLabelLayerToStyle;
  }

  /**
   * Add a FillLayer so that that polygons returned by the Isochrone API response can be displayed
   */
  private void initFillLayer(@NonNull Style style) {
    // Create and style a FillLayer based on information in the Isochrone API response
    FillLayer isochroneFillLayer = new FillLayer(ISOCHRONE_FILL_LAYER, ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
    isochroneFillLayer.setProperties(
      fillColor(get("color")),
      fillOpacity(get("fillOpacity")));
    isochroneFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    style.addLayerBelow(isochroneFillLayer, MAP_CLICK_MARKER_LAYER_ID);
  }

  /**
   * Add a LineLayer so that that lines returned by the Isochrone API response can be displayed
   */
  private void initLineLayer(@NonNull Style style) {
    // Create and style a LineLayer based on information in the Isochrone API response
    LineLayer isochroneLineLayer = new LineLayer(ISOCHRONE_LINE_LAYER, ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
    isochroneLineLayer.setProperties(
      lineColor(get("color")),
      lineWidth(5f),
      lineOpacity(.8f)); // You could also pass in get("opacity")) instead of a hardcoded value
    isochroneLineLayer.setFilter(eq(geometryType(), literal("LineString")));
    style.addLayerBelow(isochroneLineLayer, MAP_CLICK_MARKER_LAYER_ID);
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
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}