package com.mapbox.mapboxandroiddemo.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;


public class LandingActivity extends AppCompatActivity {

  private static final String SIGN_IN_AUTH_URL = "https://api.mapbox.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s";

  private static final String CREATE_ACCOUNT_AUTH_URL = "https://www.mapbox.com/signup/?route-to=https%3A%2F%2Fwww.mapbox"
    + ".com%2Fauthorize%2F%3Fclient_id%3D7bb34a0cf68455d33ec0d994af2330a3f60ee636%26redirect_uri%3Dmapbox-android-dev-"
    + "preview%3A%2F%2Fauthorize%26response_type%3Dcode";
  private static final String CLIENT_ID = "7bb34a0cf68455d33ec0d994af2330a3f60ee636";
  private static final String REDIRECT_URI = "mapbox-android-dev-preview://authorize";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("TOKEN_SAVED", false)) {
      setContentView(R.layout.activity_landing);
      getSupportActionBar().hide();
      setUpSkipDialog();
      setUpButtons();
    } else {
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
      Uri uri = getIntent().getData();
      String error = uri.getQueryParameter("error");
      if (error != null) {
        Toast.makeText(this, R.string.whoops_error_message_on_app_return, Toast.LENGTH_SHORT).show();
        Log.d("LandingActivity", "onResume: error = " + error);
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
    final String urlToVisit;
    urlToVisit = creatingAccount ? CREATE_ACCOUNT_AUTH_URL : String.format(SIGN_IN_AUTH_URL, CLIENT_ID, REDIRECT_URI);
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
      }
    });

    Button signInButton = (Button) findViewById(R.id.sign_in_button);
    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openChromeCustomTab(false);
      }
    });
  }
}