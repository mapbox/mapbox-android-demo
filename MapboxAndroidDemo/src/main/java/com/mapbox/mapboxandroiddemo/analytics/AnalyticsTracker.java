package com.mapbox.mapboxandroiddemo.analytics;


import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by langstonsmith on 4/25/17.
 */

public class AnalyticsTracker {

  private static final AnalyticsTracker INSTANCE = new AnalyticsTracker();

  private String openedApp = "Opened app";
  private String clickedOnSignInButtonEventName = "Clicked on sign in button";
  private String clickedOnCreateAccountButtonEventName = "Clicked on create account button";
  private String clickedOnNavDrawerSectionEventName = "Clicked on nav drawer section";
  private String clickedOnIndividualExampleEventName = "Clicked on individual example";
  private String sectionNameMapKey = "section name";
  private String exampleNameMapKey = "section name";


  public static AnalyticsTracker get() {
    return INSTANCE;
  }

  private Analytics analytics = Analytics.builder("zFLtBpautarTslr61PUbvEKXXLIoLRmq").build();

  public String mapboxUsername = "LangstonSmithTestUsername";


  public void openedAppForFirstTime(@NonNull String userID) {


    Map<String, String> properties = new HashMap<>();

    properties.put("Device model", Build.MODEL);
    properties.put("Device brand", Build.BRAND);
    properties.put("Device product", Build.PRODUCT);
    properties.put("Device manufacturer", Build.MANUFACTURER);

    analytics.enqueue(TrackMessage.builder("Opened App For First Time")
      .userId(mapboxUsername)
      .properties(properties)
    );

  }

  public void openedApp() {
    trackEvent(openedApp, null, null);
  }


  public void clickedOnSignInButton() {
    trackEvent(clickedOnSignInButtonEventName, null, null);
  }

  public void clickedOnCreateAccountButton() {
    trackEvent(clickedOnCreateAccountButtonEventName, null, null);
  }


  public void clickedOnNavDrawerSection(@NonNull String sectionName) {
    trackEvent(clickedOnNavDrawerSectionEventName, sectionNameMapKey, sectionName);

  }

  public void clickedOnIndividualExample(@NonNull String exampleName) {
    trackEvent(clickedOnIndividualExampleEventName, exampleNameMapKey, exampleName);
  }

  public void trackEvent(@NonNull String eventName, String keyForPropertiesMap, String valueForPropertiesMap) {


    if (keyForPropertiesMap == null || valueForPropertiesMap == null) {
      analytics.enqueue(TrackMessage.builder(eventName)
        .userId(mapboxUsername));
    }

    if (keyForPropertiesMap != null && valueForPropertiesMap != null) {

      Map<String, String> properties = new HashMap<>();
      properties.put(keyForPropertiesMap, valueForPropertiesMap);

      analytics.enqueue(TrackMessage.builder(eventName)
        .userId(mapboxUsername)
        .properties(properties));
    }

  }

  public void viewedScreen(AppCompatActivity activity) {

    analytics.enqueue(ScreenMessage.builder(activity.getClass().getSimpleName())
      .userId(mapboxUsername)
    );

  }

  public void identifyUser(@NonNull String actualNameOfUser, @NonNull String userEmailAddress) {

    Map<String, String> traits = new HashMap<>();
    traits.put("name", actualNameOfUser);
    traits.put("email", userEmailAddress);

    analytics.enqueue(IdentifyMessage.builder()
      .userId(mapboxUsername)
      .traits(traits)
    );

  }


  public String sendUserLocation() {
//    TODO: Need to finish this method


    return null;
  }

}