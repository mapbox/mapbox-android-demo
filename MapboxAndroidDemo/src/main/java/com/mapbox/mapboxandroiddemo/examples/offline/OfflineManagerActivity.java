package com.mapbox.mapboxandroiddemo.examples.offline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Download, view, navigate to, and delete an offline region.
 */
public class OfflineManagerActivity extends AppCompatActivity {

  private static final String TAG = "OffManActivity";

  // JSON encoding/decoding
  public static final String JSON_CHARSET = "UTF-8";
  public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

  // UI elements
  private MapView mapView;
  private MapboxMap map;
  private ProgressBar progressBar;
  private Button downloadButton;
  private Button listButton;

  private boolean isEndNotified;
  private int regionSelected;

  // Offline objects
  private OfflineManager offlineManager;
  private OfflineRegion offlineRegion;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_offline_manager);

    // Set up the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Assign progressBar for later use
            progressBar = findViewById(R.id.progress_bar);

            // Set up the offlineManager
            offlineManager = OfflineManager.getInstance(OfflineManagerActivity.this);

            // Bottom navigation bar button clicks are handled here.
            // Download offline button
            downloadButton = findViewById(R.id.download_button);
            downloadButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                downloadRegionDialog();
              }
            });

            // List offline regions
            listButton =  findViewById(R.id.list_button);
            listButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                downloadedRegionList();
              }
            });
          }
        });
      }
    });
  }

  // Override Activity lifecycle methods
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
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
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

  private void downloadRegionDialog() {
    // Set up download interaction. Display a dialog
    // when the user clicks download button and require
    // a user-provided region name
    AlertDialog.Builder builder = new AlertDialog.Builder(OfflineManagerActivity.this);

    final EditText regionNameEdit = new EditText(OfflineManagerActivity.this);
    regionNameEdit.setHint(getString(R.string.set_region_name_hint));

    // Build the dialog box
    builder.setTitle(getString(R.string.dialog_title))
      .setView(regionNameEdit)
      .setMessage(getString(R.string.dialog_message))
      .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          String regionName = regionNameEdit.getText().toString();
          // Require a region name to begin the download.
          // If the user-provided string is empty, display
          // a toast message and do not begin download.
          if (regionName.length() == 0) {
            Toast.makeText(OfflineManagerActivity.this, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
          } else {
            // Begin download process
            downloadRegion(regionName);
          }
        }
      })
      .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.cancel();
        }
      });

    // Display the dialog
    builder.show();
  }

  private void downloadRegion(final String regionName) {
    // Define offline region parameters, including bounds,
    // min/max zoom, and metadata

    // Start the progressBar
    startProgress();

    // Create offline definition using the current
    // style and boundaries of visible map area

    map.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        String styleUrl = style.getUri();
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        double minZoom = map.getCameraPosition().zoom;
        double maxZoom = map.getMaxZoomLevel();
        float pixelRatio = OfflineManagerActivity.this.getResources().getDisplayMetrics().density;
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
          styleUrl, bounds, minZoom, maxZoom, pixelRatio);

        // Build a JSONObject using the user-defined offline region title,
        // convert it into string, and use it to create a metadata variable.
        // The metadata variable will later be passed to createOfflineRegion()
        byte[] metadata;
        try {
          JSONObject jsonObject = new JSONObject();
          jsonObject.put(JSON_FIELD_REGION_NAME, regionName);
          String json = jsonObject.toString();
          metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
          Timber.e("Failed to encode metadata: %s", exception.getMessage());
          metadata = null;
        }

        // Create the offline region and launch the download
        offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
          @Override
          public void onCreate(OfflineRegion offlineRegion) {
            Timber.d( "Offline region created: %s" , regionName);
            OfflineManagerActivity.this.offlineRegion = offlineRegion;
            launchDownload();
          }

          @Override
          public void onError(String error) {
            Timber.e( "Error: %s" , error);
          }
        });
      }
    });
  }

  private void launchDownload() {
    // Set up an observer to handle download progress and
    // notify the user when the region is finished downloading
    offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
      @Override
      public void onStatusChanged(OfflineRegionStatus status) {
        // Compute a percentage
        double percentage = status.getRequiredResourceCount() >= 0
          ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
          0.0;

        if (status.isComplete()) {
          // Download complete
          endProgress(getString(R.string.end_progress_success));
          return;
        } else if (status.isRequiredResourceCountPrecise()) {
          // Switch to determinate state
          setPercentage((int) Math.round(percentage));
        }

        // Log what is being currently downloaded
        Timber.d("%s/%s resources; %s bytes downloaded.",
          String.valueOf(status.getCompletedResourceCount()),
          String.valueOf(status.getRequiredResourceCount()),
          String.valueOf(status.getCompletedResourceSize()));
      }

      @Override
      public void onError(OfflineRegionError error) {
        Timber.e("onError reason: %s", error.getReason());
        Timber.e("onError message: %s", error.getMessage());
      }

      @Override
      public void mapboxTileCountLimitExceeded(long limit) {
        Timber.e("Mapbox tile count limit exceeded: %s", limit);
      }
    });

    // Change the region state
    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
  }

  private void downloadedRegionList() {
    // Build a region list when the user clicks the list button

    // Reset the region selected int to 0
    regionSelected = 0;

    // Query the DB asynchronously
    offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
      @Override
      public void onList(final OfflineRegion[] offlineRegions) {
        // Check result. If no regions have been
        // downloaded yet, notify user and return
        if (offlineRegions == null || offlineRegions.length == 0) {
          Toast.makeText(getApplicationContext(), getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
          return;
        }

        // Add all of the region names to a list
        ArrayList<String> offlineRegionsNames = new ArrayList<>();
        for (OfflineRegion offlineRegion : offlineRegions) {
          offlineRegionsNames.add(getRegionName(offlineRegion));
        }
        final CharSequence[] items = offlineRegionsNames.toArray(new CharSequence[offlineRegionsNames.size()]);

        // Build a dialog containing the list of regions
        AlertDialog dialog = new AlertDialog.Builder(OfflineManagerActivity.this)
          .setTitle(getString(R.string.navigate_title))
          .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // Track which region the user selects
              regionSelected = which;
            }
          })
          .setPositiveButton(getString(R.string.navigate_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

              Toast.makeText(OfflineManagerActivity.this, items[regionSelected], Toast.LENGTH_LONG).show();

              // Get the region bounds and zoom
              LatLngBounds bounds = (offlineRegions[regionSelected].getDefinition()).getBounds();
              double regionZoom = (offlineRegions[regionSelected].getDefinition()).getMinZoom();

              // Create new camera position
              CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(bounds.getCenter())
                .zoom(regionZoom)
                .build();

              // Move camera to new position
              map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
          })
          .setNeutralButton(getString(R.string.navigate_neutral_button_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              // Make progressBar indeterminate and
              // set it to visible to signal that
              // the deletion process has begun
              progressBar.setIndeterminate(true);
              progressBar.setVisibility(View.VISIBLE);

              // Begin the deletion process
              offlineRegions[regionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                @Override
                public void onDelete() {
                  // Once the region is deleted, remove the
                  // progressBar and display a toast
                  progressBar.setVisibility(View.INVISIBLE);
                  progressBar.setIndeterminate(false);
                  Toast.makeText(getApplicationContext(), getString(R.string.toast_region_deleted),
                    Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String error) {
                  progressBar.setVisibility(View.INVISIBLE);
                  progressBar.setIndeterminate(false);
                  Timber.e( "Error: %s", error);
                }
              });
            }
          })
          .setNegativeButton(getString(R.string.navigate_negative_button_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              // When the user cancels, don't do anything.
              // The dialog will automatically close
            }
          }).create();
        dialog.show();

      }

      @Override
      public void onError(String error) {
        Timber.e( "Error: %s", error);
      }
    });
  }

  private String getRegionName(OfflineRegion offlineRegion) {
    // Get the region name from the offline region metadata
    String regionName;

    try {
      byte[] metadata = offlineRegion.getMetadata();
      String json = new String(metadata, JSON_CHARSET);
      JSONObject jsonObject = new JSONObject(json);
      regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
    } catch (Exception exception) {
      Timber.e("Failed to decode metadata: %s", exception.getMessage());
      regionName = String.format(getString(R.string.region_name), offlineRegion.getID());
    }
    return regionName;
  }

  // Progress bar methods
  private void startProgress() {
    // Disable buttons
    downloadButton.setEnabled(false);
    listButton.setEnabled(false);

    // Start and show the progress bar
    isEndNotified = false;
    progressBar.setIndeterminate(true);
    progressBar.setVisibility(View.VISIBLE);
  }

  private void setPercentage(final int percentage) {
    progressBar.setIndeterminate(false);
    progressBar.setProgress(percentage);
  }

  private void endProgress(final String message) {
    // Don't notify more than once
    if (isEndNotified) {
      return;
    }

    // Enable buttons
    downloadButton.setEnabled(true);
    listButton.setEnabled(true);

    // Stop and hide the progress bar
    isEndNotified = true;
    progressBar.setIndeterminate(false);
    progressBar.setVisibility(View.GONE);

    // Show a toast
    Toast.makeText(OfflineManagerActivity.this, message, Toast.LENGTH_LONG).show();
  }
}
