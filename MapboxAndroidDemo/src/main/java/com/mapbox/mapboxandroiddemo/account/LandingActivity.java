package com.mapbox.mapboxandroiddemo.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.perf.metrics.AddTrace;
import com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;

import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_CREATE_ACCOUNT_BUTTON;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_SIGN_IN_BUTTON;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.AUTHCODE_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.CLIENT_ID_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.REDIRECT_URI_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_SAVED_KEY;


public class LandingActivity extends AppCompatActivity {

  private static final String SIGN_IN_AUTH_URL = "https://api.mapbox.com/oauth/authorize?"
    + "response_type=code&client_id=%s&redirect_uri=%s";

  private static String CREATE_ACCOUNT_AUTH_URL;
  private static final String CLIENT_ID = "7bb34a0cf68455d33ec0d994af2330a3f60ee636";
  private static final String REDIRECT_URI = "mapbox-android-dev-preview://authorize";
  private boolean loggedIn;
  private AnalyticsTracker analytics;


  @Override
  @AddTrace(name = "onCreateLandingActivity")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loggedIn = PreferenceManager.getDefaultSharedPreferences(
      getApplicationContext())
      .getBoolean(TOKEN_SAVED_KEY, false);
    analytics = AnalyticsTracker.getInstance(this, false);


    if (!loggedIn) {
      setContentView(R.layout.activity_landing);
      getSupportActionBar().hide();
      buildAccountAuthUrl();
      setUpSkipDialog();
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
    if (getIntent().getBooleanExtra("FROM_LOG_OUT_BUTTON", false)) {
      Toast.makeText(getApplicationContext(), R.string.log_out_toast_confirm, Toast.LENGTH_LONG).show();
    } else if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
      Uri uri = getIntent().getData();
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
        analytics.trackEvent(CLICKED_ON_CREATE_ACCOUNT_BUTTON,
          loggedIn);
      }
    });

    Button signInButton = (Button) findViewById(R.id.sign_in_button);
    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openChromeCustomTab(false);
        analytics.trackEvent(CLICKED_ON_SIGN_IN_BUTTON,
          loggedIn)
        ;
      }
    });
  }

  private void showErrorDialog() {
    new MaterialStyledDialog.Builder(this)
      .setTitle(getString(R.string.whoops_error_dialog_title))
      .setDescription(getString(R.string.whoops_error_dialog_message))
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