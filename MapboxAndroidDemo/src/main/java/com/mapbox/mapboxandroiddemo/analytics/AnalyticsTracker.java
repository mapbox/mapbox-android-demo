package com.mapbox.mapboxandroiddemo.analytics;

import android.os.Build;
import android.support.annotation.NonNull;

import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class abstracts various analytics calls to Segment analytics' Java library.
 */

public class AnalyticsTracker {

  private static volatile AnalyticsTracker analyticsInstance;
  private Analytics analytics = Analytics.builder("zFLtBpautarTslr61PUbvEKXXLIoLRmq").build();

  private static final String OPENED = "Opened app";
  private static final String CLICKED_ON_SIGN_IN_BUTTON_EVENT_NAME = "Clicked on sign in button";
  private static final String CLICKED_ON_CREATE_ACCOUNT_BUTTON_EVENT_NAME = "Clicked on create account button";
  private static final String CLICKED_ON_NAV_DRAWER_SECTION_EVENT_NAME = "Clicked on nav drawer section";
  private static final String CLICKED_ON_INDIVIDUAL_EXAMPLE_EVENT_NAME = "Clicked on individual example";
  private static final String SECTION_NAME_MAP_KEY = "section name";
  private static final String EXAMPLE_NAME_MAP_KEY = "example name";
  private static final String IS_TABLET_MAP_VALUE = "tablet";
  private static final String IS_PHONE_MAP_VALUE = "phone";
  public static final String ORGANIZATION_NAME = "LangstonCompany";
  public static final String MAPBOX_USERNAME = "LangstonSmithTestUsername";
  public static final String MAPBOX_EMAIL = "langston.smith@mapbox.com";

  // Returns instance of this class for making analytics calls throughout the app
  public static AnalyticsTracker getInstance() {
    if (analyticsInstance == null) {  // Single check
      synchronized (AnalyticsTracker.class) {
        if (analyticsInstance == null) {  // Double check
          analyticsInstance = new AnalyticsTracker();
        }
      }
    }
    return analyticsInstance;
  }

  /**
   * Gets and adds device information to analytics call. Ideally, this method is called
   * when app is opened for the first time or if shared preferences is cleared.
   **/

  public void openedAppForFirstTime(boolean isTablet) {

    Map<String, String> properties = new HashMap<>();
    properties.put("model", Build.MODEL);
    properties.put("brand", Build.BRAND);
    properties.put("product", Build.PRODUCT);
    properties.put("manufacturer", Build.MANUFACTURER);
    properties.put("device", Build.DEVICE);
    properties.put("tags", Build.TAGS);
    properties.put("ISO03 language", Locale.getDefault().getISO3Language());
    properties.put("language", Locale.getDefault().getLanguage());
    properties.put("ISO03 country", Locale.getDefault().getISO3Country());
    properties.put("country", Locale.getDefault().getCountry());
    properties.put("display country", Locale.getDefault().getDisplayCountry());
    properties.put("display name", Locale.getDefault().getDisplayName());
    properties.put("display language", Locale.getDefault().getDisplayLanguage());
    properties.put("size", isTablet ? IS_TABLET_MAP_VALUE : IS_PHONE_MAP_VALUE);

    analytics.enqueue(TrackMessage.builder("Opened App For First Time")
      .userId(MAPBOX_USERNAME)
      .properties(properties)
    );
  }

  /**
   * Makes an analytics call telling Segment that the app was opened. Ideally used
   * in the MapboxApplication class' onCreate(). Could also be called in the MainActivity's onCreate()
   */
  public void openedApp() {
    trackEvent(OPENED);
  }

  /**
   * Makes an analytics call telling Segment that the Sign In button has been clicked.
   */
  public void clickedOnSignInButton() {
    trackEvent(CLICKED_ON_SIGN_IN_BUTTON_EVENT_NAME);
  }

  /**
   * Makes an analytics call telling Segment that the Create Account button has been clicked.
   */
  public void clickedOnCreateAccountButton() {
    trackEvent(CLICKED_ON_CREATE_ACCOUNT_BUTTON_EVENT_NAME);
  }

  /**
   * Makes an analytics call telling Segment which particular navigation drawer section has been selected.
   *
   * @param sectionName Name of the selected navigation drawer category
   */
  public void clickedOnNavDrawerSection(@NonNull String sectionName) {
    trackEventWithProperties(CLICKED_ON_NAV_DRAWER_SECTION_EVENT_NAME, SECTION_NAME_MAP_KEY, sectionName);
  }

  /**
   * Makes an analytics call telling Segment which particular example has selected
   *
   * @param exampleName Name of the selected example
   */
  public void clickedOnIndividualExample(@NonNull String exampleName) {
    trackEventWithProperties(CLICKED_ON_INDIVIDUAL_EXAMPLE_EVENT_NAME, EXAMPLE_NAME_MAP_KEY, exampleName);
  }

  /**
   * Makes an analytics call telling Segment what custom-named event has happened. Because there are no
   * custom parameters involved with the call, this method is the most "basic" analytics call that's
   * available in this class.
   *
   * @param eventName Name of the event that's being recorded
   */
  public void trackEvent(@NonNull String eventName) {
    trackEventWithProperties(eventName, null, null);
  }

  /**
   * Makes an analytics call telling Segment what custom-named event has happened. Custom parameters provided
   * are also included in the call.
   *
   * @param eventName Name of the event that's being recorded
   * @param keyForPropertiesMap Key to the property being attached to the event that's being called
   * @param valueForPropertiesMap Value of the property being attached to the event that's being called
   */
  public void trackEventWithProperties(@NonNull String eventName, String keyForPropertiesMap,
                                       String valueForPropertiesMap) {

    if (keyForPropertiesMap == null || valueForPropertiesMap == null) {
      analytics.enqueue(TrackMessage.builder(eventName)
        .userId(MAPBOX_USERNAME));
    }

    if (keyForPropertiesMap != null && valueForPropertiesMap != null) {

      Map<String, String> properties = new HashMap<>();
      properties.put(keyForPropertiesMap, valueForPropertiesMap);

      analytics.enqueue(TrackMessage.builder(eventName).userId(MAPBOX_USERNAME).properties(properties));
    }
  }

  /**
   * Makes an analytics call telling Segment that a certain screen has been viewed via a "ScreenMessage" call.
   * Ideally called in onCreate(), onCreateView(), etc.
   *
   * @param nameOfScreen Name of the screen/activity that's being viewed
   */
  public void viewedScreen(String nameOfScreen) {
    analytics.enqueue(ScreenMessage.builder(nameOfScreen).userId(MAPBOX_USERNAME));
  }

  /**
   * Makes an analytics call telling Segment the user's identity via a "IdentifyMessage" call.
   *
   * @param organizationName Organization name associated with user's Mapbox account
   * @param userEmailAddress Email address associated with user's Mapbox account
   */
  public void identifyUser(@NonNull String organizationName, @NonNull String userEmailAddress) {
    Map<String, String> traits = new HashMap<>();
    traits.put("organizationName", organizationName);
    traits.put("email", userEmailAddress);

    analytics.enqueue(IdentifyMessage.builder()
      .userId(MAPBOX_USERNAME)
      .traits(traits)
    );
  }
}