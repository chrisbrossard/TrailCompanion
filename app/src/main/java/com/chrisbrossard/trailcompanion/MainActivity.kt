
package com.chrisbrossard.trailcompanion

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.PRESSURE_STANDARD_ATMOSPHERE
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.IntentSanitizer
import androidx.navigation.compose.rememberNavController
import com.chrisbrossard.trailcompanion.data.AltitudeSampleDao
import com.chrisbrossard.trailcompanion.data.AltitudeSessionDao
import com.chrisbrossard.trailcompanion.data.AppDatabase
import com.chrisbrossard.trailcompanion.service.AltitudeStepsService
import com.chrisbrossard.trailcompanion.service.LocationService
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeDeleteViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.DistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.PressureViewModel
import com.chrisbrossard.trailcompanion.viewmodel.HeadingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.VerticalSpeedViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import androidx.core.content.edit
import com.chrisbrossard.trailcompanion.data.LocationSample
import com.chrisbrossard.trailcompanion.data.LocationSampleDao
import com.chrisbrossard.trailcompanion.data.LocationSessionDao
import com.chrisbrossard.trailcompanion.ui.theme.TrailCompanionTheme
import com.chrisbrossard.trailcompanion.viewmodel.ChartDistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSampleViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationViewModel
import com.chrisbrossard.trailcompanion.viewmodel.NavigationViewModel
import com.chrisbrossard.trailcompanion.viewmodel.SeaLevelPressureViewModel

class MainActivity : ComponentActivity(), SensorEventListener {
    val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { _ ->
            // permissions granted is not always correct.
            // Permissions must be checked manually
            var locationGranted = false
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationGranted = true
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                locationGranted = true
            }

