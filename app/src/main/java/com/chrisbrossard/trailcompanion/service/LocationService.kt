package com.chrisbrossard.trailcompanion.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationService : Service() {
    val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    var serviceJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()


    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val channel = NotificationChannel(
            "altitude_steps_channel",
            "Altitude and Steps Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, "altitude_steps_channel")
            .setContentTitle("Service Active")
            .build()
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val location: Location = suspendCancellableCoroutine { continuation ->
                    client.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { location ->
                        continuation.resume(
                            value = location
                        ) { cause, _, _ ->
                            TODO()
                            (cause)
                        }
                    }
                }
                val intent = Intent("com.chrisbrossard.trailcompanion.location")
                intent.putExtra("latitude", location.latitude)
                intent.putExtra("longitude", location.longitude)
                intent.putExtra("altitude", location.altitude)
                sendBroadcast(intent)

                delay(60 * 1000)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        stopSelf()
    }
}

/*@SuppressLint("MissingPermission")
suspend fun awaitLocation(): Location = suspendCancellableCoroutine { continuation ->
    val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    client.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        CancellationTokenSource().token
    ).addOnSuccessListener { location ->
        if (location != null) {
            val intent = Intent("com.chrisbrossard.trailcompanion.location")
            intent.putExtra("latitude", location.latitude)
            intent.putExtra("longitude", location.longitude)
            intent.putExtra("altitude", location.altitude)
            sendBroadcast(intent)
        }
    }
}*/

/*val locationCallback = object : LocationCallback() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        if (locationResult.locations.isNotEmpty()) {
            val location = locationResult.locations.last()
            val intent = Intent("com.chrisbrossard.trailcompanion.location")
            intent.putExtra("latitude", location.latitude)
            intent.putExtra("longitude", location.longitude)
            intent.putExtra("accuracy", location.accuracy)
            intent.putExtra("speed", location.speed)
            intent.putExtra("bearing", location.bearing)
            intent.putExtra("bearing_accuracy", location.bearingAccuracyDegrees)
            intent.putExtra("altitude", location.altitude)
            sendBroadcast(intent)
            /*onLocationResult(
                location
            )*/
        }
    }
}
inner class LocalBinder : Binder() {
    fun getService(): LocationService = this@LocationService
}*/


/*val locationRequest = LocationRequest
    .Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
    .setMinUpdateIntervalMillis(5000)
    .build()*/

/*client.requestLocationUpdates(
    locationRequest,
    locationCallback,
    Looper.getMainLooper()
)*/

/*val gpsAltitudeRecording = sharedPreferences.getInt(
    "gps_altitude_recording",
    -1
)*/
//if (locationRecording != Recording.OFF.ordinal ||
//gpsAltitudeRecording != Recording.OFF.ordinal) {
/*if (locationRecording == Recording.STARTING.ordinal) {
    client.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                val intent = Intent("com.chrisbrossard.trailcompanion.location")
                intent.putExtra("latitude", location.latitude)
                intent.putExtra("longitude", location.longitude)
                intent.putExtra("altitude", location.altitude)
                sendBroadcast(intent)
            }
        }
    val sharedPreferences = getSharedPreferences("my_app", MODE_PRIVATE)
    sharedPreferences.edit {
        putInt("location_recording", Recording.ON.ordinal)
    }
}*/