package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.staticimage.v1.MapboxStaticImage;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StaticImageActivity extends AppCompatActivity {

  private static final String TAG = "StaticImageActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mas_static_image);

    ImageView imageView = (ImageView) findViewById(R.id.mapImage);

    MapboxStaticImage staticImage;
    try {
      staticImage = new MapboxStaticImage.Builder()
          .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
          .setUsername(Constants.MAPBOX_USER)
          .setStyleId("satellite-v9")
          .setLon(12.3378) // Image center longitude
          .setLat(45.4338) // Image center Latitude
          .setZoom(13)
          .setWidth(640) // Image width
          .setHeight(360) // Image height
          .setRetina(true) // Retina 2x image will be returned
          .build();

      System.out.println(staticImage.getUrl().toString());
      new DownloadImageTask(imageView).execute(staticImage.getUrl().toString());

    } catch (ServicesException e) {
      Log.e(TAG, "MapboxStaticImage error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;

    public DownloadImageTask(ImageView imageView) {
      this.imageView = imageView;
    }

    protected Bitmap doInBackground(String... urls) {

      // Create OkHttp object
      final OkHttpClient client = new OkHttpClient();

      // Build request
      Request request = new Request.Builder()
          .url(urls[0])
          .build();

      Response response = null;
      Bitmap bitmap = null;
      try {
        // Make request
        response = client.newCall(request).execute();
      } catch (IOException e) {
        e.printStackTrace();
      }

      // If the response is successful, create the static map image
      if (response.isSuccessful()) {
        try {
          bitmap = BitmapFactory.decodeStream(response.body().byteStream());
        } catch (Exception e) {
          Log.e("Error", e.getMessage());
          e.printStackTrace();
        }
      }
      return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
      // Add static map image to imageView
      imageView.setImageBitmap(result);
    }
  }
}