            var postGranted = false
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                postGranted = true
            }

            var activityGranted = false
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                activityGranted = true
            }

            if (locationGranted && postGranted && activityGranted) {
                setContent {
                    val navController = rememberNavController()
                    TrailCompanionTheme {
                        Navigation(
                            client = fusedLocationProviderClient,
                            mutableGnssStatus,
                            magnetometerAccuracy.intValue,
                            altitudeSampleDao,
                            altitudeSessionDao,
                            altitudeListViewModel,
                            altitudeSessionCountViewModel,
                            altitudeSessionListViewModel,
                            altitudeSessionIdViewModel,
                            altitudeRecordingViewModel,
                            navController,
                            headingViewModel,
                            verticalSpeedViewModel,
                            pressureViewModel,
                            distanceViewModel,
                            locationListViewModel,
                            locationRecordingViewModel,
                            locationSessionIdViewModel,
                            locationSessionDao,
                            locationSessionListViewModel,
                            locationSampleDao,
                            locationSessionCountViewModel,
                            chartDistanceViewModel,
                            seaLevelPressureViewModel,
                        )
                    }
                }
                intent = Intent(this, LocationService::class.java)
                ContextCompat.startForegroundService(this, intent)

                intent = Intent(this, AltitudeStepsService::class.java)
                ContextCompat.startForegroundService(this, intent)
            }
        }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null
    private val gravity = FloatArray(3)
    private val magnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    val arraySize = 40
    val accelerationX = FloatArray(arraySize)
    val accelerationY = FloatArray(arraySize)
    val accelerationZ = FloatArray(arraySize)
    val magneticX = FloatArray(arraySize)
    val magneticY = FloatArray(arraySize)
    val magneticZ = FloatArray(arraySize)

    val altitudeDeque = ArrayDeque<Float>()

    val altitudeTimeDeque = ArrayDeque<Long>()
    val smoothedAltitudeDeque = ArrayDeque<Float>()
    var altitudeStartTime = 0L
    private var pressureSensor: Sensor? = null
    private var locationManager: LocationManager? = null
    lateinit var gnssStatusCallback: GnssStatus.Callback
    val magnetometerAccuracy = mutableIntStateOf(0)
    var mutableGnssStatus by mutableStateOf<GnssStatus?>(null)

    lateinit var database: AppDatabase

    lateinit var altitudeSampleDao: AltitudeSampleDao
    lateinit var altitudeListViewModel: AltitudeListViewModel
    lateinit var altitudeSessionDao: AltitudeSessionDao
    lateinit var altitudeSessionCountViewModel: AltitudeSessionCountViewModel
    lateinit var altitudeSessionListViewModel: AltitudeSessionListViewModel
    lateinit var altitudeSessionIdViewModel: AltitudeSessionIdViewModel
    lateinit var altitudeDeleteViewModel: AltitudeDeleteViewModel
    lateinit var altitudeRecordingViewModel: AltitudeRecordingViewModel
    lateinit var locationSampleDao: LocationSampleDao
    lateinit var locationSessionDao: LocationSessionDao
    lateinit var locationListViewModel: LocationListViewModel
    lateinit var locationRecordingViewModel: LocationRecordingViewModel
    lateinit var locationSessionIdViewModel: LocationSessionIdViewModel
    lateinit var locationSessionListViewModel: LocationSessionListViewModel
    lateinit var locationSessionCountViewModel: LocationSessionCountViewModel
    lateinit var locationSampleViewModel: LocationSampleViewModel

    lateinit var navigationViewModel: NavigationViewModel

    enum class Recording {
        OFF, STARTING, ON
    }

    private val headingViewModel: HeadingViewModel by viewModels()
    private val verticalSpeedViewModel: VerticalSpeedViewModel by viewModels()
    private val pressureViewModel: PressureViewModel by viewModels()
    private val seaLevelPressureViewModel: SeaLevelPressureViewModel by viewModels()
    private val distanceViewModel: DistanceViewModel by viewModels()
    private val chartDistanceViewModel: ChartDistanceViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    private var currentLocation: Location = Location("")
    var locationStartTime = 0L

    @OptIn(DelicateCoroutinesApi::class)
    val locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = Location("")
            location.latitude = intent.getDoubleExtra("latitude", 0.0)
            location.longitude = intent.getDoubleExtra("longitude", 0.0)
            location.altitude = intent.getDoubleExtra("altitude", 0.0)

            locationViewModel.updateLocation(location)

            val sharedPreferences = getSharedPreferences("my_app", MODE_PRIVATE)
            val locationRecording = sharedPreferences.getInt(
                "location_recording",
                -1
            )
            val locationSessionId = sharedPreferences.getLong(
                "location_session_id",
                -1L)

            val now = System.currentTimeMillis()
            if (locationRecording == Recording.STARTING.ordinal) {
                locationStartTime = now
            }

            if (locationRecording != Recording.OFF.ordinal) {
                val serviceScope2 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                serviceScope2.launch {
                    try {
                        locationSampleDao.insert(
                            LocationSample(
                                sessionId = locationSessionId,
                                time = now - locationStartTime,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                x = 0f
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("Location and Compass", "location sample insert failed", e)
                    }
                }
                sharedPreferences.edit {
                    putInt("location_recording", Recording.ON.ordinal)
                }
            }

            if (currentLocation.latitude != 0.0) {
                val delta = location.distanceTo(currentLocation)
                distanceViewModel.updateDistance(delta)
            }
            currentLocation = location
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(
            locationReceiver,
            IntentFilter("com.chrisbrossard.trailcompanion.location"),
            RECEIVER_EXPORTED
        )

        Log.d("Trail Companion", "OnCreate called")

        database = AppDatabase.getInstance(this)

        altitudeSampleDao = database.altitudeSampleDao()
        altitudeListViewModel = AltitudeListViewModel(altitudeSampleDao)

        altitudeSessionDao = database.altitudeSessionDao()
        altitudeSessionCountViewModel = AltitudeSessionCountViewModel(altitudeSessionDao)
        altitudeSessionListViewModel = AltitudeSessionListViewModel(altitudeSessionDao)
        altitudeSessionIdViewModel = AltitudeSessionIdViewModel(application)
        altitudeDeleteViewModel = AltitudeDeleteViewModel(altitudeSampleDao, altitudeSessionDao)
        altitudeRecordingViewModel = AltitudeRecordingViewModel(application)

        locationSampleDao = database.locationSampleDao()
        locationSessionDao = database.locationSessionDao()
        locationListViewModel = LocationListViewModel(locationSampleDao)
        locationRecordingViewModel = LocationRecordingViewModel(application)
        locationSessionIdViewModel = LocationSessionIdViewModel(application)
        locationSessionListViewModel = LocationSessionListViewModel(locationSessionDao)
        locationSessionCountViewModel = LocationSessionCountViewModel(locationSessionDao)
        locationSampleViewModel = LocationSampleViewModel(locationSampleDao)

        navigationViewModel = NavigationViewModel()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        gnssStatusCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(gnssStatus: GnssStatus) {
                mutableGnssStatus = gnssStatus
            }

            override fun onStarted() {

            }

            override fun onStopped() {

            }
        }

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS
        )

        var locationGranted = false
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationGranted = true
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationGranted = true
        }

        var postGranted = false
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            postGranted = true
        }

        var activityGranted = false
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            activityGranted = true
        }

        if (locationGranted && postGranted && activityGranted) {
            setContent {
                val navController = rememberNavController()
                TrailCompanionTheme {
                    Navigation(
                        client = fusedLocationProviderClient,
                        mutableGnssStatus,
                        magnetometerAccuracy.intValue,
                        altitudeSampleDao,
                        altitudeSessionDao,
                        altitudeListViewModel,
                        altitudeSessionCountViewModel,
                        altitudeSessionListViewModel,
                        altitudeSessionIdViewModel,
                        altitudeRecordingViewModel,
                        navController,
                        headingViewModel,
                        verticalSpeedViewModel,
                        pressureViewModel,
                        distanceViewModel,
                        locationListViewModel,
                        locationRecordingViewModel,
                        locationSessionIdViewModel,
                        locationSessionDao,
                        locationSessionListViewModel,
                        locationSampleDao,
                        locationSessionCountViewModel,
                        chartDistanceViewModel,
                        seaLevelPressureViewModel
                    )
                }
            }

            intent = Intent(this, AltitudeStepsService::class.java)
            var safeIntent = IntentSanitizer.Builder()
                .allowAnyComponent()
                .build()
                .sanitize(intent) { string ->
                    Log.d("Location and Compass", string)
                }
            ContextCompat.startForegroundService(this, safeIntent)
            intent = Intent(this, LocationService::class.java)
            safeIntent = IntentSanitizer.Builder()
                .allowAnyComponent()
                .build()
                .sanitize(intent) { string ->
                    Log.d("Location and Compass", string)
                }
            ContextCompat.startForegroundService(this, safeIntent)
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Trail Companion", "onStart() called ")
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        // This code has to be here because onResume() and onPause() are called
        // as part of the permission dialog.
        // We don't want these functions to do anything until permissions have been granted
        val fineRequiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        val fineMissingPermissions = fineRequiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) !=
                    PackageManager.PERMISSION_GRANTED
        }
        val coarseRequiredPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        val coarseMissingPermissions = coarseRequiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) !=
                    PackageManager.PERMISSION_GRANTED
        }
        if (fineMissingPermissions.isEmpty() || coarseMissingPermissions.isEmpty()) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magneticField?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            pressureSensor?.also { sensor ->
                sensorManager.registerListener(
                    this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                locationManager?.registerGnssStatusCallback(gnssStatusCallback, null)
            }
        } else {
            val requiredPermissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.POST_NOTIFICATIONS,
            )
            if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Location permission")
                builder.setMessage("Location permission required for calculating " +
                        "distance, elevation, sun and moon movements, GPS coordinates, and satellite positions")
                builder.setPositiveButton("OK") { _, _ ->
                    requestPermissionLauncher.launch(requiredPermissions)
                }
                builder.setNegativeButton("Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Location permission")
                builder.setMessage("Location permission required for calculating " +
                "distance, elevation, sun and moon movements, GPS coordinates, and satellite positions")
                builder.setPositiveButton("OK") { _, _ ->
                    requestPermissionLauncher.launch(requiredPermissions)
                }
                builder.setNegativeButton("Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else if (shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Notification permission")
                builder.setMessage("Notification permission required to create elevation and distance profiles")
                builder.setPositiveButton("OK") { _, _ ->
                    requestPermissionLauncher.launch(requiredPermissions)
                }
                builder.setNegativeButton("Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACTIVITY_RECOGNITION)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Activity permission")
                builder.setMessage("Activity permission required for step counting")
                builder.setPositiveButton("OK") { _, _ ->
                    requestPermissionLauncher.launch(requiredPermissions)
                }
                builder.setNegativeButton("Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data =
                            Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

        }
    }

    override fun onPause() {
        super.onPause()
        // This code has to be here because onResume() and onPause() are called
        // as part of the permissions dialog.
        // We don't want these functions to do anything until permissions have been granted
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) !=
                    PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            sensorManager.unregisterListener(this)
            locationManager?.unregisterGnssStatusCallback(gnssStatusCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
        stopService(Intent(this, LocationService::class.java))
        stopService(Intent(this, AltitudeStepsService::class.java))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerAccuracy.intValue = accuracy
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            //pressure.floatValue = event.values[0]
            pressureViewModel.updatePressure(event.values[0])

            val a = SensorManager.getAltitude(
                PRESSURE_STANDARD_ATMOSPHERE,
                event.values[0]
            )
            altitudeDeque.addLast(a)
            if (altitudeDeque.size > 20) {
                altitudeDeque.removeFirst()
            }
            // moving average
            smoothedAltitudeDeque.addLast(altitudeDeque.sum() / altitudeDeque.size)
            if (smoothedAltitudeDeque.size > 20) {
                smoothedAltitudeDeque.removeFirst()
            }

            val now = System.currentTimeMillis()//Clock.System.now()
            if (altitudeStartTime == 0L) {
                altitudeStartTime = now//.toEpochMilliseconds()
            }
            altitudeTimeDeque.addLast(now - altitudeStartTime)

            // linear regression
            var sumX = 0L
            var sumY = 0.0
            var sumXY = 0.0
            var sumXSquared = 0L
            val altitudeIterator = smoothedAltitudeDeque.iterator()
            val altitudeTimeIterator = altitudeTimeDeque.iterator()
            while (altitudeIterator.hasNext() && altitudeTimeIterator.hasNext()) {
                val altitude = altitudeIterator.next()
                val time = altitudeTimeIterator.next()
                sumX += time
                sumY += altitude
                sumXY += time * altitude
                sumXSquared += time * time
            }
            val numerator = smoothedAltitudeDeque.size * sumXY - sumX * sumY
            val denominator = smoothedAltitudeDeque.size * sumXSquared - sumX * sumX
            var slope = 0.0
            if (denominator != 0L) {
                slope = numerator / denominator
            }
            //altitudeSlope.doubleValue = slope
            verticalSpeedViewModel
                .updateVerticalSpeed(slope.toFloat())

            return
        }
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetic, 0, event.values.size)
        }

        // smooth acceleration X

        for (i in 0 until accelerationX.size - 1) {
            accelerationX[i] = accelerationX[i + 1]
        }
        accelerationX[accelerationX.size - 1] = gravity[0]
        var accelerationSum = 0f
        for (i in 0 until accelerationX.size) {
            accelerationSum += accelerationX[i]
        }
        gravity[0] = accelerationSum / arraySize

        // smooth acceleration Y

        for (i in 0 until accelerationY.size - 1) {
            accelerationY[i] = accelerationY[i + 1]
        }
        accelerationY[accelerationY.size - 1] = gravity[1]
        accelerationSum = 0f
        for (i in 0 until accelerationY.size) {
            accelerationSum += accelerationY[i]
        }
        gravity[1] = accelerationSum / arraySize

        // smooth acceleration Z

        for (i in 0 until accelerationZ.size - 1) {
            accelerationZ[i] = accelerationZ[i + 1]
        }
        accelerationZ[accelerationZ.size - 1] = gravity[2]
        accelerationSum = 0f
        for (i in 0 until accelerationZ.size) {
            accelerationSum += accelerationZ[i]
        }
        gravity[2] = accelerationSum / arraySize

        // smooth magnetic X

        for (i in 0 until magneticX.size - 1) {
            magneticX[i] = magneticX[i + 1]
        }
        magneticX[magneticX.size - 1] = magnetic[0]
        var magneticSum = 0f
        for (i in 0 until magneticX.size) {
            magneticSum += magneticX[i]
        }
        magnetic[0] = magneticSum / arraySize

        // smooth magnetic Y

        for (i in 0 until magneticY.size - 1) {
            magneticY[i] = magneticY[i + 1]
        }
        magneticY[magneticY.size - 1] = magnetic[1]
        magneticSum = 0f
        for (i in 0 until magneticY.size) {
            magneticSum += magneticY[i]
        }
        magnetic[1] = magneticSum / arraySize

        // smooth magnetic Z

        for (i in 0 until magneticZ.size - 1) {
            magneticZ[i] = magneticZ[i + 1]
        }
        magneticZ[magneticZ.size - 1] = magnetic[2]
        magneticSum = 0f
        for (i in 0 until magneticZ.size) {
            magneticSum += magneticZ[i]
        }
        magnetic[2] = magneticSum / arraySize

        if (gravity.all { it != 0f } && magnetic.all { it != 0f }) {
            SensorManager.getRotationMatrix(
                rotationMatrix, null,
                gravity, magnetic
            )
            SensorManager.getOrientation(rotationMatrix, orientation)

            var azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360f
            }
            headingViewModel.updateHeading(azimuthInDegrees)
            //azimuth.floatValue = azimuthInDegrees
        }
    }
}

