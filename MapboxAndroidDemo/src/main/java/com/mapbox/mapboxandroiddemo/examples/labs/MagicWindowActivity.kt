package com.mapbox.mapboxandroiddemo.examples.labs

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_magic_window.*
import kotlin.math.max
import kotlin.math.min

const val TAG = "DragActivityTag"
const val PERMISSION_REQUEST_LOCATION = 404
const val BASE_MAP_BUNDLE = "$TAG.basemap.bundle"
const val REVEAL_MAP_BUNDLE = "$TAG.revealedMap.bundle"

class MagicWindowActivity : AppCompatActivity(), LocationEngineListener {
    lateinit var listener: DragListener
    lateinit var locationEngine: LocationEngine
    var base: MapboxMap? = null
    var revealed: MapboxMap? = null
    var initialPosition = LatLng(41.0, 78.0)

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
                base = map
                if (savedInstanceState == null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 8.0))
                }
                map.addOnCameraMoveListener {
                    synchronizeMaps()
                }
            }
            synchronizeMaps()
        }
        revealedMap.getMapAsync({ map: MapboxMap? -> revealed = map })

        parentView.viewTreeObserver.addOnGlobalLayoutListener {
            val yMax = (parentView.height - revealedMap.height).toFloat()
            listener = DragListener(yMin = 0.0f, yMax = yMax )
            listener.setOnDragListener { synchronizeMaps() }
            magicWindow.setOnTouchListener(listener)
            synchronizeMaps()
        }
    }

    fun checkLocationPermissionsAndInitialize() {
        val allowed = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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

    fun setInitialMapPosition(ll: LatLng) {
        initialPosition = ll
        base?.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 8.0))
    }

    @SuppressLint("MissingPermission")
    fun initializeLocationEngine() {
        val provider = LocationEngineProvider(this)
        locationEngine = provider.obtainBestLocationEngineAvailable()
        locationEngine.priority = LocationEnginePriority.BALANCED_POWER_ACCURACY
        locationEngine.activate()

        val last = locationEngine.lastLocation
        if (last != null) {
            locationEngine.deactivate()
            setInitialMapPosition(LatLng(last.latitude, last.longitude))
        } else {
            locationEngine.addLocationEngineListener(this)
        }
    }

    override fun onConnected() {
        // noop
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            setInitialMapPosition(LatLng(location.latitude, location.longitude))
            locationEngine.deactivate()
            locationEngine.removeLocationEngineListener(this)
        }
    }

    fun synchronizeMaps() {
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
    var offsetX = 0.0f
    var offsetY = 0.0f
    var dragging = false
    val listeners: MutableList<() -> Unit> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v != null && event != null) {
            v.requestPointerCapture()

            // restrict dragging to a circular selectable area
            val r = v.width / 2.0f
            val cx = r
            val cy = r
            val delta2 = (event.x - cx) * (event.x - cx) + (event.y - cy) * (event.y - cy)
            if (delta2 > r * r) {
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

    fun constrainX(x: Float): Float {
        return x
    }

    fun constrainY(y: Float): Float {
        return min(max(y, yMin), yMax)
    }

    fun setOnDragListener(listener: () -> Unit) {
        listeners.add(listener)
    }
}

class MaskedView: FrameLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    val strokeWidth = dpToPixels(1.0f)
    val mask by lazy {
        val b = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val size = measuredWidth / 2.0f
        c.drawCircle(size, size, size, paint)
        b
    }

    val paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.isAntiAlias = true
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        p
    }

    val stroke by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = 0xFFFFFFCC.toInt()
        p.strokeWidth = strokeWidth
        p
    }

    fun dpToPixels(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

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