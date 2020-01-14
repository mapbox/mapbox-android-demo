package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.IssModel;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

/**
 * Display the space station's real-time location
 */
public class SpaceStationLocationActivity extends AppCompatActivity {

  private static final String TAG = "SpaceStationActivity";

  private Handler handler;
  private Runnable runnable;
  private Call<IssModel> call;

  // apiCallTime is the time interval when we call the API in milliseconds, by default this is set
  // to 2000 and you should only increase the value, reducing the interval will only cause server
  // traffic, the latitude and longitude values aren't updated that frequently.
  private int apiCallTime = 2000;

  // Map variables
  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_space_station_location);

    // Initialize the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        map = mapboxMap;

        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            initSpaceStationSymbolLayer(style);
            callApi();
            Toast.makeText(SpaceStationLocationActivity.this, R.string.space_station_toast, Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
  }


  private void callApi() {

    // Build our client, The API we are using is very basic only returning a handful of
    // information, mainly, the current latitude and longitude of the International Space Station.
    Retrofit client = new Retrofit.Builder()
      .baseUrl("http://api.open-notify.org/")
      .addConverterFactory(GsonConverterFactory.create())
      .build();

    final IssApiService service = client.create(IssApiService.class);

    // A handler is needed to called the API every x amount of seconds.
    handler = new Handler();
    runnable = new Runnable() {
      @Override
      public void run() {
        // Call the API so we can get the updated coordinates.
        call = service.loadLocation();
        call.enqueue(new Callback<IssModel>() {
          @Override
          public void onResponse(Call<IssModel> call, Response<IssModel> response) {

            // We only need the latitude and longitude from the API.
            if (response.body() != null) {
              double latitude = response.body().getIssPosition().getLatitude();
              double longitude = response.body().getIssPosition().getLongitude();
              updateMarkerPosition(new LatLng(latitude, longitude));
            }
          }

          @Override
          public void onFailure(Call<IssModel> call, Throwable throwable) {
            // If retrofit fails or the API was unreachable, an error will be called.
            //to check if throwable is null, then give a custom message.
            if (throwable.getMessage() == null) {
              Timber.e("Http connection failed");
            } else {
              Timber.e(throwable.getMessage());
            }

          }
        });
        // Schedule the next execution time for this runnable.
        handler.postDelayed(this, apiCallTime);
      }
    };

    // The first time this runs we don't need a delay so we immediately post.
    handler.post(runnable);
  }

  private void initSpaceStationSymbolLayer(@NonNull Style style) {
    style.addImage("space-station-icon-id",
      BitmapFactory.decodeResource(
        this.getResources(), R.drawable.iss));

    style.addSource(new GeoJsonSource("source-id"));

    style.addLayer(new SymbolLayer("layer-id", "source-id").withProperties(
      iconImage("space-station-icon-id"),
      iconIgnorePlacement(true),
      iconAllowOverlap(true),
      iconSize(.7f)
    ));
  }

  private void updateMarkerPosition(LatLng position) {
    // This method is where we update the marker position once we have new coordinates. First we
    // check if this is the first time we are executing this handler, the best way to do this is
    // check if marker is null;
    if (map.getStyle() != null) {
      GeoJsonSource spaceStationSource = map.getStyle().getSourceAs("source-id");
      if (spaceStationSource != null) {
        spaceStationSource.setGeoJson(FeatureCollection.fromFeature(
          Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
        ));
      }
    }

    // Lastly, animate the camera to the new position so the user
    // wont have to search for the marker and then return.
    map.animateCamera(CameraUpdateFactory.newLatLng(position));
  }

  // Interface used for Retrofit.
  public interface IssApiService {
    @GET("iss-now")
    Call<IssModel> loadLocation();

  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
    // When the user returns to the activity we want to resume the API calling.
    if (handler != null && runnable != null) {
      handler.post(runnable);
    }
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
    // When the user leaves the activity, there is no need in calling the API since the map
    // isn't in view.
    if (handler != null && runnable != null) {
      handler.removeCallbacks(runnable);
    }
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
