package com.mapbox.mapboxandroiddemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.crashlytics.android.Crashlytics;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends ActionBarActivity {

    private MapView mv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        mv = (MapView) findViewById(R.id.mapview);
        mv.setStyle(Style.MAPBOX_STREETS);
        mv.setCenterCoordinate(new LatLng(0, 0));

		// Show user location (purposely not in follow mode)
        mv.setMyLocationEnabled(true);

//		mv.loadFromGeoJSONURL("https://gist.githubusercontent.com/tmcw/10307131/raw/21c0a20312a2833afeee3b46028c3ed0e9756d4c/map.geojson");
        mv.addMarker(new MarkerOptions().title("Edinburgh").snippet("Scotland").position(new LatLng(55.94629, -3.20777)));
        mv.addMarker(new MarkerOptions().title("Stockholm").snippet("Sweden").position(new LatLng(59.32995, 18.06461)));
        mv.addMarker(new MarkerOptions().title("Prague").snippet("Czech Republic").position(new LatLng(50.08734, 14.42112)));
        mv.addMarker(new MarkerOptions().title("Athens").snippet("Greece").position(new LatLng(37.97885, 23.71399)));
        mv.addMarker(new MarkerOptions().title("Tokyo").snippet("Japan").position(new LatLng(35.70247, 139.71588)));
        mv.addMarker(new MarkerOptions().title("Ayacucho").snippet("Peru").position(new LatLng(-13.16658, -74.21608)));
        mv.addMarker(new MarkerOptions().title("Nairobi").snippet("Kenya").position(new LatLng(-1.26676, 36.83372)));
        mv.addMarker(new MarkerOptions().title("Canberra").snippet("Australia").position(new LatLng(-35.30952, 149.12430)));

		Button bugsBut = changeButtonTypeface((Button) findViewById(R.id.bugsButton));
		bugsBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "https://github.com/mapbox/mapbox-gl-native/issues?state=open";
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(i);
			}
		});
        mv.onCreate(savedInstanceState);
	}


    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mv.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mv.onDestroy();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mv.onResume();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mv.onPause();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.menuItemStreets:
                mv.setStyle(Style.MAPBOX_STREETS);
				return true;
			case R.id.menuItemSatellite:
                mv.setStyle(Style.SATELLITE);
				return true;
			case R.id.menuItemEmerald:
                mv.setStyle(Style.EMERALD);
				return true;
			case R.id.menuItemDark:
                mv.setStyle(Style.DARK);
				return true;
            case R.id.menuItemLight:
                mv.setStyle(Style.LIGHT);
                return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    private Button changeButtonTypeface(Button button) {
        return button;
    }

    public LatLng getMapCenter() {
        return mv.getCenterCoordinate();
    }

    public void setMapCenter(LatLng center) {
        mv.setCenterCoordinate(center);
    }

    /**
     * Method to show settings  in alert dialog
     * On pressing Settings button will launch Settings Options - GPS
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getBaseContext());

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getBaseContext().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
