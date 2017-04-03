package com.mapbox.mapboxandroiddemo.examples;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * Sample LocationEngine using Google Play Services
 */
public class GoogleLocationEngine extends LocationEngine implements
  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

  private static final String LOG_TAG = GoogleLocationEngine.class.getSimpleName();

  private static LocationEngine instance;

  private WeakReference<Context> context;
  private GoogleApiClient googleApiClient;

  public GoogleLocationEngine(Context context) {
    super();
    this.context = new WeakReference<>(context);
    googleApiClient = new GoogleApiClient.Builder(this.context.get())
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .addApi(LocationServices.API)
      .build();
  }

  public static synchronized LocationEngine getLocationEngine(Context context) {
    if (instance == null) {
      instance = new GoogleLocationEngine(context.getApplicationContext());
    }

    return instance;
  }

  @Override
  public void activate() {
    if (googleApiClient != null && !googleApiClient.isConnected()) {
      googleApiClient.connect();
    }
  }

  @Override
  public void deactivate() {
    if (googleApiClient != null && googleApiClient.isConnected()) {
      googleApiClient.disconnect();
    }
  }

  @Override
  public boolean isConnected() {
    return googleApiClient.isConnected();
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    for (LocationEngineListener listener : locationListeners) {
      listener.onConnected();
    }
  }

  @Override
  public void onConnectionSuspended(int cause) {
    Log.d(LOG_TAG, "Connection suspended: " + cause);
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.d(LOG_TAG, "Connection failed:" + connectionResult.getErrorMessage());
  }

  @Override
  public Location getLastLocation() {
    if (googleApiClient.isConnected()) {
      //noinspection MissingPermission
      return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    return null;
  }

  @Override
  public void requestLocationUpdates() {
    // Create the LocationRequest object
    LocationRequest locationRequest = LocationRequest.create();
    // Use high accuracy
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    // Set the update interval to 2 seconds
    locationRequest.setInterval(TimeUnit.SECONDS.toMillis(2));
    // Set the fastest update interval to 2 seconds
    locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(2));
    // Set the minimum displacement
    locationRequest.setSmallestDisplacement(2);

    // Register listener using the LocationRequest object
    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
  }

  @Override
  public void removeLocationUpdates() {
    if (googleApiClient.isConnected()) {
      LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    for (LocationEngineListener listener : locationListeners) {
      listener.onLocationChanged(location);
    }
  }
}