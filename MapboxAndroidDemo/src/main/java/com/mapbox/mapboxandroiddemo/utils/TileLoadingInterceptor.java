package com.mapbox.mapboxandroiddemo.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.StringDef;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mapbox.mapboxandroiddemo.BuildConfig;
import com.mapbox.mapboxsdk.Mapbox;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This Interceptor allows to measure time spent getting a response object over network.
 * The following data will be collected:
 *  responseCode, elapsedMS
 *  requestUrl (request string till the question mark),
 *  and device metadata.
 */
public class TileLoadingInterceptor implements Interceptor {

  private static String metadata = null;

  @StringDef( {CONNECTION_NONE, CONNECTION_CELLULAR, CONNECTION_WIFI})
  @Retention(RetentionPolicy.SOURCE)
  @interface ConnectionState {
  }
  private static final String CONNECTION_NONE = "none";
  private static final String CONNECTION_CELLULAR = "cellular";
  private static final String CONNECTION_WIFI = "wifi";

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    long elapsed = System.nanoTime();

    Response response = chain.proceed(request);
    elapsed = System.nanoTime() - elapsed;

    triggerPerformanceEvent(response, elapsed / 1000000);

    return response;
  }

  private void triggerPerformanceEvent(Response response, long elapsedMs) {

    String connectionState = getWifiState();
    List<Attribute<String>> attributes = new ArrayList<>();
    String request = getUrl(response.request());
    attributes.add(
            new Attribute<>("requestUrl", request));
    attributes.add(
            new Attribute<>("responseCode", String.valueOf(response.code())));
    attributes.add(
            new Attribute<>("connectionState", connectionState));

    List<Attribute<Long>> counters = new ArrayList<>();
    counters.add(new Attribute<>("elapsedMS", elapsedMs));

    Bundle bundle = new Bundle();
    Gson gson = new Gson();
    bundle.putString("attributes", gson.toJson(attributes));
    bundle.putString("counters", gson.toJson(counters));
    bundle.putString("metadata", getMetadata());

    if (Mapbox.getTelemetry() != null) {
      Mapbox.getTelemetry().onPerformanceEvent(bundle);
      Mapbox.getTelemetry().setUserTelemetryRequestState(true);
    }
  }

  private static String getUrl(Request request) {
    String url = request.url().toString();
    int questionIndex = url.indexOf('?');
    return questionIndex > 0 ? url.substring(0, questionIndex) : url;
  }

  private static String getMetadata() {
    if (metadata == null) {
      JsonObject metaData = new JsonObject();
      metaData.addProperty("os", "android");
      metaData.addProperty("manufacturer", Build.MANUFACTURER);
      metaData.addProperty("brand", Build.BRAND);
      metaData.addProperty("device", Build.MODEL);
      metaData.addProperty("version", Build.VERSION.RELEASE);
      metaData.addProperty("abi", Build.CPU_ABI);
      try {
        metaData.addProperty("country", Locale.getDefault().getISO3Country());
      } catch (MissingResourceException exception) {
        metaData.addProperty("country", "none");
      }
      metaData.addProperty("ram", getRam());
      metaData.addProperty("screenSize", getWindowSize());
      metaData.addProperty("buildFlavor", BuildConfig.FLAVOR);
      metadata = metaData.toString();
    }
    return metadata;
  }

  private static String getRam() {
    ActivityManager actManager =
            (ActivityManager) Mapbox.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
    if (actManager != null) {
      actManager.getMemoryInfo(memInfo);
    }
    return String.valueOf(memInfo.totalMem);
  }

  private static String getWindowSize() {
    WindowManager windowManager =
            (WindowManager) Mapbox.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    DisplayMetrics metrics = new DisplayMetrics();
    display.getMetrics(metrics);
    int width = metrics.widthPixels;
    int height = metrics.heightPixels;

    return "{" + width + "," + height + "}";
  }

  @ConnectionState
  private static String getWifiState() {
    Context appContext = Mapbox.getApplicationContext();
    ConnectivityManager connectivityManager =
            (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (connectivityManager != null) {
        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (capabilities != null) {
          if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return CONNECTION_WIFI;
          } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return CONNECTION_CELLULAR;
          }
        }
      }
    } else {
      if (connectivityManager != null) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
          if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            return CONNECTION_WIFI;
          } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
            return CONNECTION_CELLULAR;
          }
        }
      }
    }
    return CONNECTION_NONE;
  }

  private static class Attribute<T> {
    private String name;
    private T value;

    Attribute(String name, T value) {
      this.name = name;
      this.value = value;
    }
  }
}
