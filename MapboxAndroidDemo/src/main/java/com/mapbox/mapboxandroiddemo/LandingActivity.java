package com.mapbox.mapboxandroiddemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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


public class LandingActivity extends AppCompatActivity {
    private String TAG = "LandingActivity";

    private static final String SIGN_IN_AUTH_URL = "https://api.mapbox.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s";

    private static final String CREATE_ACCOUNT_AUTH_URL = "https://www.mapbox.com/signup/?route-to=https%3A%2F%2Fwww.mapbox"
            + ".com%2Fauthorize%2F%3Fclient_id%3D7bb34a0cf68455d33ec0d994af2330a3f60ee636%26redirect_uri%3Dmapbox-android-dev-"
            + "preview%3A%2F%2Fauthorize%26response_type%3Dcode";

    private static final String CLIENT_ID = "7bb34a0cf68455d33ec0d994af2330a3f60ee636";
    private static final String REDIRECT_URI = "mapbox-android-dev-preview://authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.mapbox.com/oauth/access_token";

    //  TODO: Fill in CLIENT_SECRET
    private static final String CLIENT_SECRET = "";

    public static final String BASE_URL = "https://api.mapbox.com/api/";
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Button createAccountButton = (Button) findViewById(R.id.create_account_button);
        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChromeCustomTab(true);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChromeCustomTab(false);
            }
        });

        setUpSkipDialog();
    }

    private void openChromeCustomTab(boolean creatingAccount) {

        final String urlToVisit;

        urlToVisit = creatingAccount ? CREATE_ACCOUNT_AUTH_URL : String.format(SIGN_IN_AUTH_URL, CLIENT_ID, REDIRECT_URI);

        Log.d("LandingActivity", "openChromeCustomTab: url = " + urlToVisit);

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.mapboxGrayDark10));
        intentBuilder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(this, Uri.parse(urlToVisit));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if (uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.d("LandingActivity", "An error has occurred : " + error);
            } else {
                String authCode = uri.getQueryParameter("code");
                Log.d("LandingActivity", "onResume: authCode = " + authCode);
                getAccessToken(authCode);
            }
        }
    }

    private void getAccessToken(String code) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Android Dev Preview")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET
                                + "&redirect_uri=" + REDIRECT_URI + "&code=" + code))
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException exception) {
                Log.d("LandingActivity", "onFailure: " + exception);

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    Log.d("LandingActivity", "onResponse: accessToken = " + accessToken);
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

    private void getUserInfo(final String userName, final String token) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MapboxAccountService service = retrofit.create(MapboxAccountService.class);

        retrofit2.Call<UserResponse> request = service.getUserAccount(userName, token);

        request.enqueue(new retrofit2.Callback<UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<UserResponse> call, retrofit2.Response<UserResponse> response) {

                String userId = response.body().getId();
                String emailAddress = response.body().getId();
                String avatarUrl = response.body().getId();

                saveUserInfoToSharedPref(userId, emailAddress, avatarUrl, token);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(retrofit2.Call<UserResponse> call, Throwable throwable) {
                // Log error here since request failed
                Log.d("LandingActivity", "onFailure: throwable = " + throwable);
            }
        });
    }

    private void saveUserInfoToSharedPref(String userId, String emailAddress, String avatarUrl, String token) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putBoolean("TOKEN_SAVED", true)
                .putString("USERNAME", userId)
                .putString("EMAIL", emailAddress)
                .putString("AVATAR_IMAGE_URL", avatarUrl)
                .putString("TOKEN", token)
                .apply();
    }


    private void setUpSkipDialog() {
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

    private void getUsernameFromJwt(String jwtEncoded) throws Exception {

        try {
            String[] split = jwtEncoded.split("\\.");
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]));
            String jwtBody = getJson(split[1]);
            Log.d(TAG, "getUsernameFromJwt: jwtBody = " + jwtBody);

            username = jwtBody.substring(6, jwtBody.length() - 34);
            Log.d(TAG, "getUsernameFromJwt: username = " + username);

        } catch (UnsupportedEncodingException exception) {
            //Error
            exception.printStackTrace();

        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}