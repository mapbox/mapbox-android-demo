package com.mapbox.mapboxandroiddemo.analytics;


import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

import java.util.HashMap;

/**
 * Created by langstonsmith on 4/25/17.
 */

public class AnalyticsTracker {

  private static final AnalyticsTracker INSTANCE = new AnalyticsTracker();

  public static AnalyticsTracker get() {

    return INSTANCE;
  }

  private Analytics analytics;
  public String mapboxUsername;

  public String getMapboxUsername() {
    return mapboxUsername;
  }

  public void setMapboxUsername(String mapboxUsername) {
    this.mapboxUsername = mapboxUsername;
  }

  public void openedAppForFirstTime(@NonNull String userID) {

    HashMap<String, String> properties = new HashMap<>();

    properties.put("Device model", Build.MODEL);
    properties.put("Device brand", Build.BRAND);
    properties.put("Device product", Build.PRODUCT);
    properties.put("Device manufacturer", Build.MANUFACTURER);
    properties.put("User location", sendUserLocation());

    analytics.enqueue(TrackMessage.builder("Opened App For First Time")
      .userId(mapboxUsername)
      .properties(properties)
    );

  }


  public void openedApp() {
    trackEvent("Opened App");
  }

  public void clickedOnSignInButton() {
    trackEvent("Clicked On Sign Up Button");
  }

  public void clickedOnCreateAccountButton() {
    trackEvent("Clicked On Create Account Button");
  }


  public void clickedOnNavDrawerSection() {
    trackEvent("Clicked On Nav Drawer Section");
  }

  public void clickedOnIndividualExample() {
    trackEvent("Clicked On Individual Example");
  }

  public void viewedScreen(AppCompatActivity activity) {

    analytics.enqueue(ScreenMessage.builder(activity.getClass().getSimpleName())
      .userId(mapboxUsername)
    );

  }

  public void identifyUser(@NonNull String name, @NonNull String userEmailAddress) {

    HashMap<String, String> traits = new HashMap<>();
    traits.put("name", name);
    traits.put("email", userEmailAddress);

    analytics.enqueue(IdentifyMessage.builder()
      .userId(mapboxUsername)
      .traits(traits)
    );

  }


  public void trackEvent(@NonNull String eventName, @) {

    analytics.enqueue(TrackMessage.builder(eventName)
      .userId(mapboxUsername));
  }

  public String sendUserLocation() {

    LatLng location = new LatLng();

//    TODO:Get user location
    return location.toString();
  }


  public Analytics getAnalytics() {
    return getDefaultTracker();
  }

  private synchronized Analytics getDefaultTracker() {
    if (analytics == null) {
      analytics = Analytics.builder(Integer.toString(R.string.segment_analytics_write_key)).build();
    }
    return analytics;
  }


}