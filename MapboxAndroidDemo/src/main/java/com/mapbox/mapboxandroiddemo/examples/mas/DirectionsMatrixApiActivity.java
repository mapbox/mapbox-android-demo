package com.mapbox.mapboxandroiddemo.examples.mas;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.models.Position;

import java.io.InputStream;
import java.util.List;

public class DirectionsMatrixApiActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private List<Position> positionList;
  private FeatureCollection featureCollection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_matrix_api);

    featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("boston_charge_stations.geojson"));

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {
        DirectionsMatrixApiActivity.this.mapboxMap = mapboxMap;

        addMarkers();

        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
          @Override
          public boolean onMarkerClick(@NonNull Marker clickedMarker) {

            for (Marker eachMarker : mapboxMap.getMarkers()) {
              if (clickedMarker != eachMarker) {
                eachMarker.showInfoWindow(mapboxMap, mapView);
              }
            }

            return false;
          }
        });


        /*// Build the call to the Directions Matrix API
        MapboxDirectionsMatrix directionsMatrixClient = new MapboxDirectionsMatrix.Builder()
          .setAccessToken(getString(R.string.access_token))
          .setProfile(DirectionsCriteria.PROFILE_DRIVING)
          .setCoordinates(positionList)
          .build();

        // Handle the API response
        directionsMatrixClient.enqueueCall(new Callback<DirectionsMatrixResponse>() {
          @Override
          public void onResponse(Call<DirectionsMatrixResponse> call, Response<DirectionsMatrixResponse> response) {


          }

          @Override
          public void onFailure(Call<DirectionsMatrixResponse> call, Throwable throwable) {
            Toast.makeText(DirectionsMatrixApiActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();
          }
        });*/

      }
    });
  }

  private void addMarkers() {
    Icon icon = IconFactory.getInstance(DirectionsMatrixApiActivity.this).fromResource(R.drawable.fastcharge_icon);
    for (Feature feature : featureCollection.getFeatures()) {
      mapboxMap.addMarker(new MarkerViewOptions()
        .position(new LatLng(feature.getProperty("Latitude").getAsDouble(),
          feature.getProperty("Longitude").getAsDouble()))
        .snippet(feature.getStringProperty("Station_Name"))
        .icon(icon));
    }
  }

  private String loadGeoJsonFromAsset(String filename) {
    try {
      // Load GeoJSON file from local asset folder
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");
    } catch (Exception exception) {
      Log.e("MapActivity", "Exception Loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
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
}
