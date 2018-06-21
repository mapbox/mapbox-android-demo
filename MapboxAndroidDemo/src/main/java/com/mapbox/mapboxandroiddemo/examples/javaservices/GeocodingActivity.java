package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Use the Mapbox Geocoding API to retrieve various information about a set of coordinates.
 */
public class GeocodingActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Button startGeocodeButton;
  private Button chooseCityButton;
  private Button mapCenterButton;
  private EditText latEditText;
  private EditText longEditText;
  private TextView geocodeResultTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_geocoding);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    initTextViews();
    initButtons();
  }

  private void initTextViews() {
    latEditText = findViewById(R.id.geocode_latitude_editText);
    longEditText = findViewById(R.id.geocode_longitude_editText);
    geocodeResultTextView = findViewById(R.id.geocode_result_message);
  }

  private void initButtons() {
    mapCenterButton = findViewById(R.id.map_center_button);
    startGeocodeButton = findViewById(R.id.start_geocode_button);
    chooseCityButton = findViewById(R.id.choose_city_spinner_button);
    startGeocodeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Make sure the EditTexts aren't empty
        if (TextUtils.isEmpty(latEditText.getText().toString())) {
          latEditText.setError(getString(R.string.fill_in_a_value));
        } else if (TextUtils.isEmpty(longEditText.getText().toString())) {
          longEditText.setError(getString(R.string.fill_in_a_value));
        } else {
          if (latCoordinateIsValid(Double.valueOf(latEditText.getText().toString()))
            && longCoordinateIsValid(Double.valueOf(longEditText.getText().toString()))) {
            // Make a geocoding search with the values inputted into the EditTexts
            makeGeocodeSearch(new LatLng(Double.valueOf(latEditText.getText().toString()),
              Double.valueOf(longEditText.getText().toString())));
          } else {
            Toast.makeText(GeocodingActivity.this, R.string.make_valid_lat, Toast.LENGTH_LONG).show();
          }
        }
      }
    });
    chooseCityButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showCityListMenu();
      }
    });
    mapCenterButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Get the map's target
        LatLng target = mapboxMap.getCameraPosition().target;

        // Fill the coordinate EditTexts with the target's coordinates
        setCoordinateEditTexts(target);

        // Make a geocoding search with the target's coordinates
        makeGeocodeSearch(target);

      }
    });
  }

  private boolean latCoordinateIsValid(double value) {
    return value >= -90 && value <= 90;
  }

  private boolean longCoordinateIsValid(double value) {
    return value >= -180 && value <= 180;
  }

  private void setCoordinateEditTexts(LatLng latLng) {
    latEditText.setText(String.valueOf(latLng.getLatitude()));
    longEditText.setText(String.valueOf(latLng.getLongitude()));
  }

  private void showCityListMenu() {
    List<String> modes = new ArrayList<>();
    modes.add("Vancouver");
    modes.add("Helsinki");
    modes.add("Lima");
    modes.add("Osaka");
    ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, modes);
    ListPopupWindow listPopup = new ListPopupWindow(this);
    listPopup.setAdapter(profileAdapter);
    listPopup.setAnchorView(chooseCityButton);
    listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
      LatLng cityLatLng = new LatLng();
      if (position == 0) {
        // Vancouver
        cityLatLng = new LatLng(49.2827, -123.1207);
        setCoordinateEditTexts(cityLatLng);
      } else if (position == 1) {
        // Helsinki
        cityLatLng = new LatLng(60.1698, 24.938);
        setCoordinateEditTexts(cityLatLng);
      } else if (position == 2) {
        // Lima
        cityLatLng = new LatLng(-12.0463, -77.0427);
        setCoordinateEditTexts(cityLatLng);
      } else if (position == 3) {
        // Osaka
        cityLatLng = new LatLng(34.693, 135.5021);
        setCoordinateEditTexts(cityLatLng);
      }
      animateCameraToNewPosition(cityLatLng);
      makeGeocodeSearch(cityLatLng);
      listPopup.dismiss();
    });
    listPopup.show();
  }

  private void makeGeocodeSearch(LatLng latLng) {
    try {
      // Build a Mapbox geocoding request
      MapboxGeocoding client = MapboxGeocoding.builder()
        .accessToken(getString(R.string.access_token))
        .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
        .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
        .mode(GeocodingCriteria.MODE_PLACES)
        .build();
      client.enqueueCall(new Callback<GeocodingResponse>() {
        @Override
        public void onResponse(Call<GeocodingResponse> call,
                               Response<GeocodingResponse> response) {
          List<CarmenFeature> results = response.body().features();
          if (results.size() > 0) {

            // Get the first Feature from the successful geocoding response
            CarmenFeature feature = results.get(0);
            geocodeResultTextView.setText(feature.toString());
            animateCameraToNewPosition(latLng);
          } else {
            Toast.makeText(GeocodingActivity.this, R.string.no_results,
              Toast.LENGTH_SHORT).show();
          }
        }

        @Override
        public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
          Timber.e("Geocoding Failure: " + throwable.getMessage());
        }
      });
    } catch (ServicesException servicesException) {
      Timber.e("Error geocoding: " + servicesException.toString());
      servicesException.printStackTrace();
    }
  }

  private void animateCameraToNewPosition(LatLng latLng) {
    mapboxMap.animateCamera(CameraUpdateFactory
      .newCameraPosition(new CameraPosition.Builder()
        .target(latLng)
        .zoom(13)
        .build()), 1500);
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