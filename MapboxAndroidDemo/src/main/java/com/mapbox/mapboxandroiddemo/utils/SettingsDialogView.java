package com.mapbox.mapboxandroiddemo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.account.LandingActivity;
import com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker;

import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.LOGGED_OUT_OF_MAPBOX_ACCOUNT;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.OPTED_IN_TO_ANALYTICS;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.OPTED_OUT_OF_ANALYTICS;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.AVATAR_IMAGE_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.EMAIL_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.FROM_LOG_OUT_BUTTON_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.LOGIN_SIGNIN_IGNORE_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_SAVED_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.USERNAME_KEY;


public class SettingsDialogView {

  private View customDialogView;
  private Context context;
  private Switch analyticsOptOutSwitch;
  private Switch alwaysShowLandingSwitch;
  private AnalyticsTracker analytics;
  private boolean loggedInOrNot;


  public SettingsDialogView(View view, Context context, Switch analyticsOptOutSwitch,Switch alwaysShowLandingSwitch,
                            AnalyticsTracker analytics, boolean loggedInOrNot) {
    this.customDialogView = view;
    this.context = context;
    this.analyticsOptOutSwitch = analyticsOptOutSwitch;
    this.alwaysShowLandingSwitch = alwaysShowLandingSwitch;
    this.analytics = analytics;
    this.loggedInOrNot = loggedInOrNot;
  }

  public void buildDialog() {
    analyticsOptOutSwitch.setChecked(!analytics.isAnalyticsEnabled());
    alwaysShowLandingSwitch.setChecked(!PreferenceManager
      .getDefaultSharedPreferences(context).getBoolean(LOGIN_SIGNIN_IGNORE_KEY, false));
    new AlertDialog.Builder(context)
      .setView(customDialogView)
      .setTitle(R.string.settings_dialog_title)
      .setPositiveButton(R.string.settings_dialog_positive_button_text, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (analyticsOptOutSwitch.isChecked()) {
            changeAnalyticsSettings(false, loggedInOrNot);
          } else {
            changeAnalyticsSettings(true, loggedInOrNot);
          }

          SharedPreferences.Editor sharePrefEditor = PreferenceManager
            .getDefaultSharedPreferences(context).edit()
            .putBoolean(LOGIN_SIGNIN_IGNORE_KEY, !alwaysShowLandingSwitch.isChecked());
          sharePrefEditor.apply();

          Toast.makeText(context, context.getString(R.string.settings_status_toast_settings_saved),
            Toast.LENGTH_SHORT).show();
        }
      })
      .setNegativeButton(R.string.settings_dialog_negative_button_text, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      })
      .show();
  }

  private void changeAnalyticsSettings(boolean optedIn, boolean loggedIn) {
    if (optedIn) {
      analytics.optUserIntoAnalytics(optedIn);
      analytics.trackEvent(OPTED_IN_TO_ANALYTICS, loggedIn);
    } else {
      analytics.trackEvent(OPTED_OUT_OF_ANALYTICS, loggedIn);
      analytics.optUserIntoAnalytics(optedIn);
    }
  }

  public void logOut(boolean loggedIn) {
    analytics.trackEvent(LOGGED_OUT_OF_MAPBOX_ACCOUNT, loggedIn);
    SharedPreferences.Editor sharePrefEditor = PreferenceManager
      .getDefaultSharedPreferences(context).edit();
    sharePrefEditor.putBoolean(TOKEN_SAVED_KEY, false);
    sharePrefEditor.putString(USERNAME_KEY, "");
    sharePrefEditor.putString(EMAIL_KEY, "");
    sharePrefEditor.putString(AVATAR_IMAGE_KEY, "");
    sharePrefEditor.putString(TOKEN_KEY, "");
    sharePrefEditor.apply();
    Intent intent = new Intent(context, LandingActivity.class);
    intent.putExtra(FROM_LOG_OUT_BUTTON_KEY, true);
    context.startActivity(intent);
  }

  private void goToLandingActivity(String stringExtraKey) {

  }
}
