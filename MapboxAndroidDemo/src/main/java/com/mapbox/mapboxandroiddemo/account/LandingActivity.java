package com.mapbox.mapboxandroiddemo.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.analytics.AnalyticsTracker;


public class LandingActivity extends AppCompatActivity {

  private static final String SIGN_IN_AUTH_URL = "https://api.mapbox.com/oauth/authorize?"
    + "response_type=code&client_id=%s&redirect_uri=%s";

  private static final String CREATE_ACCOUNT_AUTH_URL = "https://www.mapbox.com"
    + "/signup/?route-to=https%3A%2F%2Fwww.mapbox"
    + ".com%2Fauthorize%2F%3Fclient_id%3D7bb34a0cf68455d33ec0d994af2330a3f60ee636%26"
    + "redirect_uri%3Dmapbox-android-dev-"
    + "preview%3A%2F%2Fauthorize%26response_type%3Dcode";
  private static final String CLIENT_ID = "7bb34a0cf68455d33ec0d994af2330a3f60ee636";
  private static final String REDIRECT_URI = "mapbox-android-dev-preview://authorize";
  private boolean loggedIn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loggedIn = PreferenceManager.getDefaultSharedPreferences(
      getApplicationContext())
      .getBoolean("TOKEN_SAVED", false);

    if (!loggedIn) {
      setContentView(R.layout.activity_landing);
      getSupportActionBar().hide();
      setUpSkipDialog();
      setUpButtons();
      AnalyticsTracker.getInstance(getApplicationContext()).viewedScreen("Create/login screen",
        loggedIn);
    } else {
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (getIntent().getBooleanExtra("FROM_LOG_OUT_BUTTON", false)) {
      Log.d("LandingActivity", "onResume: FROM_LOG_OUT_BUTTON == false");
    } else if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
      Log.d("LandingActivity", "onResume: getIntent().getAction().equals(Intent.ACTION_VIEW");
      Uri uri = getIntent().getData();
      String error = uri.getQueryParameter("error");
      if (error != null) {
        showErrorDialog();
        Log.d("LandingActivity", "onResume: error = " + error);
        AnalyticsTracker.getInstance(getApplicationContext()).trackEvent(
          "Error in LandingActivity onResume()", loggedIn);
      } else {
        String authCode = uri.getQueryParameter("code");
        Intent intent = new Intent(this, AccountRetrievalService.class);
        intent.putExtra("AUTHCODE", authCode);
        intent.putExtra("REDIRECT_URI", REDIRECT_URI);
        intent.putExtra("CLIENT_ID", CLIENT_ID);
        startService(intent);
        Intent loadingActivityIntent = new Intent(this, LoadingActivity.class);
        startActivity(loadingActivityIntent);
      }
    }
  }

  private void openChromeCustomTab(boolean creatingAccount) {
    final String urlToVisit = creatingAccount ? CREATE_ACCOUNT_AUTH_URL
      : String.format(SIGN_IN_AUTH_URL, CLIENT_ID, REDIRECT_URI);
    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
    intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.mapboxGrayDark10));
    intentBuilder.setShowTitle(true);
    CustomTabsIntent customTabsIntent = intentBuilder.build();
    customTabsIntent.launchUrl(this, Uri.parse(urlToVisit));
  }

  private void setUpSkipDialog() {
    Button skipForNowButton = (Button) findViewById(R.id.button_skip_for_now);
    skipForNowButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
          .putBoolean("SKIPPED", true)
          .apply();

        AnalyticsTracker.getInstance(getApplicationContext()).trackEvent("Skipped account creation/login",
          loggedIn);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
      }
    });
  }

  private void setUpButtons() {
    Button createAccountButton = (Button) findViewById(R.id.create_account_button);
    createAccountButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openChromeCustomTab(true);
        AnalyticsTracker.getInstance(getApplicationContext()).trackEvent("Clicked on create account "
          + "button", loggedIn);
      }
    });

    Button signInButton = (Button) findViewById(R.id.sign_in_button);
    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openChromeCustomTab(false);
        AnalyticsTracker.getInstance(getApplicationContext()).trackEvent("Clicked on sign in button",
          loggedIn)
        ;
      }
    });
  }

  private void showErrorDialog() {
    new MaterialStyledDialog.Builder(this)
      .setTitle(getString(R.string.whoops_error_dialog_title))
      .setDescription(getString(R.string.whoops_error_dialog_message))
      .setIcon(R.mipmap.ic_launcher)
      .setHeaderColor(R.color.mapboxRedDark)
      .withDivider(true)
      .setPositiveText(getString(R.string.whoops_error_dialog_ok_positive_button))
      .onPositive(new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
          dialog.dismiss();
        }
      })
      .show();
  }
}