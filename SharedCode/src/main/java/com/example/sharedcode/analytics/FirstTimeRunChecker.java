package com.example.sharedcode.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.sharedcode.BuildConfig;


/**
 * This class checks whether the app is being opened for the first time or not and adjusts shared preferences
 */

public class FirstTimeRunChecker {

  private static final String PREFS_NAME = "MyPrefsFile";
  private static final String PREF_VERSION_CODE_KEY = "version_code";
  private static final int DOESNT_EXIST = -1;
  private Context context;

  // Get current version code
  private int currentVersionCode = BuildConfig.VERSION_CODE;

  // Get saved version code
  private SharedPreferences prefs;
  private int savedVersionCode;

  public FirstTimeRunChecker(Context context) {
    this.context = context;
    prefs = PreferenceManager.getDefaultSharedPreferences(context);
    savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
  }

  public boolean firstEverOpen() {
    if (savedVersionCode == DOESNT_EXIST) {
      return true;
    }
    return false;
  }

  public void updateSharedPrefWithCurrentVersion() {
    prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
  }

}
