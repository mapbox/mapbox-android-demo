package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.TileSet;

/**
 * Adding an external Web Map Service layer to the map.
 */
public class AddWmsSourceActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_add_wms_source);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Add the web map source to the map
            style.addSource(new RasterSource(
              "web-map-source",
              new TileSet("tileset", "https://img.nj.gov/imagerywms/Natural2015?bbox={"
                + "bbox-epsg-3857}&format=image/png&service=WMS&version=1.1.1&request=GetMap&srs=EPSG:"
                + "3857&transparent=true&width=256&height=256&layers=Natural2015"), 256));

            // Create a RasterLayer with the source created above and then add the layer to the map
            if (style.getLayer("tunnel-street-minor-low") != null) {
              style.addLayerBelow(
                new RasterLayer("web-map-layer", "web-map-source"), "tunnel-street-minor-low");
            } else {
              style.addLayer(new RasterLayer("web-map-layer", "web-map-source"));
            }
          }
        });
      }
    });
  }

  @Override
  protected void onResume() {
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
  protected void onPause() {
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
