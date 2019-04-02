package com.mapbox.mapboxandroiddemo.examples.basics

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_basic_simple_kotlin.*
import timber.log.Timber

/**
 * Test activity showcasing the Snapshot API to create and display a bitmap of the current shown Map.
 */
class SimpleMapViewActivityKotlin : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapboxMap: MapboxMap

    private val idleListener = object : MapView.OnDidBecomeIdleListener {
        override fun onDidBecomeIdle() {
            mapView.removeOnDidBecomeIdleListener(this)
            Log.v("idle", "Idle listener removed")
            mapboxMap.snapshot { snapshot ->
                imageView.setImageBitmap(snapshot)
                mapView.addOnDidBecomeIdleListener(this)
                Log.v("idle", "Idle listener added")
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_basic_simple_kotlin)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }


    override fun onMapReady(map: MapboxMap) {
        mapboxMap = map
        mapboxMap.setStyle(Style.Builder().fromUrl(Style.OUTDOORS)) { mapView.addOnDidBecomeIdleListener(idleListener) }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapboxMap.snapshot {
            Timber.e("Regression test for https://github.com/mapbox/mapbox-gl-native/pull/11358")
        }
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapView.removeOnDidBecomeIdleListener(idleListener)
        mapView.onDestroy()
    }
}