@OptIn(ExperimentalTime::class)
@SuppressLint("MissingPermission")
fun requestCurrentLocation(
    client: FusedLocationProviderClient,
    onLocationResult1: (Location) -> Unit
) {
    client.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        CancellationTokenSource().token
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationResult1(location)
        }
    }
}

/*private fun doubleToDMSString(value: Double): String {
    var s = ""
    s += value.toInt().toString()
    s += "\u00b0 "
    val minutes = value - value.toInt().toDouble()
    s += (minutes * 60).roundToInt().toString()
    s += "\u2032 "
    val seconds = (minutes * 60) - (minutes * 60).toInt().toDouble()
    s += seconds.roundToInt().toString()
    s += "\u2033"
    return s
}*/

/*fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Earth radius in meters

    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)
    val initialLat = Math.toRadians(lat1)
    val finalLat = Math.toRadians(lat2)

    val a = sin(latDistance / 2).pow(2) +
            sin(lonDistance / 2).pow(2) * cos(initialLat) * cos(finalLat)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c // Distance in km
}*/


/*@OptIn(DelicateCoroutinesApi::class)
val altitudeStepsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        //val scope = rememberCoroutineScope()
        when (intent.getIntExtra("type", 0)) {
            updateAltitude -> {
                var a = intent.getIntExtra("altitude",0)
                //a += (0..100).random()
                sampledAltitudeDeque.addLast(a)
                if (sampledAltitudeDeque.size > 500) {
                    sampledAltitudeDeque.removeAt(0)
                }
            }
            updateSteps -> {
                val steps = intent.getLongExtra("steps", 0)
                if (startSteps == 0L) {
                    startSteps = steps
                }
                val normalizedSteps = steps - startSteps
                stepsDeque.addLast(normalizedSteps)
                if (stepsDeque.size > 30000) {
                    stepsDeque.removeFirst()
                }
                val millis = System.currentTimeMillis()
                if (stepsStartMillis == 0L) {
                    stepsStartMillis = millis
                }
                val normalizedTime: Long = millis - stepsStartMillis
                stepsTimesDeque.addLast((normalizedTime))
                if (stepsTimesDeque.size > 30000) {
                    stepsTimesDeque.removeFirst()
                }
                GlobalScope.launch {
                    val pendingResult = goAsync()
                    try {
                        val stepSample = StepSample(
                            time = normalizedTime,
                            steps = normalizedSteps
                        )
                        stepSampleDao.insert(stepSample)
                    } finally {
                        pendingResult.finish()
                    }
                }

                // linear regression
                var sumX = 0L
                var sumY = 0.0
                var sumXY = 0.0
                var sumXSquared = 0L
                val stepsIterator = stepsDeque.iterator()
                val stepsTimesIterator = stepsTimesDeque.iterator()
                while (stepsIterator.hasNext() && stepsTimesIterator.hasNext()) {
                    val steps = stepsIterator.next()
                    val time = stepsTimesIterator.next()
                    sumX += time
                    sumY += steps
                    sumXY += time * steps
                    sumXSquared += time * time
                }
                val numerator = stepsDeque.size * sumXY - sumX * sumY
                val denominator = stepsDeque.size * sumXSquared - sumX * sumX
                var slope = 0.0
                if (denominator != 0L) {
                    slope = numerator / denominator
                }
                stepsSlope.floatValue = slope.toFloat()
            }
        }
    }
}*/

