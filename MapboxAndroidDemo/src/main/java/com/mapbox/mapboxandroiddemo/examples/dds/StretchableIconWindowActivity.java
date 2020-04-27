package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.ImageContent;
import com.mapbox.mapboxsdk.maps.ImageStretches;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.format;
import static com.mapbox.mapboxsdk.style.expressions.Expression.formatEntry;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_TEXT_FIT_BOTH;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTextFit;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;

/**
 * Use a SymbolLayer to show a BubbleLayout above a SymbolLayer icon. This is a more performant
 * way to show the BubbleLayout that appears when using the MapboxMap.addMarker() method.
 */
public class StretchableIconWindowActivity extends AppCompatActivity implements OnMapReadyCallback {
  private static final String POPUP_IMAGE_ID = "popup";
  private static final String STRETCH_SOURCE = "STRETCH_SOURCE";
  private static final String STRETCH_LAYER = "STRETCH_LAYER";
  private MapboxMap mapboxMap;
  private MapView mapView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dds_stretchable_icon);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
      Bitmap popupBitMap = BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.popup));
      if (popupBitMap != null) {

        // The two (blue) columns of pixels that can be stretched horizontally:
        //   - the pixels between x: 25 and x: 55 can be stretched
        //   - the pixels between x: 85 and x: 115 can be stretched.
        List<ImageStretches> stretchX = new ArrayList<>();
        stretchX.add(new ImageStretches(25, 55));
        stretchX.add(new ImageStretches(85, 115));

        // The one (red) row of pixels that can be stretched vertically:
        //   - the pixels between y: 25 and y: 100 can be stretched
        List<ImageStretches> stretchY = new ArrayList<>();
        stretchY.add(new ImageStretches(5, 120));

        // This part of the image that can contain text ([x1, y1, x2, y2]):
        ImageContent content = new ImageContent(25, 25, 115, 100);

        style.addImage(POPUP_IMAGE_ID, popupBitMap, stretchX, stretchY, content);
      }
      new LoadFeatureTask(StretchableIconWindowActivity.this).execute();
    });
  }

  private void onFeatureLoaded(String json) {
    if (json == null) {
      Timber.e("json is null.");
      return;
    }
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Set up label formatting expression
        Expression.FormatEntry bigLabel = formatEntry(
            get("capital")
        );

        Expression.FormatEntry comma = formatEntry(" is the capital of");

        Expression.FormatEntry newLine = formatEntry(
            // Add "\n" in order to break the line and have the second label underneath
            "\n"
        );

        Expression.FormatEntry smallLabel = formatEntry(
            get("name")
        );

        Expression format = format(bigLabel, comma, newLine, smallLabel);

        GeoJsonSource stretchSource = new GeoJsonSource(STRETCH_SOURCE,
            FeatureCollection.fromJson(json));
        style.addSource(stretchSource);
        style.addLayer(new SymbolLayer(STRETCH_LAYER, STRETCH_SOURCE)
            .withProperties(
                textField(format),
                iconImage(POPUP_IMAGE_ID),
                iconAllowOverlap(true),
                textAllowOverlap(true),
                textIgnorePlacement(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[]{0f, -31f}),
//                textOffset(new Float[]{0f, -29f}),
                iconTextFit(ICON_TEXT_FIT_BOTH)));
      }
    });
  }

  private static class LoadFeatureTask extends AsyncTask<Void, Integer, String> {

    private WeakReference<StretchableIconWindowActivity> activity;

    private LoadFeatureTask(StretchableIconWindowActivity activity) {
      this.activity = new WeakReference<>(activity);
    }

    @Override
    protected String doInBackground(Void... params) {
      StretchableIconWindowActivity activity = this.activity.get();
      if (activity != null) {
        String json = null;
        json = activity.loadJsonFromAsset("us_west_coast.geojson");
        return json;
      }
      return null;
    }

    @Override
    protected void onPostExecute(String json) {
      super.onPostExecute(json);
      StretchableIconWindowActivity activity = this.activity.get();
      if (activity != null) {
        activity.onFeatureLoaded(json);
      }
    }
  }

  private String loadJsonFromAsset(String filename) {
    // Using this method to load in GeoJSON files from the assets folder.

    try {
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, Charset.forName("UTF-8"));

    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }
}