package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.io.InputStream;

import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Use the style API to highlight different types of data.
 * In this example, parks, hotels, and attractions are displayed.
 */
public class LosAngelesTourismActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private ValueAnimator parkColorAnimator;
  private ValueAnimator hotelColorAnimator;
  private ValueAnimator attractionsColorAnimator;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_los_angeles_tourism);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }


  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    // Add the hotels source to the map
    GeoJsonSource hotelSource = new GeoJsonSource("hotels", loadJsonFromAsset("la_hotels.geojson"));
    mapboxMap.addSource(hotelSource);

    FillLayer hotelLayer = new FillLayer("hotels", "hotels").withProperties(
      fillColor(Color.parseColor("#5a9fcf")),
      PropertyFactory.visibility(Property.NONE)
    );

    mapboxMap.addLayer(hotelLayer);

    final FillLayer hotels = (FillLayer) mapboxMap.getLayer("hotels");

    hotelColorAnimator = ValueAnimator.ofObject(
      new ArgbEvaluator(),
      Color.parseColor("#5a9fcf"), // Brighter shade
      Color.parseColor("#2C6B97") // Darker shade
    );
    hotelColorAnimator.setDuration(1000);
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

    // Add the attractions source to the map
    GeoJsonSource attractionsSource = new GeoJsonSource("attractions", loadJsonFromAsset("la_attractions.geojson"));
    mapboxMap.addSource(attractionsSource);

    CircleLayer attractionsLayer = new CircleLayer("attractions", "attractions").withProperties(
      circleColor(Color.parseColor("#5a9fcf")),
      PropertyFactory.visibility(Property.NONE)
    );

    mapboxMap.addLayer(attractionsLayer);

    final CircleLayer attractions = (CircleLayer) mapboxMap.getLayer("attractions");

    attractionsColorAnimator = ValueAnimator.ofObject(
      new ArgbEvaluator(),
      Color.parseColor("#ec8a8a"), // Brighter shade
      Color.parseColor("#de3232") // Darker shade
    );
    attractionsColorAnimator.setDuration(1000);
    attractionsColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
    attractionsColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
    attractionsColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {

        attractions.setProperties(
          circleColor((int) animator.getAnimatedValue())
        );
      }

    });

    final FillLayer parks = (FillLayer) mapboxMap.getLayer("parks");
    parks.setProperties(
      PropertyFactory.visibility(Property.NONE)
    );

    parkColorAnimator = ValueAnimator.ofObject(
      new ArgbEvaluator(),
      Color.parseColor("#7ac79c"), // Brighter shade
      Color.parseColor("#419a68") // Darker shade
    );
    parkColorAnimator.setDuration(1000);
    parkColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
    parkColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
    parkColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {

        parks.setProperties(
          fillColor((int) animator.getAnimatedValue())
        );
      }
    });

    FloatingActionButton toggleHotelsFab = (FloatingActionButton) findViewById(R.id.fab_toggle_hotels);
    toggleHotelsFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setLayerVisible("hotels");
      }
    });


    FloatingActionButton toggleParksFab = (FloatingActionButton) findViewById(R.id.fab_toggle_parks);
    toggleParksFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setLayerVisible("parks");
      }
    });

    FloatingActionButton toggleAttractionsFab = (FloatingActionButton) findViewById(R.id.fab_toggle_attractions);
    toggleAttractionsFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setLayerVisible("attractions");
      }
    });

    // Start all the animation at the same time so that they are in sync when displayed.
    parkColorAnimator.start();
    hotelColorAnimator.start();
    attractionsColorAnimator.start();

  }

  private void setLayerVisible(String layerId) {
    Layer layer = mapboxMap.getLayer(layerId);
    if (layer == null) {
      return;
    }
    if (VISIBLE.equals(layer.getVisibility().getValue())) {
      // Layer is visible
      layer.setProperties(
        PropertyFactory.visibility("none")
      );
    } else {
      // Layer isn't visible
      layer.setProperties(
        PropertyFactory.visibility("visible")
      );
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
    if (hotelColorAnimator != null) {
      hotelColorAnimator.start();
    }
    if (parkColorAnimator != null) {
      parkColorAnimator.start();
    }
    if (attractionsColorAnimator != null) {
      attractionsColorAnimator.start();
    }
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
    hotelColorAnimator.cancel();
    parkColorAnimator.cancel();
    attractionsColorAnimator.cancel();
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