/*@Composable
fun StepSpeedScreen(
steps: Int,
startSteps: Int
) {
    Box {
        val style = TextStyle()
        val measuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.horizontal_speed)),
            style = style
        )
        val kilometersPerHourMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.kilometers_per_hour)),
            style = style
        )
        val zeroMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("0"),
            style = style
        )
        val twoMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("2"),
            style = style
        )
        val fourMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("4"),
            style = style
        )
        val sixMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("6"),
            style = style
        )
        val s = String.format(
            Locale.US, "Steps: %d", //"distance %d m",
            steps - startSteps //distance.toInt()
        )
        val distanceMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(s),
            style = style
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()

        ) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 2,
                center = center,
                alpha = 1f,
                style = Stroke(width = 8f),
            )
            for (i in 0..270 step 90) {
                rotate(i.toFloat()) {
                    drawLine(
                        brush = SolidColor(Color.Black),
                        start = Offset(center.x, center.y - size.minDimension / 2),
                        end = Offset(
                            center.x,
                            center.y - size.minDimension / 2 + 50f
                        ),
                        strokeWidth = 4f,
                        alpha = 1f,
                    )
                }
            }
            for (i in 45..315 step 90) {
                rotate(i.toFloat()) {
                    drawLine(
                        brush = SolidColor(Color.Black),
                        start = Offset(
                            center.x,
                            center.y - size.minDimension / 2
                        ),
                        end = Offset(
                            center.x,
                            center.y - size.minDimension / 2 + 25f
                        ),
                        strokeWidth = 2f,
                        alpha = 1f,
                    )
                }
            }
            drawLine(
                brush = SolidColor(Color.Red),
                start = Offset(center.x, center.y),
                end = Offset(
                    center.x,
                    center.y + size.minDimension / 2
                ),
                strokeWidth = 8f,
                alpha = 1f,
            )

            drawText(
                textLayoutResult = measuredText,
                topLeft = Offset(
                    center.x - measuredText.size.width / 2,
                    center.y - size.minDimension / 4
                )
            )
            drawText(
                textLayoutResult = kilometersPerHourMeasuredText,
                topLeft = Offset(
                    center.x - kilometersPerHourMeasuredText.size.width / 2,
                    center.y - size.minDimension / 4 + kilometersPerHourMeasuredText.size.height
                )
            )
            drawText(
                textLayoutResult = zeroMeasuredText,
                topLeft = Offset(
                    center.x - zeroMeasuredText.size.width / 2,
                    center.y + center.x - 50 - zeroMeasuredText.size.height
                )
            )
            drawText(
                textLayoutResult = twoMeasuredText,
                topLeft = Offset(
                    center.x - size.minDimension / 2 + 50,
                    center.y - twoMeasuredText.size.height / 2
                )
            )
            drawText(
                textLayoutResult = fourMeasuredText,
                topLeft = Offset(
                    center.x - fourMeasuredText.size.width / 2,
                    center.y - center.x + 50
                )
            )
            drawText(
                textLayoutResult = sixMeasuredText,
                topLeft = Offset(
                    center.x + size.minDimension / 2 - 50 - sixMeasuredText.size.width,
                    center.y - sixMeasuredText.size.height / 2
                )
            )
            drawText(
                textLayoutResult = distanceMeasuredText,
                topLeft = Offset(
                    center.x - distanceMeasuredText.size.width / 2,
                    center.y + size.minDimension / 4 - distanceMeasuredText.size.height
                )
            )
        }
    }
}*/

