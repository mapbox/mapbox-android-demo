package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;

import static com.mapbox.mapboxsdk.style.layers.Filter.eq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class LandUseStylingActivity extends AppCompatActivity {
  private static final String TAG = LandUseStylingActivity.class.getSimpleName();

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lab_land_use_styling);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        FillLayer schoolLayer = new FillLayer("school-layer", "composite");
        schoolLayer.setSourceLayer("landuse");
        schoolLayer.setProperties(
            fillColor(Color.parseColor("#f6f6f4"))
        );
        schoolLayer.setFilter(eq("class", "school"));
        mapboxMap.addLayer(schoolLayer);

        FillLayer hospitalLayer = new FillLayer("hospital-layer", "composite");
        hospitalLayer.setSourceLayer("landuse");
        hospitalLayer.setProperties(
            fillColor(Color.parseColor("#eceeed"))
        );
        hospitalLayer.setFilter(eq("class", "hospital"));
        mapboxMap.addLayer(hospitalLayer);

        FloatingActionButton toggleParks = (FloatingActionButton) findViewById(R.id.fab_toggle_parks);
        toggleParks.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FillLayer parks = mapboxMap.getLayerAs("parks");
            if (parks != null) {

              if (!colorsEqual(parks.getFillColorAsInt(), ContextCompat.getColor(LandUseStylingActivity.this, R.color.mapboxGreen))) {
                parks.setProperties(
                    fillColor(ContextCompat.getColor(LandUseStylingActivity.this, R.color.mapboxGreen))
                );
              } else {
                parks.setProperties(
                    fillColor(Color.parseColor("#eceeed"))
                );
              }
            }
          }
        });

        FloatingActionButton toggleSchools = (FloatingActionButton) findViewById(R.id.fab_toggle_schools);
        toggleSchools.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FillLayer schools = mapboxMap.getLayerAs("school-layer");
            if (schools != null) {
              if (schools.getFillColor().isValue() &&
                  !colorsEqual(schools.getFillColorAsInt(), ContextCompat.getColor(LandUseStylingActivity.this, R.color.mapboxYellow))) {
                schools.setProperties(
                    fillColor(ContextCompat.getColor(LandUseStylingActivity.this, R.color.mapboxYellow))
                );
              } else {
                schools.setProperties(
                    fillColor(Color.parseColor("#f6f6f4"))
                );
              }
            }
          }
        });

        FloatingActionButton toggleHospital = (FloatingActionButton) findViewById(R.id.fab_toggle_hospital);
        toggleHospital.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FillLayer hospital = mapboxMap.getLayerAs("hospital-layer");
            if (hospital != null) {
              if (hospital.getFillColor().isValue() &&
                  !colorsEqual(hospital.getFillColorAsInt(), ContextCompat.getColor(LandUseStylingActivity.this, R.color.mapboxPurple))) {
                hospital.setProperties(
                    fillColor(ContextCompat.getColor(LandUseStylingActivity.this, R.color.mapboxPurple))
                );
              } else {
                hospital.setProperties(
                    fillColor(Color.parseColor("#eceeed"))
                );
              }
            }
          }
        });

      }
    });
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
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private boolean colorsEqual(int a, int b) {
    boolean equal = Math.abs(Color.red(a) - Color.red(b)) <= 10 &&
        Math.abs(Color.green(a) - Color.green(b)) <= 10 &&
        Math.abs(Color.blue(a) - Color.blue(b)) <= 10;

    Log.i(TAG, String.format("Comparing colors: %s, %s (%s, %s ,%s => %s)",
        a,
        b,
        Math.abs(Color.red(a) - Color.red(b)),
        Math.abs(Color.green(a) - Color.green(b)),
        Math.abs(Color.blue(a) - Color.blue(b)),
        equal)
    );
    return equal;
  }
}
