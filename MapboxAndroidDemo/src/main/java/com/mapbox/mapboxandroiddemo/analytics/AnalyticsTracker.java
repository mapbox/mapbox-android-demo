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
    private String exampleNameMapKey = "example name";
    private String isTabletMapValue = "tablet";
    private String isPhoneMapValue = "phone";


    public static AnalyticsTracker get() {
        return INSTANCE;
    }

    private Analytics analytics = Analytics.builder("zFLtBpautarTslr61PUbvEKXXLIoLRmq").build();

    public String mapboxUsername = "LangstonSmithTestUsername";

    public void openedAppForFirstTime(@NonNull String userId, boolean isTablet) {

        Map<String, String> properties = new HashMap<>();

        properties.put("Device model", Build.MODEL);
        properties.put("Device brand", Build.BRAND);
        properties.put("Device product", Build.PRODUCT);
        properties.put("Device manufacturer", Build.MANUFACTURER);
        properties.put("Device device", Build.DEVICE);
        properties.put("Device tags", Build.TAGS);

        if (isTablet) {
            properties.put("Device size", isTabletMapValue);
        } else {
            properties.put("Device size", isPhoneMapValue);
        }

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

    public void viewedScreen(String nameOfScreen) {
        analytics.enqueue(ScreenMessage.builder(nameOfScreen).userId(mapboxUsername));
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


}