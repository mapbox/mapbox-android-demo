package com.mapbox.mapboxandroiddemo.analytics;


import android.os.Build;
import android.support.annotation.NonNull;

import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

import java.util.HashMap;
import java.util.Map;

/**
 *  A class which abstracts the various analytics calls for Segment analytics' Java library.
 *
 *  
 *
 */

public class AnalyticsTracker {

  private static final AnalyticsTracker INSTANCE = new AnalyticsTracker();

  private static final String OPENED = "Opened app";
  private static final String CLICKED_ON_SIGN_IN_BUTTON_EVENT_NAME = "Clicked on sign in button";
  private static final String CLICKED_ON_CREATE_ACCOUNT_BUTTON_EVENT_NAME = "Clicked on create account button";
  private static final String CLICKED_ON_NAV_DRAWER_SECTION_EVENT_NAME = "Clicked on nav drawer section";
  private static final String CLICKED_ON_INDIVIDUAL_EXAMPLE_EVENT_NAME = "Clicked on individual example";
  private static final String SECTION_NAME_MAP_KEY = "section name";
  private static final String EXAMPLE_NAME_MAP_KEY = "example name";
  private static final String IS_TABLET_MAP_VALUE = "tablet";
  private static final String IS_PHONE_MAP_VALUE = "phone";


  public static AnalyticsTracker get() {
    return INSTANCE;
  }

  private Analytics analytics = Analytics.builder("zFLtBpautarTslr61PUbvEKXXLIoLRmq").build();

  public static final String MAPBOX_USERNAME = "LangstonSmithTestUsername";

  public void openedAppForFirstTime(@NonNull String userId, boolean isTablet) {

    Map<String, String> properties = new HashMap<>();

    properties.put("Device model", Build.MODEL);
    properties.put("Device brand", Build.BRAND);
    properties.put("Device product", Build.PRODUCT);
    properties.put("Device manufacturer", Build.MANUFACTURER);
    properties.put("Device device", Build.DEVICE);
    properties.put("Device tags", Build.TAGS);
    String deviceSize = isTablet ? IS_TABLET_MAP_VALUE : IS_PHONE_MAP_VALUE;
    properties.put("Device size", deviceSize);

    analytics.enqueue(TrackMessage.builder("Opened App For First Time")
      .userId(MAPBOX_USERNAME)
      .properties(properties)
    );

  }

  public void openedApp() {
    trackEvent(OPENED, null, null);
  }

  public void clickedOnSignInButton() {
    trackEvent(CLICKED_ON_SIGN_IN_BUTTON_EVENT_NAME, null, null);
  }

  public void clickedOnCreateAccountButton() {
    trackEvent(CLICKED_ON_CREATE_ACCOUNT_BUTTON_EVENT_NAME, null, null);
  }

  public void clickedOnNavDrawerSection(@NonNull String sectionName) {
    trackEvent(CLICKED_ON_NAV_DRAWER_SECTION_EVENT_NAME, SECTION_NAME_MAP_KEY, sectionName);
  }

  public void clickedOnIndividualExample(@NonNull String exampleName) {
    trackEvent(CLICKED_ON_INDIVIDUAL_EXAMPLE_EVENT_NAME, EXAMPLE_NAME_MAP_KEY, exampleName);
  }

  public void trackEvent(@NonNull String eventName, String keyForPropertiesMap, String valueForPropertiesMap) {
    
    if (keyForPropertiesMap == null || valueForPropertiesMap == null) {
      analytics.enqueue(TrackMessage.builder(eventName)
        .userId(MAPBOX_USERNAME));
    }

    if (keyForPropertiesMap != null && valueForPropertiesMap != null) {

      Map<String, String> properties = new HashMap<>();
      properties.put(keyForPropertiesMap, valueForPropertiesMap);

      analytics.enqueue(TrackMessage.builder(eventName)
        .userId(MAPBOX_USERNAME)
        .properties(properties));
    }
  }

  public void viewedScreen(String nameOfScreen) {
    analytics.enqueue(ScreenMessage.builder(nameOfScreen).userId(MAPBOX_USERNAME));
  }

  public void identifyUser(@NonNull String actualNameOfUser, @NonNull String userEmailAddress) {

    Map<String, String> traits = new HashMap<>();
    traits.put("name", actualNameOfUser);
    traits.put("email", userEmailAddress);

    analytics.enqueue(IdentifyMessage.builder()
      .userId(MAPBOX_USERNAME)
      .traits(traits)
    );
  }


}