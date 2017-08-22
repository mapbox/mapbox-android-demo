package com.mapbox.mapboxandroiddemo.examples.mas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directionsmatrix.v1.MapboxDirectionsMatrix;
import com.mapbox.services.api.directionsmatrix.v1.models.DirectionsMatrixResponse;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DirectionsMatrixApiActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_matrix_api);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        DirectionsMatrixApiActivity.this.mapboxMap = mapboxMap;

        // Build the call to the Directions Matrix API
        MapboxDirectionsMatrix directionsMatrixClient = new MapboxDirectionsMatrix.Builder()
          .setAccessToken(getString(R.string.access_token))
          .setProfile(DirectionsCriteria.PROFILE_DRIVING)
          .setCoordinates(positions)
          .build();

        directionsMatrixClient.enqueueCall(new Callback<DirectionsMatrixResponse>() {
          @Override
          public void onResponse(Call<DirectionsMatrixResponse> call, Response<DirectionsMatrixResponse> response) {


          }

          @Override
          public void onFailure(Call<DirectionsMatrixResponse> call, Throwable throwable) {
            Toast.makeText(DirectionsMatrixApiActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();
          }
        });

      }
    });
  }

  private void getMcDonaldsLocations() {
    MapboxGeocoding mapboxGeocoding = new MapboxGeocoding.Builder()
      .setAccessToken(Mapbox.getAccessToken())
      .setLocation("1600 Pennsylvania Ave NW")
      .build();

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
