package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.IntervalStops;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionBase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

public class PopulationDensityExtrusionActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private boolean mapIsTilted = false;
    private boolean roadsDisplayed = true;
    private boolean labelsDisplayed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_population_density_extrusion);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap map) {
                mapboxMap = map;
                setUpFabs();
                setUpFillsLayer();
                setUpExtrusionsLayer();
            }
        });
    }

    private void setUpFillsLayer() {
        FillLayer fillsLayer = new FillLayer("fills", "population");
        fillsLayer.withSourceLayer("outgeojson");
        fillsLayer.setFilter(Filter.lt("pkm2", 300000));
        fillsLayer.withProperties(
                fillColor(Function.property("pkm2", IntervalStops.interval(
                        stop(0, fillColor(Color.parseColor("#160e23"))),
                        stop(14500, fillColor(Color.parseColor("#00617f"))),
                        stop(145000, fillColor(Color.parseColor("#55e9ff")))))),
                fillOpacity(1f)
        );

        mapboxMap.addLayer(fillsLayer);
    }

    private void setUpExtrusionsLayer() {
        FillLayer fillExtrusionLayer = new FillLayer("extrusions", "population");
        fillExtrusionLayer.withSourceLayer("outgeojson");
        fillExtrusionLayer.setFilter(Filter.gt("p", 1));
        fillExtrusionLayer.setFilter(Filter.lt("pkm2", 300000));
        fillExtrusionLayer.withProperties(
                fillColor(Function.property("population", IntervalStops.interval(
                        stop(0, fillColor(Color.parseColor("#160e23"))),
                        stop(14500, fillColor(Color.parseColor("#00617f"))),
                        stop(145000, fillColor(Color.parseColor("#55e9ff")))))),
                fillExtrusionBase(0f),
                // TODO: Finish height stops below
                fillExtrusionHeight(Function.property("pkm2", IntervalStops.interval(
                        stop(0, fillExtrusionHeight(0f)),
                        stop(1450000, fillExtrusionHeight(20000f))))),
                fillOpacity(0f));
        mapboxMap.addLayer(fillExtrusionLayer);
    }

    // TODO: Create radiusHighlight, highlightedFill, and highlighted extrusion layers

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void setUpFabs() {

        FloatingActionButton tiltMapToggleButton = (FloatingActionButton) findViewById(R.id.fab_tilt_toggle);
        tiltMapToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mapboxMap != null) {
                    toggleMapTilt();
                }
            }
        });

        FloatingActionButton roadsToggleButton = (FloatingActionButton) findViewById(R.id.fab_road_toggle);
        roadsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapboxMap != null) {
                    roadsAreVisible();
                }
            }
        });

        FloatingActionButton labelsToggleButton = (FloatingActionButton) findViewById(R.id.fab_label_toggle);
        labelsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mapboxMap != null) {
                    labelsAreVisible();
                }
            }
        });
    }

    private void toggleMapTilt() {
        if (!mapIsTilted) {
            CameraPosition position = new CameraPosition.Builder()
                    .tilt(50) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 500);
            mapIsTilted = true;

        } else {
            CameraPosition position = new CameraPosition.Builder()
                    .tilt(0) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 500);
            mapIsTilted = false;
        }
    }

    private void roadsAreVisible() {
        if (!roadsDisplayed) {
            for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
                if (mapboxMap.getLayers().get(x).getId().contains("road")) {
                    mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("visible"));
                }
            }
            roadsDisplayed = true;
        } else {
            for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
                if (mapboxMap.getLayers().get(x).getId().contains("road")) {
                    mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("none"));
                }
            }
            roadsDisplayed = false;
        }
    }

    private void labelsAreVisible() {
        if (!labelsDisplayed) {
            for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
                if (mapboxMap.getLayers().get(x).getId().contains("label")
                        || mapboxMap.getLayers().get(x).getId().contains("poi_label_3")
                        || mapboxMap.getLayers().get(x).getId().contains("place")) {
                    mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("visible"));
                }
            }
            labelsDisplayed = true;
        } else {
            for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
                if (mapboxMap.getLayers().get(x).getId().contains("label")
                        || mapboxMap.getLayers().get(x).getId().contains("poi_label_3")
                        || mapboxMap.getLayers().get(x).getId().contains("place")) {
                    mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("none"));
                }
            }
            labelsDisplayed = false;
        }
    }
}
