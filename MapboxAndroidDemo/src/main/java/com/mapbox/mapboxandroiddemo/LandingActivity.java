package com.mapbox.mapboxandroiddemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LandingActivity extends AppCompatActivity {

  private static final String AUTH_URL = "https://api.mapbox.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code";
  private static final String CLIENT_ID = "7bb34a0cf68455d33ec0d994af2330a3f60ee636";
  private static final String REDIRECT_URI = "mapbox-android-dev-preview://authorize";
  private static final String ACCESS_TOKEN_URL = "https://api.mapbox.com/oauth/access_token";

  //  TODO: Fill in CLIENT_SECRET
  private static final String CLIENT_SECRET = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_landing);

    Button createAccountButton = (Button) findViewById(R.id.create_account_button);
    Button signInButton = (Button) findViewById(R.id.sign_in_button);
    createAccountButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO: Haven't tested this flow yet
//        openChromeCustomTab(view);
      }
    });

    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openChromeCustomTab(view);
      }
    });

    skipForNowDialog();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
      Uri uri = getIntent().getData();
      if (uri.getQueryParameter("error") != null) {
        String error = uri.getQueryParameter("error");
        Log.e("LandingActivity", "An error has occurred : " + error);
      } else {
        String code = uri.getQueryParameter("code");
        getAccessToken(code);
      }
    }
  }


  private void getAccessToken(String code) {
    OkHttpClient client = new OkHttpClient();
    String authString = CLIENT_ID + ":";
    String encodedAuthString = Base64.encodeToString(authString.getBytes(),
      Base64.NO_WRAP);

    Request request = new Request.Builder()
      .addHeader("User-Agent", "Sample App")
      .addHeader("Authorization", "Basic " + encodedAuthString)
      .url(ACCESS_TOKEN_URL)
      .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
        "grant_type=authorization_code&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET
          + "&redirect_uri=" + REDIRECT_URI + "&code=" + code))
      .build();

    Log.d("LandingActivity", "getAccessToken: " + RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
      "grant_type=authorization_code&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET
        + "&redirect_uri=" + REDIRECT_URI + "&code=" + code));


    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        Log.e("LandingActivity", "ERROR: " + exception);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String json = response.body().string();

        JSONObject data = null;
        try {
          data = new JSONObject(json);
          String accessToken = data.optString("access_token");
          String refreshToken = data.optString("refresh_token");

          Log.d("LandingActivity", "Access Token = " + accessToken);
          Log.d("LandingActivity", "Refresh Token = " + refreshToken);
        } catch (JSONException exception) {
          exception.printStackTrace();
        }
      }
    });

  }

  private void openChromeCustomTab(View view) {
    String url = String.format(AUTH_URL, CLIENT_ID, REDIRECT_URI);

    Log.d("LandingActivity", "openChromeCustomTab: url = " + url);

    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
    intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.mapboxGrayDark10));
    intentBuilder.setShowTitle(true);
    CustomTabsIntent customTabsIntent = intentBuilder.build();
    customTabsIntent.launchUrl(this, Uri.parse(url));
    Log.d("LandingActivity", "openChromeCustomTab: Uri.parse(url) = " + Uri.parse(url));
  }

  private void skipForNowDialog() {
    Button skipForNowButton = (Button) findViewById(R.id.button_skip_for_now);
    skipForNowButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        new MaterialStyledDialog.Builder(LandingActivity.this)
          .setTitle(getString(R.string.skip_for_now_dialog_title))
          .setDescription(getString(R.string.skip_for_now_dialog_description))
          .withDivider(true)
          .setIcon(R.mipmap.ic_launcher)
          .setHeaderColor(R.color.mapboxGrayLight)
          .setPositiveText(getString(R.string.skip_for_now_dialog_go_back_button))
          .onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
              dialog.dismiss();
            }
          })
          .setNegativeText(getString(R.string.skip_for_now_dialog_skip_button))
          .onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
              Intent intent = new Intent(getApplicationContext(), MainActivity.class);
              startActivity(intent);
            }
          })
          .show();
      }
    });

  }

}