package com.mapbox.mapboxandroiddemo.examples;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class BasicViewAnnotationActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_view_annotation);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                //TODO delete icon stuff
                IconFactory iconFactory = IconFactory.getInstance(BasicViewAnnotationActivity.this);
                Drawable iconDrawable = ContextCompat.getDrawable(BasicViewAnnotationActivity.this, R.drawable.purple_marker);
                Icon icon = iconFactory.fromDrawable(iconDrawable);

                IconFactory redIconFactory = IconFactory.getInstance(BasicViewAnnotationActivity.this);
                Drawable redIconDrawable = ContextCompat.getDrawable(BasicViewAnnotationActivity.this, R.drawable.default_marker);
                Icon redIcon = redIconFactory.fromDrawable(redIconDrawable);

                // The easiest way to add a view annotation
                mapboxMap.addMarker(new MarkerViewOptions()
                        .position(new LatLng(-37.821629, 144.978535))
                        .anchor(0.5f, 0.5f) //TODO remove anchor and icon
                        .icon(redIcon));

                // View annotation using all the different options available
                mapboxMap.addMarker(new MarkerViewOptions()
                        .position(new LatLng(-37.822829, 144.981842))
                        .icon(icon)
                        .rotation(90)
                        .anchor(0.5f, 0.5f)
                        // TODO add alpha()
                        .title("Hisense Arena")
                        .snippet("Olympic Blvd, Melbourne VIC 3001, Australia")
                        .infoWindowAnchor(0.5f, 0.5f)
                        .flat(true));
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
}
