package com.mapbox.mapboxandroiddemo.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.firebase.perf.metrics.AddTrace;
import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_CREATE_ACCOUNT_BUTTON;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_SIGN_IN_BUTTON;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.AUTHCODE_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.CLIENT_ID_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.FROM_LOGIN_SCREEN_MENU_ITEM_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.FROM_LOG_OUT_BUTTON_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.LOGIN_SIGNIN_IGNORE_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.REDIRECT_URI_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_SAVED_KEY;


public class LandingActivity extends AppCompatActivity {

  private static final String SIGN_IN_AUTH_URL = "https://api.mapbox.com/oauth/authorize?"
    + "response_type=code&client_id=%s&redirect_uri=%s";
  private static final String CLIENT_ID = "7bb34a0cf68455d33ec0d994af2330a3f60ee636";
  private static final String REDIRECT_URI = "mapbox-android-dev-preview://authorize";
  private static String CREATE_ACCOUNT_AUTH_URL;
  private boolean loggedIn;
  private boolean ignoreCheckboxIsChecked;
  private AnalyticsTracker analytics;

  @Override
  @AddTrace(name = "onCreateLandingActivity")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    loggedIn = PreferenceManager.getDefaultSharedPreferences(
      getApplicationContext())
      .getBoolean(TOKEN_SAVED_KEY, false);

    boolean alreadyIgnoredLandingAsk = PreferenceManager.getDefaultSharedPreferences(
      getApplicationContext())
      .getBoolean(LOGIN_SIGNIN_IGNORE_KEY, false);

    analytics = AnalyticsTracker.getInstance(this, false);

    if (getIntent().getBooleanExtra(FROM_LOG_OUT_BUTTON_KEY, false)) {
      alreadyIgnoredLandingAsk = false;
    }
    if (!loggedIn && !alreadyIgnoredLandingAsk || getIntent().getBooleanExtra(
      FROM_LOGIN_SCREEN_MENU_ITEM_KEY, false)) {
      setContentView(R.layout.activity_landing);
      if (getSupportActionBar() != null) {
        getSupportActionBar().hide();
      }
      buildAccountAuthUrl();
      setUpButtons();
    } else {
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    }
  }

  @Override
  public void startActivityForResult(Intent intent, int requestCode) {
    super.startActivityForResult(intent, requestCode);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (getIntent().getBooleanExtra(FROM_LOG_OUT_BUTTON_KEY, false)) {
      Toast.makeText(getApplicationContext(), R.string.log_out_toast_confirm, Toast.LENGTH_LONG).show();
    } else if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
      Uri uri = getIntent().getData();
      if (uri != null) {
        String error = uri.getQueryParameter("error");
        if (error != null) {
          showErrorDialog();
        } else {
          String authCode = uri.getQueryParameter("code");
          Intent intent = new Intent(this, AccountRetrievalService.class);
          intent.putExtra(AUTHCODE_KEY, authCode);
          intent.putExtra(REDIRECT_URI_KEY, REDIRECT_URI);
          intent.putExtra(CLIENT_ID_KEY, CLIENT_ID);
          startService(intent);
          Intent loadingActivityIntent = new Intent(this, LoadingActivity.class);
          startActivity(loadingActivityIntent);
        }
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

  private void setUpButtons() {
    findViewById(R.id.button_skip_for_now).setOnClickListener(view -> {
      goToMainActivity();
    });

    findViewById(R.id.create_account_button).setOnClickListener(view -> {
      openChromeCustomTab(true);
      analytics.trackEvent(CLICKED_ON_CREATE_ACCOUNT_BUTTON,
        loggedIn);
    });

    findViewById(R.id.sign_in_button).setOnClickListener(view -> {
      openChromeCustomTab(false);
      analytics.trackEvent(CLICKED_ON_SIGN_IN_BUTTON,
        loggedIn);
    });

    CheckBox checkBox = findViewById(R.id.do_not_show_again_checkbox);
    checkBox.setChecked(PreferenceManager.getDefaultSharedPreferences(
      getApplicationContext()).getBoolean(LOGIN_SIGNIN_IGNORE_KEY, false));
    ignoreCheckboxIsChecked = PreferenceManager.getDefaultSharedPreferences(
      getApplicationContext()).getBoolean(LOGIN_SIGNIN_IGNORE_KEY, false);
    checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
      if (ignoreCheckboxIsChecked) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
          .putBoolean(LOGIN_SIGNIN_IGNORE_KEY, false).apply();
      }
      ignoreCheckboxIsChecked = isChecked;
      if (isChecked) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
          .putBoolean(LOGIN_SIGNIN_IGNORE_KEY, true).apply();
        goToMainActivity();
      }
    });
  }

  private void goToMainActivity() {
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(intent);
  }

  private void showErrorDialog() {
    new MaterialStyledDialog.Builder(this)
      .setTitle(getString(R.string.whoops_error_dialog_title))
      .setDescription(getString(R.string.whoops_error_dialog_message))
      .setHeaderColor(R.color.mapboxRedDark)
      .withDivider(true)
      .setPositiveText(getString(R.string.whoops_error_dialog_ok_positive_button))
      .onPositive((dialog, which) -> dialog.dismiss())
      .show();
  }

  private void buildAccountAuthUrl() {
    Uri.Builder builder = new Uri.Builder();
    builder.scheme("https")
      .authority("www.mapbox.com")
      .appendPath("signup")
      .appendQueryParameter("route-to", "https%3A%2F%2Fwww.mapbox"
        + ".com%2Fauthorize%2F%3Fclient_id%3D7bb34a0cf68455d33ec0d994af2330a3f60ee636%26"
        + "redirect_uri%3Dmapbox-android-dev-"
        + "preview%3A%2F%2Fauthorize%26response_type%3Dcode");
    CREATE_ACCOUNT_AUTH_URL = builder.build().toString();
  }
}