/*        database = Room
            .databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "app-database"
            )
            .allowMainThreadQueries()
            .build()
*/

//import com.chrisbrossard.trailcompanion.data.GPSAltitudeSample
//import com.chrisbrossard.trailcompanion.data.GPSAltitudeSampleDao
//import com.chrisbrossard.trailcompanion.data.StepSampleDao
//import com.chrisbrossard.trailcompanion.data.StepSessionDao
/*import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeDeleteViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionDao
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeViewModel*/
/*import com.chrisbrossard.trailcompanion.viewmodel.StepCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepDeleteViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepViewModel*/

//azimuth.floatValue,
//pressure.floatValue,
//altitudeSlope.doubleValue,
//sampledAltitudeDeque,
//stepsDeque,
//stepsTimesDeque,
//stepsSlope.floatValue,
//stepSampleDao,
//stepSessionDao,
//steps.intValue,
/*stepCountViewModel,
stepListViewModel,
stepSessionCountViewModel,
stepSessionListViewModel,
stepSessionIdViewModel,
stepRecordingViewModel,
stepDeleteViewModel,*/
//stepViewModel,
//gPSAltitudeViewModel,
//gPSAltitudeSessionDao,
//gPSAltitudeSessionIdViewModel,
//gPSAltitudeListViewModel,
//gPSAltitudeRecordingViewModel,
//locationSampleViewModel,

