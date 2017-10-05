package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class MapillaryActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_mapillary);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {
        map = mapboxMap;
        map.addSource(Mapillary.createSource());
        map.addLayerBelow(Mapillary.createLineLayer(), Mapillary.ID_ABOVE_LAYER);
        map.addLayerBelow(Mapillary.createCircleLayer(), Mapillary.ID_LINE_LAYER);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
              new LatLng(55.6089295863, 13.0006076843), 14), 2500);
            new LoadImageIdTask(Picasso.with(getApplicationContext()), mapboxMap).execute();
            view.setVisibility(View.GONE);
          }
        });
      }
    });
  }

  @Override
  protected void onResume() {
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
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private static class Mapillary {

    static final String ID_SOURCE = "mapillary";
    static final String ID_LINE_LAYER = ID_SOURCE + ".line";
    static final String ID_CIRCLE_LAYER = ID_SOURCE + ".circle";
    static final String ID_ABOVE_LAYER = "aerialway";
    static final String URL_TILESET = "https://d25uarhxywzl1j.cloudfront.net/v0.1/{z}/{x}/{y}.mvt";

    static Source createSource() {
      TileSet mapillaryTileset = new TileSet("2.1.0", Mapillary.URL_TILESET);
      mapillaryTileset.setMinZoom(0);
      mapillaryTileset.setMaxZoom(14);
      return new VectorSource(Mapillary.ID_SOURCE, mapillaryTileset);
    }

    static Layer createLineLayer() {
      LineLayer lineLayer = new LineLayer(Mapillary.ID_LINE_LAYER, Mapillary.ID_SOURCE);
      lineLayer.setSourceLayer("mapillary-sequences");
      lineLayer.setProperties(
        lineCap(Property.LINE_CAP_ROUND),
        lineJoin(Property.LINE_JOIN_ROUND),
        lineOpacity(0.6f),
        lineWidth(2.0f),
        lineColor(Color.GREEN)
      );
      return lineLayer;
    }

    static Layer createCircleLayer() {
      CircleLayer circleLayer = new CircleLayer(Mapillary.ID_CIRCLE_LAYER, Mapillary.ID_SOURCE);
      circleLayer.setSourceLayer("mapillary-sequence-overview");
      circleLayer.setProperties(
        circleColor(Color.GREEN),
        circleRadius(4.0f),
        circleOpacity(0.6f)
      );
      return circleLayer;
    }
  }

  private static class LoadImageIdTask extends AsyncTask<Void, Void, DataLoadResult> {

    static final String URL_IMAGE_PLACEHOLDER = "https://d1cuyjsrcm0gby.cloudfront.net/%s/thumb-320.jpg";
    static final String KEY_UNIQUE_FEATURE = "key";
    static final String TOKEN_UNIQUE_FEATURE = "{" + KEY_UNIQUE_FEATURE + "}";
    static final String ID_SOURCE = "cluster_source";
    static final int IMAGE_SIZE = 56;

    private MapboxMap map;
    private Picasso picasso;

    LoadImageIdTask(Picasso picasso, MapboxMap mapboxMap) {
      this.map = mapboxMap;
      this.picasso = picasso;
    }

    @Override
    protected DataLoadResult doInBackground(Void... voids) {
      OkHttpClient okHttpClient = new OkHttpClient();
      try {
        Request request = new Request.Builder()
          .url("https://a.mapillary.com/v3/images/"
            + "?lookat=12.9981086701,55.6075236275&closeto=13.0006076843,55.6089295863&radius=1000"
            + "&client_id=bjgtc1FDTnFPaXpxeTZuUDNabmJ5dzozOGE1ODhkMmEyYTkyZTI4")
          .build();

        Response response = okHttpClient.newCall(request).execute();
        FeatureCollection featureCollection = FeatureCollection.fromJson(response.body().string());
        DataLoadResult dataLoadResult = new DataLoadResult(featureCollection);
        for (Feature feature : featureCollection.getFeatures()) {
          String imageId = feature.getStringProperty(KEY_UNIQUE_FEATURE);
          String imageUrl = String.format(URL_IMAGE_PLACEHOLDER, imageId);
          Bitmap bitmap = picasso.load(imageUrl).resize(IMAGE_SIZE, IMAGE_SIZE).get();
          dataLoadResult.add(feature, bitmap);
        }
        return dataLoadResult;

      } catch (Exception exception) {
        Timber.e(exception);
      }
      return null;
    }

    @Override
    protected void onPostExecute(DataLoadResult dataLoadResult) {
      super.onPostExecute(dataLoadResult);
      FeatureCollection featureCollection = dataLoadResult.featureCollection;

      Map<Feature, Bitmap> bitmapMap = dataLoadResult.bitmapHashMap;
      for (Map.Entry<Feature, Bitmap> featureBitmapEntry : bitmapMap.entrySet()) {
        Feature feature = featureBitmapEntry.getKey();
        String key = feature.getStringProperty(KEY_UNIQUE_FEATURE);
        map.addImage(key, featureBitmapEntry.getValue());
      }

      map.addSource(new GeoJsonSource(ID_SOURCE, featureCollection, new GeoJsonOptions()
        .withCluster(true)
        .withClusterMaxZoom(17)
        .withClusterRadius(IMAGE_SIZE / 2)
      ));

      // unclustered
      map.addLayer(new SymbolLayer("test_layer", ID_SOURCE)
        .withProperties(
          iconImage(TOKEN_UNIQUE_FEATURE),
          iconAllowOverlap(true)
        ));

      // clustered
      int[][] layers = new int[][] {
        new int[] {100, Color.RED},
        new int[] {20, Color.GREEN},
        new int[] {0, Color.BLUE}
      };

      for (int i = 0; i < layers.length; i++) {
        //Add cluster circles
        CircleLayer clusterLayer = new CircleLayer("cluster-" + i, ID_SOURCE);
        clusterLayer.setProperties(
          circleColor(layers[i][1]),
          circleRadius(18f),
          circleOpacity(0.6f)
        );

        // Add a filter to the cluster layer that hides the circles based on "point_count"
        clusterLayer.setFilter(
          i == 0
            ? gte("point_count", layers[i][0]) :
            all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
        );
        map.addLayer(clusterLayer);
      }

      //Add the count labels
      SymbolLayer count = new SymbolLayer("count", ID_SOURCE);
      count.setProperties(
        textField("{point_count}"),
        textSize(8f),
        textOffset(new Float[] {0.0f, 0.0f}),
        textColor(Color.WHITE),
        textIgnorePlacement(true)
      );
      map.addLayer(count);
    }
  }

  private static class DataLoadResult {
    private final HashMap<Feature, Bitmap> bitmapHashMap = new HashMap<>();
    private final FeatureCollection featureCollection;

    DataLoadResult(FeatureCollection featureCollection) {
      this.featureCollection = featureCollection;
    }

    public void add(Feature feature, Bitmap bitmap) {
      bitmapHashMap.put(feature, bitmap);
    }
  }
}