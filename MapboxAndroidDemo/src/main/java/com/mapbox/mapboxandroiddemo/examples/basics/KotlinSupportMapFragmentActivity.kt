package com.mapbox.mapboxandroiddemo.examples.basics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.SupportMapFragment

/**
 * Use Kotlin to add a map into a fragment container.
 */
class KotlinSupportMapFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_kotlin_support_map_frag)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // Create supportMapFragment
        val mapFragment: SupportMapFragment
        if (savedInstanceState == null) {

            // Create fragment
            val transaction = supportFragmentManager.beginTransaction()

            // Build mapboxMap
            val options = MapboxMapOptions.createFromAttributes(this, null)
            options.camera(CameraPosition.Builder()
                    .target(LatLng(-52.6885, -70.1395))
                    .zoom(9.0)
                    .build())

            // Create map fragment
            mapFragment = SupportMapFragment.newInstance(options)

            // Add map fragment to parent container
            transaction.add(R.id.container, mapFragment, "com.mapbox.map")
            transaction.commit()
        } else {
            mapFragment = supportFragmentManager.findFragmentByTag("com.mapbox.map") as SupportMapFragment
        }

        mapFragment.getMapAsync {
            mapboxMap -> mapboxMap.setStyle(Style.SATELLITE) {

            // Map in fragment container is set up and the style has loaded.
            // Now you can add data or make other map adjustments.
            }
        }
    }
}