//val azimuth = mutableFloatStateOf(-1f)
//val pressure = mutableFloatStateOf(0f)
//var startSteps = 0L
//val sampledAltitudeDeque = ArrayDeque<Int>()
//val stepsDeque = ArrayDeque<Long>()
//val stepsTimesDeque = ArrayDeque<Long>()

//var stepsStartMillis = 0L
//val altitudeSlope = mutableDoubleStateOf(0.0)

//val stepsSlope = mutableFloatStateOf(0f)
//private var stepSensor: Sensor? = null
//val updateSteps: Int = 0
//val updateAltitude = 1
//val steps = mutableIntStateOf(0)

/*lateinit var stepSampleDao: StepSampleDao
lateinit var stepCountViewModel: StepCountViewModel
lateinit var stepListViewModel: StepListViewModel*/

//lateinit var gPSAltitudeSampleDao: GPSAltitudeSampleDao
//lateinit var gPSAltitudeListViewModel: GPSAltitudeListViewModel
//lateinit var stepSessionDao: StepSessionDao
//lateinit var gPSAltitudeSessionDao: GPSAltitudeSessionDao

/*lateinit var stepSessionCountViewModel: StepSessionCountViewModel
lateinit var stepSessionListViewModel: StepSessionListViewModel
lateinit var stepSessionIdViewModel: StepSessionIdViewModel
lateinit var stepDeleteViewModel: StepDeleteViewModel*/

