package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mapbox.api.isochrone.IsochroneCriteria;
import com.mapbox.api.isochrone.MapboxIsochrone;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.turf.TurfMeta;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use an Android system seekbar to set the minute range and use the Mapbox Isochrone API
 * (https://www.mapbox.com/api-documentation/#isochrone) to see how far one can
 * travel in a car.
 */
public class IsochroneSeekbarActivity extends AppCompatActivity {

  private static final String RESPONSE_FILL_LAYER_GEOJSON_SOURCE_ID = "RESPONSE_FILL_LAYER_GEOJSON_SOURCE_ID";
  private static final String ISOCHRONE_FILL_LAYER = "ISOCHRONE_FILL_LAYER";
  private static final String MAP_CENTER_DOWNTOWN_VIENNA_SOURCE_ID = "MAP_CENTER_DOWNTOWN_VIENNA_SOURCE_ID";
  private static final String MAP_CENTER_DOWNTOWN_VIENNA_ICON_ID = "MAP_CENTER_DOWNTOWN_VIENNA_ICON_ID";
  private static final String MAP_CENTER_DOWNTOWN_VIENNA_LAYER_ID = "MAP_CENTER_DOWNTOWN_VIENNA_LAYER_ID";
  private static final Point DOWNTOWN_VIENNA = Point.fromLngLat(16.374893, 48.20355511);
  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_isochrone_with_seekbar);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT)
            .withImage(MAP_CENTER_DOWNTOWN_VIENNA_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.red_marker)))
            .withSource(new GeoJsonSource(MAP_CENTER_DOWNTOWN_VIENNA_SOURCE_ID,
                Feature.fromGeometry(DOWNTOWN_VIENNA)))
            .withSource(new GeoJsonSource(RESPONSE_FILL_LAYER_GEOJSON_SOURCE_ID))
            .withLayer(new SymbolLayer(MAP_CENTER_DOWNTOWN_VIENNA_LAYER_ID,
                MAP_CENTER_DOWNTOWN_VIENNA_SOURCE_ID).withProperties(
                iconImage(MAP_CENTER_DOWNTOWN_VIENNA_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -4f})
            )), new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                IsochroneSeekbarActivity.this.mapboxMap = mapboxMap;

                // Initialize the Seekbar slider
                final SeekBar liveWithinMinutesSeekbar =
                    findViewById(R.id.isochrone_minute_seekbar_slider);
                liveWithinMinutesSeekbar.setMax(60);
                liveWithinMinutesSeekbar.incrementProgressBy(5);
                liveWithinMinutesSeekbar.setProgress(30);

                // Initialize the TextView, which will update as the slider is moved
                // back and forth.
                final TextView liveWithinMinTextView = findViewById(R.id.minutes_textview);
                liveWithinMinTextView.setText(String.format(getString(R.string.minutes_textview),
                    liveWithinMinutesSeekbar.getProgress()));

                makeIsochroneApiCall(new LatLng(DOWNTOWN_VIENNA.latitude(), DOWNTOWN_VIENNA.longitude()),
                    liveWithinMinutesSeekbar.getProgress());

                liveWithinMinutesSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                  @Override
                  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progress = progress / 5;
                    progress = progress * 5;
                    liveWithinMinTextView.setText(String.format(getString(R.string.minutes_textview), progress));
                  }

                  @Override
                  public void onStartTrackingTouch(SeekBar seekBar) {
                    // Not needed in this example.
                  }

                  @Override
                  public void onStopTrackingTouch(SeekBar seekBar) {
                    makeIsochroneApiCall(new LatLng(DOWNTOWN_VIENNA.latitude(),
                        DOWNTOWN_VIENNA.longitude()), seekBar.getProgress());
                  }
                });

                initFillLayer(style);
              }
            });
      }
    });
  }

  /**
   * Make a request to the Mapbox Isochrone API
   *
   * @param mapClickPoint The center point of the isochrone. It is part of the API request.
   * @param contourMin    The number of travel minutes to calculate Isochrone information for.
   */
  private void makeIsochroneApiCall(@NonNull LatLng mapClickPoint, @NonNull Integer contourMin) {

    MapboxIsochrone mapboxIsochroneRequest = MapboxIsochrone.builder()
        .accessToken(getString(R.string.access_token))
        .profile(IsochroneCriteria.PROFILE_DRIVING)
        .addContoursMinutes(contourMin)
        .polygons(true)
        .addContoursColors("5a42f4")
        .denoise(.4f)
        .generalize(10f)
        .coordinates(Point.fromLngLat(mapClickPoint.getLongitude(), mapClickPoint.getLatitude()))
        .build();

    mapboxIsochroneRequest.enqueueCall(new Callback<FeatureCollection>() {
      @Override
      public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
        // Redraw Isochrone information based on response body
        if (response.body() != null) {

          FeatureCollection featureCollection = response.body();

          mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              GeoJsonSource fillLayerSource = style.getSourceAs(
                  RESPONSE_FILL_LAYER_GEOJSON_SOURCE_ID);
              if (fillLayerSource != null && featureCollection.features() != null
                && featureCollection.features().size() > 0) {
                fillLayerSource.setGeoJson(featureCollection);

                // Move the camera from the map in case it's too zoomed in.
                // This is here so that the isochrone information can be seen if the camera
                // is too close to the map.
                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                  .includes(createLatLngsForCameraBounds(featureCollection.features()))
                  .build();

                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
                  latLngBounds, 50), 2000);
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
   * Use the {@link TurfMeta#coordAll(Feature, boolean)} method and the Isochrone API
   * response to build a list of {@link LatLng}. The list will be used to create a
   * camera bounds so that the camera can adjust to the size of the visible Isochrone
   * API data.
   *
   * @param featureList list from the body of the Isochrone API response.
   * @return a list of {@link LatLng}.
   */
  private List<LatLng> createLatLngsForCameraBounds(List<Feature> featureList) {
    List<LatLng> latLngList = new ArrayList<>(featureList.size());
    for (Feature singleFeature : featureList) {
      for (Point singlePoint : TurfMeta.coordAll(singleFeature, false)) {
        latLngList.add(new LatLng((singlePoint.latitude()), singlePoint.longitude()));
      }
    }
    return latLngList;
  }

  /**
   * Add a FillLayer so that that polygon returned by the Isochrone API response can be displayed
   */
  private void initFillLayer(@NonNull Style style) {
    // Create and style a FillLayer based on information in the Isochrone API response
    FillLayer isochroneFillLayer = new FillLayer(ISOCHRONE_FILL_LAYER,
        RESPONSE_FILL_LAYER_GEOJSON_SOURCE_ID);
    isochroneFillLayer.setProperties(
        fillColor(get("color")),
        fillOpacity(.7f)); // You could also pass in get("opacity")) instead of a hardcoded value
    isochroneFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    style.addLayerAbove(isochroneFillLayer, "land");
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