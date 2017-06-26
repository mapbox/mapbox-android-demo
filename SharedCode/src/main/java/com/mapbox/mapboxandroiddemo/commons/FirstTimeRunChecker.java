package com.mapbox.mapboxandroiddemo.commons;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class checks whether the app is being opened for the first time or not and adjusts shared preferences
 */

public class FirstTimeRunChecker {

  private static final String PREF_VERSION_CODE_KEY = "version_code";
  private static final int DOESNT_EXIST = -1;

  private final SharedPreferences prefs;
  private final int savedVersionCode;

  public FirstTimeRunChecker(Context context) {
    prefs = PreferenceManager.getDefaultSharedPreferences(context);
    savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
  }

  public boolean firstEverOpen() {
    return savedVersionCode == DOESNT_EXIST;
  }

  public void updateSharedPrefWithCurrentVersion() {
    prefs.edit().putInt(PREF_VERSION_CODE_KEY, BuildConfig.VERSION_CODE).apply();
  }

}
