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

public class LandUseStyling extends AppCompatActivity {
    private static final String TAG = LandUseStyling.class.getSimpleName();

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

                FillLayer industrialLayer = new FillLayer("industrial-layer", "composite");
                industrialLayer.setSourceLayer("landuse");
                industrialLayer.setProperties(
                        fillColor(Color.parseColor("#eceeed"))
                );
                industrialLayer.setFilter(eq("class", "industrial"));
                mapboxMap.addLayer(industrialLayer);

                FloatingActionButton toggleParks = (FloatingActionButton) findViewById(R.id.fab_toggle_parks);
                toggleParks.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FillLayer parks = mapboxMap.getLayerAs("parks");
                        if (parks != null) {

                            if (!colorsEqual(parks.getFillColorAsInt(), ContextCompat.getColor(LandUseStyling.this, R.color.mapboxGreen))) {
                                parks.setProperties(
                                        fillColor(ContextCompat.getColor(LandUseStyling.this, R.color.mapboxGreen))
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
                            if (!colorsEqual(schools.getFillColorAsInt(), ContextCompat.getColor(LandUseStyling.this, R.color.mapboxYellow))) {
                                schools.setProperties(
                                        fillColor(ContextCompat.getColor(LandUseStyling.this, R.color.mapboxYellow))
                                );
                            } else {
                                schools.setProperties(
                                        fillColor(Color.parseColor("#f6f6f4"))
                                );
                            }
                        }
                    }
                });

                FloatingActionButton toggleIndustrial = (FloatingActionButton) findViewById(R.id.fab_toggle_industry);
                toggleIndustrial.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FillLayer industrial = mapboxMap.getLayerAs("industrial-layer");
                        if (industrial != null) {
                            if (!colorsEqual(industrial.getFillColorAsInt(), ContextCompat.getColor(LandUseStyling.this, R.color.mapboxPurple))) {
                                industrial.setProperties(
                                        fillColor(ContextCompat.getColor(LandUseStyling.this, R.color.mapboxPurple))
                                );
                            } else {
                                industrial.setProperties(
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
        Log.i(TAG, String.format("Comparing colors: %s, %s (%s, %s > %s)", a, b, a / 10, b / 10, a / 10 == b / 10));
        return a / 10 == b / 10;
    }
}
