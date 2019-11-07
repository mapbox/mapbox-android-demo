package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import android.graphics.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Aggregate and compute data for each cluster based on underlying feature attributes..
 */
public class AggregatedClusteringDataActivity extends AppCompatActivity {

    private static final double CAMERA_ZOOM_DELTA = 0.01;
    private MapView mapView;
    private MapboxMap mapboxMap;

    private GeoJsonSource clusterSource;

    private int clickOptionCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_aggregated_clustering_data);

        mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap map) {

                mapboxMap = map;

                map.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                36.2048, 138.2529), 2));

                        addClusteredGeoJsonSource(style);
                        style.addImage(
                                "cross-icon-id",
                                BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_cross)),
                                true
                        );

                        Toast.makeText(AggregatedClusteringDataActivity.this, R.string.tap_on_cluster_instructions,
                                Toast.LENGTH_SHORT).show();
                    }
                });

                mapboxMap.addOnMapClickListener(latLng -> {
                    PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
                    List<Feature> features = mapboxMap.queryRenderedFeatures(point, "cluster-0", "cluster-1", "cluster-2");
                    if (!features.isEmpty()) {
                        onClusterClick(features.get(0), new Point((int) point.x, (int) point.y));
                    }
                    return true;
                });

            }
        });
    }

    private void onClusterClick(Feature cluster, Point clickPoint) {
        FeatureCollection collection = clusterSource.getClusterLeaves(cluster, 100, 0);
        List<Feature> features = collection.features();
        Double runningSum = 0.0;
        for (int i = 0; i < features.size(); i++) {
            // Get the magnitude of each earthquake in the cluster to add to a running sum.
            runningSum += features.get(i).properties().get("mag").getAsDouble();
        }
        Toast.makeText(this, "Average magnitude: " + runningSum/features.size(), Toast.LENGTH_SHORT).show();
    }

    private void updateClickOptionCounter() {
        if (clickOptionCounter == 2) {
            clickOptionCounter = 0;
        } else {
            clickOptionCounter++;
        }
    }

    // Create the GeoJsonSource for the clustering layer.
    private GeoJsonSource createClusterSource() throws URISyntaxException {
        return new GeoJsonSource("earthquakes",
                new URI("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
                new GeoJsonOptions()
                        .withCluster(true)
                        .withClusterMaxZoom(14)
                        .withClusterRadius(50)
        );
    }

    @Override
    public void onStart() {
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
    public void onStop() {
        super.onStop();
        mapView.onStop();
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


    private void addClusteredGeoJsonSource(@NonNull Style loadedMapStyle) {

        // Add a new source from the GeoJSON data and set the 'cluster' option to true.
        try {
            loadedMapStyle.addSource(
                    clusterSource = createClusterSource()
            );
        } catch (URISyntaxException uriSyntaxException) {
            Timber.e("Check the URL %s", uriSyntaxException.getMessage());
        }

        //Creating a marker layer for single data points
        SymbolLayer unclustered = new SymbolLayer("unclustered-points", "earthquakes");

        unclustered.setProperties(
                iconImage("cross-icon-id"),
                iconSize(
                        division(
                                get("mag"), literal(4.0f)
                        )
                ),
                iconColor(
                        interpolate(exponential(1), get("mag"),
                                stop(2.0, rgb(0, 255, 0)),
                                stop(4.5, rgb(0, 0, 255)),
                                stop(7.0, rgb(255, 0, 0))
                        )
                )
        );
        unclustered.setFilter(has("mag"));
        loadedMapStyle.addLayer(unclustered);

        // Use the earthquakes GeoJSON source to create three layers: One layer for each cluster category.
        // Each point range gets a different fill color.
        int[][] layers = new int[][] {
                new int[] {150, ContextCompat.getColor(this, R.color.mapboxRed)},
                new int[] {20, ContextCompat.getColor(this, R.color.mapboxGreen)},
                new int[] {0, ContextCompat.getColor(this, R.color.mapbox_blue)}
        };

        for (int i = 0; i < layers.length; i++) {
            // Add clusters' circles
            CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(18f)
            );

            Expression pointCount = toNumber(get("point_count"));

            // Add a filter to the cluster layer that hides the circles based on "point_count"
            circles.setFilter(
                    i == 0
                            ? all(has("point_count"),
                            gte(pointCount, literal(layers[i][0]))
                    ) : all(has("point_count"),
                            gte(pointCount, literal(layers[i][0])),
                            lt(pointCount, literal(layers[i - 1][0]))
                    )
            );
            loadedMapStyle.addLayer(circles);
        }

        // Add the count labels
        SymbolLayer count = new SymbolLayer("count", "earthquakes");
        count.setProperties(
                textField(Expression.toString(get("point_count"))),
                textSize(12f),
                textColor(Color.WHITE),
                textIgnorePlacement(true),
                textAllowOverlap(true)
        );
        loadedMapStyle.addLayer(count);

    }
}
