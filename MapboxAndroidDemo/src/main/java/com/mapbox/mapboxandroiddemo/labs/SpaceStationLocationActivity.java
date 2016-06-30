package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.IssModel;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpaceStationLocationActivity extends AppCompatActivity{

    private MapView mapView;
    private Handler handler;
    private Runnable runnable;

    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_station_location);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                Retrofit client = new Retrofit.Builder()
                        .baseUrl("http://api.open-notify.org/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                final IssAPI service = client.create(IssAPI.class);

                final int delay = 2000;
                handler = new Handler();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        Call<IssModel> call = service.loadLocation();
                        call.enqueue(new Callback<IssModel>() {
                            @Override
                            public void onResponse(Call<IssModel> call, Response<IssModel> response) {

                                double latitude = response.body().getIssPosition().getLatitude();
                                double longitude = response.body().getIssPosition().getLongitude();

                                if(marker == null){
                                    marker = mapboxMap.addMarker(new MarkerViewOptions().position(new LatLng(latitude, longitude)));
                                    return;
                                }

                                ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position",
                                        new LatLngEvaluator(), marker.getPosition(), new LatLng(latitude, longitude));
                                markerAnimator.setDuration(2000);
                                markerAnimator.setInterpolator(new LinearInterpolator());
                                markerAnimator.start();
                            }

                            @Override
                            public void onFailure(Call<IssModel> call, Throwable t) {
                                System.out.println(t.getMessage());
                            }
                        });


                        handler.postDelayed(this, delay);
                    }
                };

                handler.postDelayed(runnable, delay);









            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        handler.removeCallbacks(runnable);
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
}
