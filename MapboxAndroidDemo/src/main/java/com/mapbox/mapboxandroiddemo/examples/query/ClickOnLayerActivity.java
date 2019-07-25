package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

/**
 * Detect click events on a polygon that was added as a GeoJsonSource.
 */
public class ClickOnLayerActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private static final String geoJsonSourceId = "geoJsonData";
  private static final String geoJsonLayerId = "polygonFillLayer";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_click_on_layer);

    Toast.makeText(ClickOnLayerActivity.this, R.string.click_on_polygon_toast_instruction,
      Toast.LENGTH_SHORT).show();

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    ClickOnLayerActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        mapboxMap.addOnMapClickListener(ClickOnLayerActivity.this);
        addGeoJsonSourceToMap(style);

        // Create FillLayer with GeoJSON source and add the FillLayer to the map
        if (style != null) {
          style.addLayer(new FillLayer(geoJsonLayerId, geoJsonSourceId)
            .withProperties(fillOpacity(0.5f)));
        }
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    PointF pointf = mapboxMap.getProjection().toScreenLocation(point);
    RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);
    List<Feature> featureList = mapboxMap.queryRenderedFeatures(rectF, geoJsonLayerId);
    if (featureList.size() > 0) {
      for (Feature feature : featureList) {
        Timber.d("Feature found with %1$s", feature.toJson());
        Toast.makeText(ClickOnLayerActivity.this, R.string.click_on_polygon_toast,
          Toast.LENGTH_SHORT).show();
      }
      return true;
    }
    return false;
  }

  private void addGeoJsonSourceToMap(@NonNull Style loadedMapStyle) {
    try {
      // Add GeoJsonSource to map
      loadedMapStyle.addSource(new GeoJsonSource(geoJsonSourceId, new URI("https://gist.githubusercontent"
        + ".com/tobrun/cf0d689c8187d42ebe62757f6d0cf137/raw/4d8ac3c8333f1517df9d303"
        + "d58f20f4a1d8841e8/regions.geojson")));
    } catch (Throwable throwable) {
      Timber.e("Couldn't add GeoJsonSource to map - %s", throwable);
    }
  }

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
