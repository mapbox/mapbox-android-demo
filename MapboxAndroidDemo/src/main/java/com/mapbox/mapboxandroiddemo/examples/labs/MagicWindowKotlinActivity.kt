package com.mapbox.mapboxandroiddemo.examples.labs

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mapbox.android.core.location.*
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_magic_window.*
import kotlin.math.max
import kotlin.math.min

/**
 * Pan around a map with a magic window into a satellite view of the same location.
 *
 * Demonstrates:
 * Using multiple maps and styles in a single activity
 * Controlling which user interactions are enabled on a map
 * Rendering maps to texture for more flexible compositing
 * Showing the user's location on first launch
 *
 * Moving the magic window requires Android O and above.
 */
class MagicWindowKotlinActivity : AppCompatActivity(), LocationEngineCallback<LocationEngineResult> {
    private lateinit var listener: DragListener
    private lateinit var locationEngine: LocationEngine
    private var base: MapboxMap? = null
    private var revealed: MapboxMap? = null
    private var initialPosition = LatLng(39.0, -77.0)
    private var initialZoom = 8.0

    companion object {
        const val TAG = "DragActivityTag"
        const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
        const val PERMISSION_REQUEST_LOCATION = 404
        const val BASE_MAP_BUNDLE = "$TAG.basemap.bundle"
        const val REVEAL_MAP_BUNDLE = "$TAG.revealedMap.bundle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.access_token))
        setContentView(R.layout.activity_magic_window)

        if (savedInstanceState == null) {
            checkLocationPermissionsAndInitialize()
        }
        baseMap.onCreate(savedInstanceState?.getBundle(BASE_MAP_BUNDLE))
        revealedMap.onCreate(savedInstanceState?.getBundle(REVEAL_MAP_BUNDLE))

        baseMap.getMapAsync { map: MapboxMap? ->
            if (map != null) {
                map.setStyle(Style.DARK)
                base = map
                if (savedInstanceState == null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, initialZoom))
                }
                map.addOnCameraMoveListener {
                    synchronizeMaps()
                }
            }
            synchronizeMaps()
        }

        revealedMap.getMapAsync { map: MapboxMap? ->
            if (map != null) {
                revealed = map
                map.setStyle(Style.SATELLITE)
            }
        }

        parentView.viewTreeObserver.addOnGlobalLayoutListener {
            val yMax = (parentView.height - revealedMap.height).toFloat()
            listener = DragListener(yMin = 0.0f, yMax = yMax)
            listener.setOnDragListener { synchronizeMaps() }
            magicWindow.setOnTouchListener(listener)
            synchronizeMaps()
        }
    }

    private fun checkLocationPermissionsAndInitialize() {
        val allowed = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (allowed) {
            initializeLocationEngine()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeLocationEngine()
                }
            }
            else -> {
                // not something we requested
            }
        }
    }

    private fun setInitialMapPosition(ll: LatLng) {
        initialPosition = ll
        initialZoom = 9.0
        base?.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 12.0))
    }

    @SuppressLint("MissingPermission")
    fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)

        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()

        locationEngine.requestLocationUpdates(request, this, mainLooper)

        locationEngine.getLastLocation(this)
    }

    override fun onSuccess(result: LocationEngineResult?) {
        if (result != null) {
            setInitialMapPosition(LatLng(result.lastLocation!!.latitude,
                    result.lastLocation!!.longitude))
            locationEngine.removeLocationUpdates(this)
        }
    }

    override fun onFailure(exception: Exception) {
        Toast.makeText(this, getString(R.string.could_not_get_location),
                Toast.LENGTH_SHORT).show()
    }

    private fun synchronizeMaps() {
        val base = base
        val revealed = revealed

        if (base != null && revealed != null) {
            // convert x/y position into a lon/lat for camera
            val x = magicWindow.x + magicWindow.width / 2.0f
            val y = magicWindow.y + magicWindow.height / 2.0f
            val ll = base.projection.fromScreenLocation(PointF(x, y))

            revealed.cameraPosition = CameraPosition.Builder()
                    .target(ll)
                    .zoom(base.cameraPosition.zoom)
                    .tilt(base.cameraPosition.tilt)
                    .bearing(base.cameraPosition.bearing)
                    .build()
        }
    }

    public override fun onStart() {
        super.onStart()
        baseMap.onStart()
        revealedMap.onStart()
    }

    public override fun onResume() {
        super.onResume()
        baseMap.onResume()
        revealedMap.onResume()
    }

    public override fun onPause() {
        super.onPause()
        baseMap.onPause()
        revealedMap.onPause()
    }

    public override fun onStop() {
        super.onStop()
        baseMap.onStop()
        revealedMap.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        baseMap.onLowMemory()
        revealedMap.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        baseMap.onDestroy()
        revealedMap.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val baseMapBundle = Bundle()
        baseMap.onSaveInstanceState(baseMapBundle)
        val revealedMapBundle = Bundle()
        revealedMap.onSaveInstanceState(revealedMapBundle)
        outState.putBundle(BASE_MAP_BUNDLE, baseMapBundle)
        outState.putBundle(REVEAL_MAP_BUNDLE, revealedMapBundle)
    }
}

class DragListener(val yMax: Float = Float.POSITIVE_INFINITY, val yMin: Float = Float.NEGATIVE_INFINITY) : View.OnTouchListener {
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var dragging = false
    private val listeners: MutableList<() -> Unit> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v != null && event != null) {
            v.requestPointerCapture()

            // restrict dragging to a circular selectable area
            val restrictWidth = v.width / 2.0f
            val delta2 = (event.x - restrictWidth) * (event.x - restrictWidth) +
                    (event.y - restrictWidth) * (event.y - restrictWidth)
            if (delta2 > restrictWidth * restrictWidth) {
                dragging = false
                return false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    offsetX = event.rawX - v.x
                    offsetY = event.rawY - v.y
                    dragging = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (dragging) {
                        v.x = constrainX(event.rawX - offsetX)
                        v.y = constrainY(event.rawY - offsetY)
                        listeners.forEach { listener -> listener() }
                    } else {
                        return false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.releasePointerCapture()
                    dragging = false
                }
            }
        }
        // consumed event
        return true
    }

    private fun constrainX(x: Float): Float {
        return x
    }

    private fun constrainY(y: Float): Float {
        return min(max(y, yMin), yMax)
    }

    fun setOnDragListener(listener: () -> Unit) {
        listeners.add(listener)
    }
}

class MaskedView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val strokeWidth = dpToPixels(1.0f)
    private val mask by lazy {
        val b = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val size = measuredWidth / 2.0f
        c.drawCircle(size, size, size, paint)
        b
    }

    private val paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.isAntiAlias = true
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        p
    }

    private val stroke by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = 0xFFFFFFCC.toInt()
        p.strokeWidth = strokeWidth
        p
    }

    private fun dpToPixels(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (canvas != null) {
            canvas.drawBitmap(mask, 0.0f, 0.0f, paint)
            val size = measuredWidth / 2.0f
            canvas.drawCircle(size, size, size - strokeWidth / 2.0f, stroke)
        }
    }

    // Prevent children of masked view from receiving touch events
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = true
}