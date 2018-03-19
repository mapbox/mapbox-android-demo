package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import java.util.Arrays;

public class DownloadableFontActivity extends AppCompatActivity implements
  OnMapReadyCallback, AdapterView.OnItemSelectedListener {


  private MapView mapView;
  private MapboxMap mapboxMap;
  private String tag = "DownloadableFontActivity";

  private Handler handler = null;
  private Spinner spinner;
  private ArraySet<String> familyNameSet;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_downloadable_font);

    familyNameSet = new ArraySet<>();
    familyNameSet.addAll(Arrays.asList(getResources().getStringArray(R.array.downloadable_font_names_array)));

    initSpinnerMenu();

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }


  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    DownloadableFontActivity.this.mapboxMap = mapboxMap;

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
      android.R.layout.simple_dropdown_item_1line,
      getResources().getStringArray(R.array.downloadable_font_names_array));

    /*FontRequest request = new FontRequest("com.example.fontprovider",
      "com.example.fontprovider", "my font", certs);
*/
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    String selectedFontName = parent.getItemAtPosition(position).toString();
    if (!isValidFamilyName(selectedFontName)) {
      Toast.makeText(
        DownloadableFontActivity.this,
        R.string.invalid_input,
        Toast.LENGTH_SHORT).show();
      return;
    }
    requestDownload(selectedFontName);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Empty on purpose
  }

  private void requestDownload(String familyName) {
    FontRequest request = new FontRequest(
      "com.google.android.gms.fonts", "com.google.android.gms",
      "name=" + familyName,
      R.array.com_google_android_gms_fonts_certs);

    FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
      .FontRequestCallback() {
      @Override
      public void onTypefaceRetrieved(Typeface typeface) {

        Log.d(tag, "onTypefaceRetrieved: Name of retrieved typeface = " + typeface.toString());
        Log.d(tag, "onTypefaceRetrieved: String.valueOf(typeface.getStyle()) = " + String.valueOf(typeface.getStyle()));

        //        adjustLayers(typeface.toString());
      }

      @Override
      public void onTypefaceRequestFailed(int reason) {
        Toast.makeText(DownloadableFontActivity.this,
          getString(R.string.request_failed, reason), Toast.LENGTH_LONG)
          .show();
      }
    };
    FontsContractCompat
      .requestFont(DownloadableFontActivity.this, request, callback,
        getHandlerThreadHandler());
  }

  private Handler getHandlerThreadHandler() {
    if (handler == null) {
      HandlerThread handlerThread = new HandlerThread("fonts");
      handlerThread.start();
      handler = new Handler(handlerThread.getLooper());
    }
    return handler;
  }

  private boolean isValidFamilyName(String familyName) {
    return familyName != null && familyNameSet.contains(familyName);
  }

  private void adjustLayers(String fontName) {
    mapboxMap.getLayer("country-label-lg").setProperties(PropertyFactory.textFont(new String[] {fontName}));
    mapboxMap.getLayer("marine-label-lg-pt").setProperties(PropertyFactory.textFont(new String[] {fontName}));
  }

  private void initSpinnerMenu() {
    Spinner spinner = findViewById(R.id.font_spinner_menu);

    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
      R.array.downloadable_font_names_array, android.R.layout.simple_spinner_item);

    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}