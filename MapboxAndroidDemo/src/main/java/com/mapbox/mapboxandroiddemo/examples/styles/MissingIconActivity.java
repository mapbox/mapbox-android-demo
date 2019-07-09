package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;

/**
 * Use the {@link MapView#addOnStyleImageMissingListener(MapView.OnStyleImageMissingListener)}
 * to handle the situation where a SymbolLayer tries using a missing image as an icon. If an icon-image
 * cannot be found in a map style, a custom image can be provided to the map via
 * the listener.
 */
public class MissingIconActivity extends AppCompatActivity {

  private static final String ICON_SOURCE_ID = "ICON_SOURCE_ID";
  private static final String ICON_LAYER_ID = "ICON_LAYER_ID";
  private static final String PROFILE_NAME = "PROFILE_NAME";
  private static final String CARLOS = "Carlos";
  private static final String ANTONY = "Antony";
  private static final String MARIA = "Maria";
  private static final String LUCIANA = "Luciana";
  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_styles_missing_icon);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        // Add Features which represent the location of each profile photo SymbolLayer icon
        Feature carlosFeature = Feature.fromGeometry(Point.fromLngLat(-7.9760742,
          41.2778064));
        carlosFeature.addStringProperty(PROFILE_NAME, CARLOS);

        Feature antonyFeature = Feature.fromGeometry(Point.fromLngLat(-8.0639648,
          37.5445773));
        antonyFeature.addStringProperty(PROFILE_NAME, ANTONY);

        Feature mariaFeature = Feature.fromGeometry(Point.fromLngLat(-9.1845703,
          38.9764924));
        mariaFeature.addStringProperty(PROFILE_NAME, MARIA);

        Feature lucianaFeature = Feature.fromGeometry(Point.fromLngLat(-7.5146484,
          40.2459915));
        lucianaFeature.addStringProperty(PROFILE_NAME, LUCIANA);

        // Use a URL to build and add a Style object to the map. Then add a source to the Style.
        mapboxMap.setStyle(
          new Style.Builder().fromUri(Style.LIGHT)
            .withSource(new GeoJsonSource(ICON_SOURCE_ID,
              FeatureCollection.fromFeatures(new Feature[] {
                carlosFeature,
                antonyFeature,
                mariaFeature,
                lucianaFeature}))),
          new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              MissingIconActivity.this.mapboxMap = mapboxMap;

              // Add a SymbolLayer to the style. iconImage is set to a value that will
              // be used later in the addOnStyleImageMissingListener below
              style.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(get(PROFILE_NAME)),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                textField(get(PROFILE_NAME)),
                textIgnorePlacement(true),
                textAllowOverlap(true),
                textOffset(new Float[] {0f, 2f})
              ));
            }
          });
      }
    });

    // Use the listener to match the id with the appropriate person. The correct profile photo is
    // given to the map during "runtime".
    mapView.addOnStyleImageMissingListener(new MapView.OnStyleImageMissingListener() {
      @Override
      public void onStyleImageMissing(@NonNull String id) {
        switch (id) {
          case CARLOS:
            addImage(id, R.drawable.carlos);
            break;
          case ANTONY:
            addImage(id, R.drawable.antony);
            break;
          case MARIA:
            addImage(id, R.drawable.maria);
            break;
          case LUCIANA:
            addImage(id, R.drawable.luciana);
            break;
          default:
            addImage(id, R.drawable.carlos);
            break;
        }
      }
    });
  }

  private void addImage(String id, int drawableImage) {
    Style style = mapboxMap.getStyle();
    if (style != null) {
      style.addImageAsync(id, BitmapUtils.getBitmapFromDrawable(
        getResources().getDrawable(drawableImage)));
    }
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
