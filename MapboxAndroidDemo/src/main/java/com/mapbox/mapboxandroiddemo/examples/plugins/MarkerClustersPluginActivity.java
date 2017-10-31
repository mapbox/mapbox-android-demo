package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.ClusterItem;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.ClusterManagerPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MarkerClustersPluginActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private ClusterManagerPlugin<MyItem> clusterManagerPlugin;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_marker_clusters_plugin);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        MarkerClustersPluginActivity.this.mapboxMap = mapboxMap;
        startDemo();
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

  protected void startDemo() {
    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.865539, 2.348603), 9),3500);

    clusterManagerPlugin = new ClusterManagerPlugin<>(this, mapboxMap);
    mapboxMap.addOnCameraIdleListener(clusterManagerPlugin);

    try {
      readItems();
    } catch (JSONException exception) {
      Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
    }
  }

  private void readItems() throws JSONException {
    InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
    List<MyItem> items = new MyItemReader().read(inputStream);
    clusterManagerPlugin.addItems(items);
  }

  public static class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public MyItem(double lat, double lng) {
      mPosition = new LatLng(lat, lng);
      mTitle = null;
      mSnippet = null;
    }

    public MyItem(double lat, double lng, String title, String snippet) {
      mPosition = new LatLng(lat, lng);
      mTitle = title;
      mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
      return mPosition;
    }

    @Override
    public String getTitle() {
      return mTitle;
    }

    @Override
    public String getSnippet() {
      return mSnippet;
    }

    public void setTitle(String title) {
      mTitle = title;
    }

    public void setSnippet(String snippet) {
      mSnippet = snippet;
    }
  }

  public static class MyItemReader {

    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";

    public List<MyItem> read(InputStream inputStream) throws JSONException {
      List<MyItem> items = new ArrayList<MyItem>();
      String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();
      JSONArray array = new JSONArray(json);
      for (int i = 0; i < array.length(); i++) {
        String title = null;
        String snippet = null;
        JSONObject object = array.getJSONObject(i);
        double lat = object.getDouble("latitude");
        double lng = object.getDouble("longitude");
        if (!object.isNull("name")) {
          title = object.getString("name");
        }
        if (!object.isNull("address")) {
          snippet = object.getString("address");
        }
        items.add(new MyItem(lat, lng, title, snippet));
      }
      return items;
    }
  }
}