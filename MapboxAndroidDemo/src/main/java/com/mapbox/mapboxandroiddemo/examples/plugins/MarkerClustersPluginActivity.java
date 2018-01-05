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

/**
 * Use the marker cluster plugin to automatically add colored circles with numbers, so that a user knows
 * how many markers are in a certain area at a higher zoom level.
 */
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
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.865539, 2.348603), 10.8),2800);

        // Initializing the cluster plugin
        clusterManagerPlugin = new ClusterManagerPlugin<>(MarkerClustersPluginActivity.this, mapboxMap);
        initCameraListener();
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

  protected void initCameraListener() {
    mapboxMap.addOnCameraIdleListener(clusterManagerPlugin);
    try {
      addItemsToClusterPlugin(R.raw.paris_bike_share_hubs);
    } catch (JSONException exception) {
      Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
    }
  }

  private void addItemsToClusterPlugin(int rawResourceFile) throws JSONException {
    InputStream inputStream = getResources().openRawResource(rawResourceFile);
    List<MyItem> items = new MyItemReader().read(inputStream);
    clusterManagerPlugin.addItems(items);
  }

  /**
   * Custom class for use by the marker cluster plugin
   */
  public static class MyItem implements ClusterItem {
    private final LatLng position;
    private String title;
    private String snippet;

    public MyItem(double lat, double lng) {
      position = new LatLng(lat, lng);
      title = null;
      snippet = null;
    }

    public MyItem(double lat, double lng, String title, String snippet) {
      position = new LatLng(lat, lng);
      this.title = title;
      this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
      return position;
    }

    @Override
    public String getTitle() {
      return title;
    }

    @Override
    public String getSnippet() {
      return snippet;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public void setSnippet(String snippet) {
      this.snippet = snippet;
    }
  }

  /**
   * Custom class which reads JSON data and creates a list of MyItem objects
   */
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