/*lateinit var gPSAltitudeSessionCountViewModel: GPSAltitudeSessionCountViewModel
lateinit var gPSAltitudeSessionListViewModel: GPSAltitudeSessionListViewModel
lateinit var gPSAltitudeSessionIdViewModel: GPSAltitudeSessionIdViewModel
lateinit var gPSAltitudeDeleteViewModel: GPSAltitudeDeleteViewModel*/

//lateinit var stepRecordingViewModel: StepRecordingViewModel
//lateinit var gPSAltitudeRecordingViewModel: GPSAltitudeRecordingViewModel

//private val stepViewModel: StepViewModel by viewModels()
//private val gPSAltitudeViewModel: GPSAltitudeViewModel by viewModels()
//var gpsAltitudeStartTime = 0L


/*if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
    if (startSteps == 0L) {
        startSteps = event.values[0].toLong()
    }
    //steps.intValue = (event.values[0].toLong() - startSteps).toInt()
    stepViewModel.updateSteps(
        ((event.values[0].toLong() - startSteps).toFloat())
    )
}*/

/*val gpsAltitudeRecording = sharedPreferences.getInt(
    "gps_altitude_recording",
    -1
)*/
/*val gPSAltitudeSessionId = sharedPreferences.getLong(
    "gps_altitude_session_id",
    -1L)*/
