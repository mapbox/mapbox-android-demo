package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.google.gson.JsonObject;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.geojson.GeoJsonPlugin;
import com.mapbox.mapboxsdk.plugins.geojson.GeoJsonPluginBuilder;
import com.mapbox.mapboxsdk.plugins.geojson.listener.OnLoadingGeoJsonListener;
import com.mapbox.mapboxsdk.plugins.geojson.listener.OnMarkerEventListener;

import java.io.File;

import timber.log.Timber;

public class GeoJsonPluginActivity extends AppCompatActivity implements OnMapReadyCallback,
  OnLoadingGeoJsonListener, OnMarkerEventListener, FileChooserDialog.FileCallback {

  private CoordinatorLayout coordinatorLayout;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private GeoJsonPlugin geoJsonPlugin;
  private ProgressBar progressBar;
  private FloatingActionButton urlFab;
  private FloatingActionButton assetsFab;
  private FloatingActionButton pathFab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_geojson_plugin);
    setUpFabButtons();
    progressBar = (ProgressBar) findViewById(R.id.geoJSONLoadProgressBar);
    coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    GeoJsonPluginActivity.this.mapboxMap = mapboxMap;
    geoJsonPlugin = new GeoJsonPluginBuilder()
      .withContext(this)
      .withMap(mapboxMap)
      .withOnLoadingURL(this)
      .withOnLoadingFileAssets(this)
      .withOnLoadingFilePath(this)
      .withMarkerClickListener(this)
      .build();
  }

  private void setUpFabButtons() {
    urlFab = (FloatingActionButton) findViewById(R.id.fabURL);
    assetsFab = (FloatingActionButton) findViewById(R.id.fabAssets);
    pathFab = (FloatingActionButton) findViewById(R.id.fabPath);
    onUrlFabClick();
    onAssetsFabClick();
    onPathFabClick();
  }

  private void onUrlFabClick() {
    urlFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mapboxMap != null && geoJsonPlugin != null) {
          mapboxMap.clear();
          geoJsonPlugin.setUrl("https://raw.githubusercontent.com/johan/world.geo.json/master/countries/SEN.geo.json");
        }
      }
    });
  }

  private void onAssetsFabClick() {
    assetsFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mapboxMap != null && geoJsonPlugin != null) {
          mapboxMap.clear();
          geoJsonPlugin.setAssetsName("boston_police_stations.geojson");
        }
      }
    });
  }

  private void onPathFabClick() {
    pathFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (Build.VERSION.SDK_INT >= 23) {
          if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            Timber.v("Permission is granted");
            showFileChooserDialog();
            Toast.makeText(GeoJsonPluginActivity.this, R.string.find_file_instruction_toast,
              Toast.LENGTH_SHORT).show();
          } else {
            Timber.v("Permission is revoked");
            ActivityCompat.requestPermissions(GeoJsonPluginActivity.this,
              new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
          }
        } else { //permission is automatically granted on sdk<23 upon installation
          Timber.v("Permission is granted");
          showFileChooserDialog();
        }
      }
    });
  }

  /**
   * Draws GeoJSON file from a specific path. Please add and locate a GeoJSON file in your device to test it.
   *
   * @param file selected file from external storage
   */
  private void drawFromPath(File file) {
    String path = file.getAbsolutePath();
    if (mapboxMap != null && geoJsonPlugin != null) {
      mapboxMap.clear();
      geoJsonPlugin.setFilePath(path);
    }
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

  private void showFileChooserDialog() {
    new FileChooserDialog.Builder(this)
      .extensionsFilter(".geojson", ".json", ".js", ".txt")
      .goUpLabel("Up")
      .show();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Timber.v("Permission: " + permissions[0] + "was " + grantResults[0]);
      showFileChooserDialog();
    }
  }

  @Override
  public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
    drawFromPath(file);
  }

  @Override
  public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

  }

  @Override
  public void onPreLoading() {
    progressBar.setVisibility(View.VISIBLE);
  }

  @Override
  public void onLoaded() {
    Toast.makeText(this, "GeoJson data loaded", Toast.LENGTH_LONG).show();
    progressBar.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onLoadFailed(Exception exception) {
    progressBar.setVisibility(View.INVISIBLE);
    Toast.makeText(this, "Error occur during load GeoJson data. see logcat", Toast.LENGTH_LONG).show();
    exception.printStackTrace();
  }

  @Override
  public void onMarkerClickListener(Marker marker, JsonObject properties) {
    Snackbar.make(coordinatorLayout, properties.get("NAME").getAsString(), Snackbar.LENGTH_SHORT).show();
  }
}

