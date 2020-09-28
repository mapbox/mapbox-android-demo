package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

/**
 * Use runtime styling to change the language displayed on the map
 */
public class LanguageSwitchActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_language_switch);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            LanguageSwitchActivity.this.mapboxMap = mapboxMap;
          }
        });
      }
    });
  }

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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_map_langauge, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        SymbolLayer countryLabelTextSymbolLayer = style.getLayerAs("country-label");
        if (countryLabelTextSymbolLayer != null) {
          switch (item.getItemId()) {
            case R.id.french:
              countryLabelTextSymbolLayer.setProperties(textField("{name_fr}"));
              return;
            case R.id.russian:
              countryLabelTextSymbolLayer.setProperties(textField("{name_ru}"));
              return;
            case R.id.german:
              countryLabelTextSymbolLayer.setProperties(textField("{name_de}"));
              return;
            case R.id.spanish:
              countryLabelTextSymbolLayer.setProperties(textField("{name_es}"));
              return;
            case R.id.italian:
              countryLabelTextSymbolLayer.setProperties(textField("{name_it}"));
              return;
            case R.id.vietnamese:
              countryLabelTextSymbolLayer.setProperties(textField("{name_vi}"));
              return;
            case R.id.korean:
              countryLabelTextSymbolLayer.setProperties(textField("{name_ko}"));
              return;
            case R.id.japanese:
              countryLabelTextSymbolLayer.setProperties(textField("{name_ja}"));
              return;
            case R.id.simplified_chinese:
              countryLabelTextSymbolLayer.setProperties(textField("{name_zh-Hans}"));
              return;
            case R.id.traditional_chinese:
              countryLabelTextSymbolLayer.setProperties(textField("{name_zh-Hant}"));
              return;
            default:
              countryLabelTextSymbolLayer.setProperties(textField("{name_en}"));
          }
        }
      }
    });
    return super.onOptionsItemSelected(item);
  }
}