/*if (gpsAltitudeRecording == Recording.STARTING.ordinal) {
    gpsAltitudeStartTime = now
}*/
/*if (gpsAltitudeRecording != Recording.OFF.ordinal) {
    val serviceScope1 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    serviceScope1.launch {
        try {
            gPSAltitudeSampleDao.insert(
                GPSAltitudeSample(
                    sessionId = gPSAltitudeSessionId,
                    time = now - gpsAltitudeStartTime,
                    altitude = location.altitude.toFloat()
                )
            )
        } catch (e: Exception) {
            Log.e("Location and Compass", "altitude sample insert failed", e)
        }
    }
    sharedPreferences.edit {
        putInt("gps_altitude_recording", Recording.ON.ordinal)
    }
}*/

//gPSAltitudeViewModel.updateAltitude(location.altitude)
/*GlobalScope.launch {
    val pendingResult = goAsync()
    try {
        val stepSample = StepSample(
            sessionId = 0L,
            time = 0L,
            steps = 0L
        )
        stepSampleDao.insert(stepSample)
    } finally {
        pendingResult.finish()
    }
}*/

//stepSampleDao = database.stepSampleDao()
//stepCountViewModel = StepCountViewModel(stepSampleDao)
//stepListViewModel = StepListViewModel(stepSampleDao)

//gPSAltitudeSampleDao = database.gPSAltitudeSampleDao()
//gPSAltitudeListViewModel = GPSAltitudeListViewModel(gPSAltitudeSampleDao)

/*stepSessionDao = database.stepSessionDao()
stepSessionCountViewModel = StepSessionCountViewModel(stepSessionDao)
stepSessionListViewModel = StepSessionListViewModel(stepSessionDao)
stepSessionIdViewModel = StepSessionIdViewModel(application)
stepDeleteViewModel = StepDeleteViewModel(stepSampleDao, stepSessionDao)*/
//gPSAltitudeSessionDao = database.gPSAltitudeSessionDao()
//gPSAltitudeSessionCountViewModel = GPSAltitudeSessionCountViewModel(gPSAltitudeSessionDao)
//gPSAltitudeSessionListViewModel = GPSAltitudeSessionListViewModel(
//    gPSAltitudeSessionDao)
//gPSAltitudeSessionIdViewModel = GPSAltitudeSessionIdViewModel(application)
//gPSAltitudeDeleteViewModel =
//    GPSAltitudeDeleteViewModel(gPSAltitudeSampleDao, gPSAltitudeSessionDao)

//stepRecordingViewModel = StepRecordingViewModel(application)
//gPSAltitudeRecordingViewModel = GPSAltitudeRecordingViewModel(application)
//stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

//azimuth.floatValue,
//pressure.floatValue,
//altitudeSlope.doubleValue,
//sampledAltitudeDeque,
//stepsDeque,
//stepsTimesDeque,
//stepsSlope.floatValue,
//stepSampleDao,
//stepSessionDao,
//steps.intValue,
/*del,
stepListViewModel,
stepSessionCountViewModel,
stepSessionListViewModel,
stepSessionIdViewModel,
stepRecordingViewModel,
stepDeleteViewModel,*/
//stepViewModel,
/*gPSAltitudeViewModel,
gPSAltitudeSessionDao,
gPSAltitudeSessionIdViewModel,
gPSAltitudeListViewModel,
gPSAltitudeRecordingViewModel,*/
//locationSampleViewModel,

//sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
