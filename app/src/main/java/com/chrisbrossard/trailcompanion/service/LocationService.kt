package com.chrisbrossard.trailcompanion.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
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

class LocationService : Service() {
    val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
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

        /*val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(5000)
            .build()*/

        /*client.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )*/

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                //val sharedPreferences = getSharedPreferences("my_app", MODE_PRIVATE)
                /*val locationRecording = sharedPreferences.getInt(
                    "location_recording",
                    -1
                )
                val gpsAltitudeRecording = sharedPreferences.getInt(
                    "gps_altitude_recording",
                    -1
                )*/
                //if (locationRecording != Recording.OFF.ordinal ||
                    //gpsAltitudeRecording != Recording.OFF.ordinal) {
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
                //}
                delay(60 * 1000)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        //client.removeLocationUpdates(locationCallback)
    }
}