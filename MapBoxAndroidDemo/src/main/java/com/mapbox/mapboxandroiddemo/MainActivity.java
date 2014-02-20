package com.mapbox.mapboxandroiddemo;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.util.Log;
import android.view.Window;
import android.widget.Spinner;
import android.widget.CheckBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import java.util.List;
import java.util.ArrayList;
import android.widget.ArrayAdapter;

public class MainActivity extends ActionBarActivity {

    private MapController mapController;
    private LatLng startingPoint = new LatLng(51f, 0f);
    private MapView mv;
    private UserLocationOverlay myLocationOverlay;
    private PathOverlay equator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mv = (MapView)findViewById(R.id.mapview);
        mapController = mv.getController();
        mv.setCenter(startingPoint).setZoom(4);

        mv.loadFromGeoJSONURL("https://gist.github.com/fdansv/8541618/raw/09da8aef983c8ffeb814d0a1baa8ecf563555b5d/geojsonpointtest");
        Marker m = new Marker(mv, "Hello", "World", new LatLng(0f, 0f));
        m.setIcon(new Icon(Icon.Size.l, "bus", "000"));
        mv.addMarker(m);

        Spinner layerspinner = (Spinner) findViewById(R.id.layerspinner);
        List<String> list = new ArrayList<String>();
        list.add("Satellite");
        list.add("Streets");
        list.add("Terrain");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, list);
        layerspinner.setAdapter(dataAdapter);
        layerspinner.setOnItemSelectedListener(new LayerSelectedListener());

        final CheckBox layerSpinner = (CheckBox) findViewById(R.id.locationCheckBox);
        layerSpinner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layerSpinner.isChecked()) {
                    addLocationOverlay();
                } else {
                    removeLocationOverlay();
                }
            }
        });

        mv.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }
        });
        mv.setVisibility(View.VISIBLE);
        equator = new PathOverlay();
        equator.addPoint(0,-89);
        equator.addPoint(0, 89);
        mv.getOverlays().add(equator);
    }

    class LayerSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String label = (String) parent.getItemAtPosition(pos);
            Log.i(TAG, label);
            if (label.equals("Satellite")) {
                mv.setTileSource(new MapboxTileLayer("brunosan.map-cyglrrfu"));
            } else if (label.equals("Streets")) {
                mv.setTileSource(new MapboxTileLayer("examples.map-vyofok3q"));
            } else if (label.equals("Terrain")) {
                mv.setTileSource(new MapboxTileLayer("examples.map-zgrqqx0w"));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private void addLocationOverlay() {
        if (myLocationOverlay == null) {
            // Adds an icon that shows location
            myLocationOverlay = new UserLocationOverlay(this, mv);
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.setDrawAccuracyEnabled(true);
            mv.getOverlays().add(myLocationOverlay);
        }
    }

    private void removeLocationOverlay() {
        if (myLocationOverlay != null) {
            mv.getOverlays().remove(myLocationOverlay);
            myLocationOverlay = null;
        }
    }

    private void addLine() {
        // Configures a line
        PathOverlay po = new PathOverlay(Color.RED, this);
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(5);
        po.setPaint(linePaint);
        po.addPoint(startingPoint);
        po.addPoint(new LatLng(51.7, 0.3));
        po.addPoint(new LatLng(51.2, 0));

        // Adds line and marker to the overlay
        mv.getOverlays().add(po);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return true;
    }

    private final String TAG = "Mapbox Demo";
}
