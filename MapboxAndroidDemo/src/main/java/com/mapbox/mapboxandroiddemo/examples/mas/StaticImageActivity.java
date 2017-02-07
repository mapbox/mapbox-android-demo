package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.Constants;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.staticimage.v1.MapboxStaticImage;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StaticImageActivity extends AppCompatActivity {

  private static final String TAG = "StaticImageActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_mas_static_image);

    ImageView imageView = (ImageView) findViewById(R.id.mapImage);

    MapboxStaticImage staticImage;
    try {
      staticImage = new MapboxStaticImage.Builder()
        .setAccessToken(Mapbox.getAccessToken())
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

    } catch (ServicesException servicesException) {
      Log.e(TAG, "MapboxStaticImage error: " + servicesException.getMessage());
      servicesException.printStackTrace();
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
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }

      // If the response is successful, create the static map image
      if (response != null) {
        if (response.isSuccessful()) {
          try {
            bitmap = BitmapFactory.decodeStream(response.body().byteStream());
          } catch (Exception exception) {
            Log.e("Error", exception.getMessage());
            exception.printStackTrace();
          }
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