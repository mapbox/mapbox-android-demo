package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.IssModel;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class SpaceStationLocationActivity extends AppCompatActivity {

  private static final String TAG = "SpaceStationActivity";

  private Handler handler;
  private Runnable runnable;
  private Call<IssModel> call;

  // APICallTime is the time interval when we call the API in milliseconds, by default this is set
  // to 2000 and you should only increase the value, reducing the interval will only cause server
  // traffic, the latitude and longitude values aren't updated that frequently.
  private int APICallTime = 2000;

  // Map variables
  private MapView mapView;
  private MarkerView marker;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lab_space_station_location);

    // Initialize the map view
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        map = mapboxMap;

        callAPI();

      }
    });
  }// End onCreate

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

  private void callAPI() {

    // Build our client, The API we are using is very basic only returning a handful of
    // information, mainly, the current latitude and longitude of the International Space Station.
    Retrofit client = new Retrofit.Builder()
        .baseUrl("http://api.open-notify.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    final IssAPIService service = client.create(IssAPIService.class);

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
            double latitude = response.body().getIssPosition().getLatitude();
            double longitude = response.body().getIssPosition().getLongitude();

            updateMarkerPosition(new LatLng(latitude, longitude));
          }

          @Override
          public void onFailure(Call<IssModel> call, Throwable t) {
            // If retrofit fails or the API was unreachable, an error will be called.
            Log.e(TAG, t.getMessage());
          }
        });
        // Schedule the next execution time for this runnable.
        handler.postDelayed(this, APICallTime);
      }
    };

    // The first time this runs we don't need a delay so we immediately post.
    handler.post(runnable);
  }

  private void updateMarkerPosition(LatLng position) {
    // This method is were we update the marker position once we have new coordinates. First we
    // check if this is the first time we are executing this handler, the best way to do this is
    // check if marker is null;
    if (marker == null) {

      // Create the icon for the marker
      IconFactory iconFactory = IconFactory.getInstance(SpaceStationLocationActivity.this);
      Drawable iconDrawable = ContextCompat.getDrawable(SpaceStationLocationActivity.this, R.drawable.iss);
      Icon icon = iconFactory.fromDrawable(iconDrawable);

      // Add the marker to the map using the API's latitude and longitude.
      marker = map.addMarker(new MarkerViewOptions()
          .position(position)
          .anchor(0.5f, 0.5f)
          .icon(icon));

      // Lastly, animate the camera to the new position so the user
      // wont have to search for the marker and then return.
      map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 8), APICallTime);
      return;
    }

    // Marker rotation is critical only if you want the marker to point in the direction the
    // object's moving.
    marker.setRotation((float) computeHeading(marker.getPosition(), position));

    ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position",
        new LatLngEvaluator(), marker.getPosition(), position);
    markerAnimator.setDuration(APICallTime);
    markerAnimator.setInterpolator(new LinearInterpolator());
    markerAnimator.start();
  }

  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
    // Method is used to interpolate the marker animation.

    private LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude() +
          ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude() +
          ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  }


  // Returns the heading from one LatLng to another LatLng. Headings are. Expressed in degrees
  // clockwise from North within the range [-180,180). The math for this method came from
  // http://williams.best.vwh.net/avform.htm#Crs I only converted it to Java.
  public static double computeHeading(LatLng from, LatLng to) {
    double fromLat = Math.toRadians(from.getLatitude());
    double fromLng = Math.toRadians(from.getLongitude());
    double toLat = Math.toRadians(to.getLatitude());
    double toLng = Math.toRadians(to.getLongitude());
    double dLng = toLng - fromLng;
    double heading = Math.atan2(Math.sin(dLng) * Math.cos(toLat),
        Math.cos(fromLat) * Math.sin(toLat) - Math.sin(fromLat) * Math.cos(toLat) * Math.cos(dLng));
    return (Math.toDegrees(heading) >= -180 && Math.toDegrees(heading) < 180) ?
        Math.toDegrees(heading) : ((((Math.toDegrees(heading) + 180) % 360) + 360) % 360 + -180);
  }

  // Interface used for Retrofit.
  public interface IssAPIService {
    @GET("iss-now")
    Call<IssModel> loadLocation();
  }
}
