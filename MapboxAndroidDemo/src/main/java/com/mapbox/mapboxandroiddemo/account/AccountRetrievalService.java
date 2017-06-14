package com.mapbox.mapboxandroiddemo.account;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.example.sharedcode.analytics.AnalyticsTracker;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.usermodel.UserResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sharedcode.analytics.StringConstants.AVATAR_IMAGE_KEY;
import static com.example.sharedcode.analytics.StringConstants.EMAIL_KEY;
import static com.example.sharedcode.analytics.StringConstants.TOKEN_KEY;
import static com.example.sharedcode.analytics.StringConstants.TOKEN_SAVED_KEY;
import static com.example.sharedcode.analytics.StringConstants.USERNAME_KEY;


/**
 * Background service which retrieves Mapbox Account information
 */

public class AccountRetrievalService extends IntentService {

  private static final String BASE_URL = "https://api.mapbox.com/api/";
  private static final String ACCESS_TOKEN_URL = "https://api.mapbox.com/oauth/access_token";
  private static final String SERVICE_NAME = "AccountRetrievalService";
  private String clientId;
  private String redirectUri;
  private String username;

  private AnalyticsTracker analytics;


  public AccountRetrievalService() {
    super(SERVICE_NAME);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    analytics = AnalyticsTracker.getInstance(this, false);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if (intent != null) {
      String authCode = intent.getStringExtra("AUTHCODE");
      clientId = intent.getStringExtra("CLIENT_ID");
      redirectUri = intent.getStringExtra("REDIRECT_URI");
      getAccessToken(authCode);
    } else {
      Log.d("AccountRetrievalService", "onHandleIntent: intent == null");
    }
  }

  private void getAccessToken(String code) {
    String clientSecret = getString(R.string.mapbox_auth_flow_secret);
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
      .addHeader("User-Agent", "Android Dev Preview")
      .addHeader("Content-Type", "application/x-www-form-urlencoded")
      .url(ACCESS_TOKEN_URL)
      .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
        "grant_type=authorization_code&client_id=" + clientId + "&client_secret=" + clientSecret
          + "&redirect_uri=" + redirectUri + "&code=" + code))
      .build();

    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(okhttp3.Call call, IOException exception) {
        Log.d("AccountRetrievalService", "onFailure: " + exception);
      }

      @Override
      public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
        String json = response.body().string();
        try {
          JSONObject data = new JSONObject(json);
          String accessToken = data.optString("access_token");
          try {
            getUsernameFromJwt(accessToken);
          } catch (Exception exception) {
            exception.printStackTrace();
          }
          getUserInfo(username, accessToken);
        } catch (JSONException exception) {
          exception.printStackTrace();
        }
      }
    });
  }

  private void getUsernameFromJwt(String jwtEncoded) throws Exception {
    try {
      String[] split = jwtEncoded.split("\\.");
      String jwtBody = getJson(split[1]);
      username = jwtBody.substring(6, jwtBody.length() - 34);
    } catch (UnsupportedEncodingException exception) {
      exception.printStackTrace();
    }
  }

  private void getUserInfo(final String userName, final String token) {
    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build();
    MapboxAccountRetrofitService service = retrofit.create(MapboxAccountRetrofitService.class);
    retrofit2.Call<UserResponse> request = service.getUserAccount(userName, token);
    request.enqueue(new retrofit2.Callback<UserResponse>() {
      @Override
      public void onResponse(retrofit2.Call<UserResponse> call, retrofit2.Response<UserResponse> response) {
        String userId = response.body().getId();
        String emailAddress = response.body().getEmail();
        String avatarUrl = response.body().getAvatar();
        saveUserInfoToSharedPref(userId, emailAddress, avatarUrl, token);
        analytics.setMapboxUsername();
        analytics.identifyUser(emailAddress);
      }

      @Override
      public void onFailure(retrofit2.Call<UserResponse> call, Throwable throwable) {
        throwable.printStackTrace();
      }
    });
  }

  private static String getJson(String strEncoded) throws UnsupportedEncodingException {
    byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
    return new String(decodedBytes, "UTF-8");
  }

  private void saveUserInfoToSharedPref(String userId, String emailAddress, String avatarUrl, String token) {
    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
      .putBoolean(TOKEN_SAVED_KEY, true)
      .putString(USERNAME_KEY, userId)
      .putString(EMAIL_KEY, emailAddress)
      .putString(AVATAR_IMAGE_KEY, avatarUrl)
      .putString(TOKEN_KEY, token)
      .apply();
  }
}
