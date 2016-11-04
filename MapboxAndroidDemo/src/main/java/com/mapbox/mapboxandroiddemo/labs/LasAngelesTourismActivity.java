package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.io.InputStream;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class LasAngelesTourismActivity extends AppCompatActivity {

  private MapView mapView;
  private GeoJsonSource hotelSource;
  private float colorTint = 1;
  private ValueAnimator hotelColorAnimator;
  private ValueAnimator parkColorAnimator;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    MapboxAccountManager.start(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_lab_las_angeles_tourisim);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        hotelSource = new GeoJsonSource("hotels", loadJsonFromAsset("la_hotels.geojson"));
        mapboxMap.addSource(hotelSource);

        FillLayer hotelLayer = new FillLayer("hotels-layer", "hotels").withProperties(
          fillColor(Color.parseColor("#CB1BC3"))
        );

        mapboxMap.addLayer(hotelLayer);

        final FillLayer hotels = (FillLayer) mapboxMap.getLayer("hotels-layer");

        hotelColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#CB1BC3"), Color.parseColor("#74126F"));
        hotelColorAnimator.setDuration(1000); // milliseconds
        hotelColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        hotelColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        hotelColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

          @Override
          public void onAnimationUpdate(ValueAnimator animator) {

            hotels.setProperties(
              fillColor((int) animator.getAnimatedValue())
            );
          }

        });
        hotelColorAnimator.start();

        SymbolLayer hotelLabelsLayer = new SymbolLayer("hotels-label", "hotels");

        mapboxMap.addLayer(hotelLabelsLayer);





//        final FillLayer parks = (FillLayer) mapboxMap.getLayer("parks");
//
//        parkColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#20C73D"), Color.parseColor("#147725"));
//        parkColorAnimator.setDuration(1000); // milliseconds
//        parkColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        parkColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
//        parkColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//          @Override
//          public void onAnimationUpdate(ValueAnimator animator) {
//
//            parks.setProperties(
//              fillColor((int) animator.getAnimatedValue())
//            );
//          }
//
//        });
//        parkColorAnimator.start();




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
  public void onPause() {
    super.onPause();
    mapView.onPause();
    hotelColorAnimator.cancel();
    parkColorAnimator.cancel();
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

  private String loadJsonFromAsset(String filename) {
    // Using this method to load in GeoJSON files from the assets folder.

    try {
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");

    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
