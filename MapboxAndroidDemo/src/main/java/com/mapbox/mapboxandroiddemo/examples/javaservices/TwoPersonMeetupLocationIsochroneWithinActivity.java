package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.api.isochrone.IsochroneCriteria;
import com.mapbox.api.isochrone.MapboxIsochrone;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.within;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * See how which places of interest (POIs) are within a 15 min walk of two different peoples'
 * locations. This example requests information from the Mapbox
 * Isochrone API (https://www.mapbox.com/api-documentation/#isochrone) and then uses the
 * {@link com.mapbox.mapboxsdk.style.expressions.Expression#within(Polygon)} expression to
 * filter out POIs that aren't inside of both peoples' isochrone {@link Polygon} areas.
 */
public class TwoPersonMeetupLocationIsochroneWithinActivity extends AppCompatActivity {

  private static final String PERSON_ONE_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID =
      "PERSON_ONE_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID";
  private static final String PERSON_TWO_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID =
      "PERSON_TWO_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID";
  private static final String PERSON_ONE_ISOCHRONE_FILL_LAYER = "PERSON_ONE_ISOCHRONE_FILL_LAYER";
  private static final String PERSON_TWO_ISOCHRONE_FILL_LAYER = "PERSON_TWO_ISOCHRONE_FILL_LAYER";
  private static final String PERSON_ONE_MARKER_SOURCE_ID = "PERSON_ONE_MARKER_SOURCE_ID";
  private static final String PERSON_TWO_MARKER_SOURCE_ID = "PERSON_TWO_MARKER_SOURCE_ID";
  private static final String PERSON_ONE_MARKER_ICON_ID = "PERSON_ONE_MARKER_ICON_ID";
  private static final String PERSON_TWO_MARKER_ICON_ID = "PERSON_TWO_MARKER_ICON_ID";
  private static final LatLng PERSON_ONE_STARTING_LAT_LNG = new LatLng(
      41.37932575, 2.17045283);
  private static final LatLng PERSON_TWO_STARTING_LAT_LNG = new LatLng(
      41.39003512, 2.17685586);
  private static final int WALKING_TIME = 15;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private Polygon personOnePolygonArea;
  private Polygon personTwoPolygonArea;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_two_person_isochrone_poi_meetup);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS)
                //Add a SymbolLayer to the map so that the map click point has a visual marker. This is where the
                // Isochrone API information radiates from.
                .withImage(PERSON_ONE_MARKER_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                    getResources().getDrawable(R.drawable.luciana)))
                .withImage(PERSON_TWO_MARKER_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                    getResources().getDrawable(R.drawable.carlos)))
                .withSource(new GeoJsonSource(PERSON_ONE_MARKER_SOURCE_ID,
                    Feature.fromGeometry(Point.fromLngLat(PERSON_ONE_STARTING_LAT_LNG.getLongitude(),
                        PERSON_ONE_STARTING_LAT_LNG.getLatitude()))))
                .withSource(new GeoJsonSource(PERSON_TWO_MARKER_SOURCE_ID,
                    Feature.fromGeometry(Point.fromLngLat(PERSON_TWO_STARTING_LAT_LNG.getLongitude(),
                        PERSON_TWO_STARTING_LAT_LNG.getLatitude()))))
                .withSource(new GeoJsonSource(PERSON_ONE_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID))
                .withSource(new GeoJsonSource(PERSON_TWO_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID)),
            new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                TwoPersonMeetupLocationIsochroneWithinActivity.this.mapboxMap = mapboxMap;

                hideLayers();
                initPeopleFaceSymbols();
                initIsochroneResponsePolygonFillLayers(style);

                Toast.makeText(TwoPersonMeetupLocationIsochroneWithinActivity.this,
                    getString(R.string.drag_faces_instruction), Toast.LENGTH_SHORT).show();

                makeIsochroneApiCall(PERSON_ONE_STARTING_LAT_LNG, true);
                makeIsochroneApiCall(PERSON_TWO_STARTING_LAT_LNG, false);
              }
            });
      }
    });
  }

  private void initPeopleFaceSymbols() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconIgnorePlacement(true);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.create(new SymbolOptions()
            .withLatLng(PERSON_ONE_STARTING_LAT_LNG)
            .withIconImage(PERSON_ONE_MARKER_ICON_ID)
            .withDraggable(true));

        symbolManager.create(new SymbolOptions()
            .withLatLng(PERSON_TWO_STARTING_LAT_LNG)
            .withIconImage(PERSON_TWO_MARKER_ICON_ID)
            .withDraggable(true));

        symbolManager.addDragListener(new OnSymbolDragListener() {
          @Override
          public void onAnnotationDragStarted(Symbol annotation) {
            // Left empty on purpose because it's not needed in this example
          }

          @Override
          public void onAnnotationDrag(Symbol symbol) {
            // Left empty on purpose because it's not needed in this example
          }

          @Override
          public void onAnnotationDragFinished(Symbol annotation) {
            // annotation.getId() is either 0 or 1, so person 1 in
            // this example actually has an annotation id of 0.
            makeIsochroneApiCall(annotation.getLatLng(),
                annotation.getId() == 0);
          }
        });
      }
    });
  }

  /**
   * Make a request to the Mapbox Isochrone API
   *
   * @param finalDragLatLng The center point of the isochrone. It is part of the API request.
   */
  private void makeIsochroneApiCall(@NonNull LatLng finalDragLatLng,
                                    boolean callMadeForPersonOne) {

    MapboxIsochrone mapboxIsochroneRequest = MapboxIsochrone.builder()
        .accessToken(getString(R.string.access_token))
        .profile(IsochroneCriteria.PROFILE_WALKING)
        .addContoursMinutes(WALKING_TIME)
        .polygons(true)
        .generalize(2f)
        .denoise(.4f)
        .coordinates(Point.fromLngLat(finalDragLatLng.getLongitude(), finalDragLatLng.getLatitude()))
        .build();

    mapboxIsochroneRequest.enqueueCall(new Callback<FeatureCollection>() {
      @Override
      public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
        // Redraw Isochrone information based on response body
        if (response.body() != null && response.body().features() != null) {
          mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              if (callMadeForPersonOne) {
                GeoJsonSource personOneSource = style.getSourceAs(PERSON_ONE_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
                if (personOneSource != null && response.body().features().size() > 0) {
                  personOneSource.setGeoJson(response.body());
                }
                personOnePolygonArea = (Polygon) response.body().features().get(0).geometry();
              } else {
                GeoJsonSource personTwoSource = style.getSourceAs(PERSON_TWO_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
                if (personTwoSource != null && response.body().features().size() > 0) {
                  personTwoSource.setGeoJson(response.body());
                }
                personTwoPolygonArea = (Polygon) response.body().features().get(0).geometry();
              }

              SymbolLayer poiLabelLayer = style.getLayerAs("poi-label");
              if (poiLabelLayer != null) {
                if (personOnePolygonArea != null && personTwoPolygonArea != null) {
                  poiLabelLayer.setFilter(all(within(personOnePolygonArea), within(personTwoPolygonArea)));
                  poiLabelLayer.setProperties(
                      textSize(14f),
                      textColor(Color.BLACK));
                }
              }
            }
          });
        }
      }

      @Override
      public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
        Timber.d("Request failed: %s", throwable.getMessage());
      }
    });
  }

  /**
   * Add a FillLayer so that that polygons returned by the Isochrone API response can be displayed
   */
  private void initIsochroneResponsePolygonFillLayers(@NonNull Style style) {
    // Create and style a FillLayer based on information in the Isochrone API response
    FillLayer personOneIsochroneFillLayer = new FillLayer(PERSON_ONE_ISOCHRONE_FILL_LAYER,
        PERSON_ONE_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
    personOneIsochroneFillLayer.setProperties(
        fillColor(Color.parseColor("#41f4f1")),
        fillOpacity(.6f));
    personOneIsochroneFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    style.addLayerBelow(personOneIsochroneFillLayer, "poi-label");

    // Create and style a FillLayer based on information in the Isochrone API response
    FillLayer personTwoIsochroneFillLayer = new FillLayer(PERSON_TWO_ISOCHRONE_FILL_LAYER,
        PERSON_TWO_ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
    personTwoIsochroneFillLayer.setProperties(
        fillColor(Color.parseColor("#bc404c")),
        fillOpacity(.6f));
    personTwoIsochroneFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    style.addLayerBelow(personTwoIsochroneFillLayer, PERSON_ONE_ISOCHRONE_FILL_LAYER);
  }

  /**
   * Remove other types of label layers from the style in order to highlight the POI label layer.
   */
  private void hideLayers() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        SymbolLayer roadLabelLayer = style.getLayerAs("road-label");
        if (roadLabelLayer != null) {
          roadLabelLayer.setProperties(visibility(NONE));
        }
        SymbolLayer transitLabelLayer = style.getLayerAs("transit-label");
        if (transitLabelLayer != null) {
          transitLabelLayer.setProperties(visibility(NONE));
        }
        SymbolLayer roadNumberShieldLayer = style.getLayerAs("road-number-shield");
        if (roadNumberShieldLayer != null) {
          roadNumberShieldLayer.setProperties(visibility(NONE));
        }
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}