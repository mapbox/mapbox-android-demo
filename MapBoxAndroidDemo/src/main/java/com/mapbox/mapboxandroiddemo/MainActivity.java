package com.mapbox.mapboxandroiddemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.crashlytics.android.Crashlytics;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.*;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

public class MainActivity extends ActionBarActivity {

    private MapController mapController;
    private LatLng startingPoint = new LatLng(51f, 0f);
    private MapView mv;
    private UserLocationOverlay myLocationOverlay;
    private String satellite = "brunosan.map-cyglrrfu";
    private String street = "examples.map-vyofok3q";
    private String terrain = "examples.map-zgrqqx0w";
    private String currentLayer = "satellite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_main);
        mv = (MapView) findViewById(R.id.mapview);

        mapController = mv.getController();
        replaceMapView(satellite);
        addLocationOverlay();

        mv.loadFromGeoJSONURL("https://gist.githubusercontent.com/tmcw/10307131/raw/21c0a20312a2833afeee3b46028c3ed0e9756d4c/map.geojson");
        setButtonListeners();
        Marker m = new Marker(mv, "Edinburgh", "Scotland", new LatLng(55.94629, -3.20777));
        m.setIcon(new Icon(this, Icon.Size.SMALL, "marker-stroked", "FF0000"));
        mv.addMarker(m);

        m = new Marker(mv, "Stockholm", "Sweden", new LatLng(59.32995, 18.06461));
        m.setIcon(new Icon(this, Icon.Size.MEDIUM, "city", "FFFF00"));
        mv.addMarker(m);

        m = new Marker(mv, "Prague", "Czech Republic", new LatLng(50.08734, 14.42112));
        m.setIcon(new Icon(this, Icon.Size.LARGE, "land-use", "00FFFF"));
        mv.addMarker(m);

        m = new Marker(mv, "Prague2", "Czech Republic", new LatLng(50.0875, 14.42112));
        m.setIcon(new Icon(getBaseContext(), Icon.Size.LARGE, "land-use", "00FF00"));
        mv.addMarker(m);

        m = new Marker(mv, "Athens", "Greece", new LatLng(37.97885, 23.71399));
        mv.addMarker(m);

        mv.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }

            @Override
            public boolean onTilesLoadStarted() {
                // TODO Auto-generated method stub
                return false;
            }
        });
        mv.setVisibility(View.VISIBLE);
    }

    private void setButtonListeners() {
        Button satBut = changeButtonTypeface((Button) findViewById(R.id.satbut));
        satBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentLayer.equals("satellite")) {
                    replaceMapView(satellite);
                    currentLayer = "satellite";
                }
            }
        });
        Button terBut = changeButtonTypeface((Button) findViewById(R.id.terbut));
        terBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentLayer.equals("terrain")) {
                    replaceMapView(terrain);
                    currentLayer = "terrain";
                }
            }
        });
        Button strBut = changeButtonTypeface((Button) findViewById(R.id.strbut));
        strBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentLayer.equals("street")) {
                    replaceMapView(street);
                    currentLayer = "street";
                }
            }
        });
        Button bugsBut = changeButtonTypeface((Button) findViewById(R.id.bugsButton));
        bugsBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/mapbox/mapbox-android-sdk/issues?state=open";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }


    protected void replaceMapView(String layer) {
        ITileLayer source;
        BoundingBox box;

        source = new MapboxTileLayer(layer);

        mv.setTileSource(source);
        box = source.getBoundingBox();
        mv.setScrollableAreaLimit(box);
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(mv.getTileProvider().getCenterCoordinate());
        mv.setZoom(0);
    }

    private void addLocationOverlay() {
        // Adds an icon that shows location
        myLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(this), mv);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mv.getOverlays().add(myLocationOverlay);
    }

    private Button changeButtonTypeface(Button button) {
        return button;
    }

    public LatLng getMapCenter() {
        return mv.getCenter();
    }

    public void setMapCenter(ILatLng center) {
        mv.setCenter(center);
    }

    /**
     * Method to show settings  in alert dialog
     * On pressing Settings button will lauch Settings Options - GPS
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getBaseContext());

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getBaseContext().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}