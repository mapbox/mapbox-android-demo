package com.mapbox.mapboxandroiddemo.examples;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class CustomInfoWindowActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation_custom_info_window);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                IconFactory iconFactory = IconFactory.getInstance(CustomInfoWindowActivity.this);
                Drawable iconDrawable = ContextCompat.getDrawable(CustomInfoWindowActivity.this, R.drawable.purple_marker);
                Icon icon = iconFactory.fromDrawable(iconDrawable);

                Marker marker = mapboxMap.addMarker(new MarkerViewOptions()
                        .position(new LatLng(40.73581, -73.99155))
                        .title("Tonga")
                        .icon(icon));

                mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
                    @Nullable
                    @Override
                    public View getInfoWindow(@NonNull Marker marker) {

                        LinearLayout parent = new LinearLayout(CustomInfoWindowActivity.this);

                        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        parent.setOrientation(LinearLayout.VERTICAL);

                        TextView textView = new TextView(CustomInfoWindowActivity.this);
                        textView.setText(marker.getTitle());
                        textView.setTextColor(Color.BLACK);
                        textView.setBackgroundColor(Color.WHITE);
                        textView.setPadding(10, 10, 10, 10);

                        ImageView countryFlagImage = new ImageView(CustomInfoWindowActivity.this);
                        countryFlagImage.setImageDrawable(ContextCompat.getDrawable(CustomInfoWindowActivity.this, R.drawable.flag_of_tonga));
                        countryFlagImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(150, 100));
                        countryFlagImage.setBackgroundColor(Color.WHITE);
                        countryFlagImage.setPadding(10, 10, 10, 10);

                        parent.addView(countryFlagImage);
                        parent.addView(textView);

                        return parent;
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

}
