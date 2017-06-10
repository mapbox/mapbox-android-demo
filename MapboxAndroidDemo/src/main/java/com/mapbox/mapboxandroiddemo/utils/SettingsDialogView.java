package com.mapbox.mapboxandroiddemo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Switch;

import com.mapbox.mapboxandroiddemo.R;

/**
 * Created by LangstonSmith on 6/10/17.
 */

public class SettingsDialogView {

  public SettingsDialogView() {
  }

  public AlertDialog buildDialog(final Context context, View view, final Switch analyticsOptOutSwitch) {
    new AlertDialog.Builder(context)
        .setView(view)
        .setTitle(R.string.settings_dialog_title)
        .setPositiveButton(R.string.settings_dialog_positive_button_text, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (analyticsOptOutSwitch.isChecked()) {
              changeAnalyticsSettings(false, context.getString(R.string.settings_status_toast_analytics_opt_out));
            } else {
              changeAnalyticsSettings(true, context.getString(R.string.settings_status_toast_analytics_opt_in));
            }
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
}
