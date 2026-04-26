package com.chrisbrossard.trailcompanion.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.chrisbrossard.trailcompanion.MainActivity.Recording
import com.chrisbrossard.trailcompanion.data.AltitudeSample
import com.chrisbrossard.trailcompanion.data.AltitudeSampleDao
import com.chrisbrossard.trailcompanion.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.core.content.edit

class AltitudeStepsService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var pressureSensor: Sensor? = null
    var startTime = 0L
    var periodStartTime = 0L
    val notificationId = 1
    lateinit var manager: NotificationManager
    lateinit var notification: Notification
    private lateinit var altitudeSampleDao: AltitudeSampleDao

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        altitudeSampleDao = AppDatabase.getInstance(applicationContext).altitudeSampleDao()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI)

        val channel = NotificationChannel(
            "location_channel",
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Service Active")
            .build()
        startForeground(
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val sharedPreferences = getSharedPreferences("my_app", MODE_PRIVATE)
            val recording = sharedPreferences.getInt(
                "altitude_recording",
                Recording.OFF.ordinal
            )
            val sessionId = sharedPreferences.getLong("altitude_session_id", -1L)

            if (recording == Recording.OFF.ordinal) {
                return
            }

            val seaLevelPressure = sharedPreferences.getFloat("sea_level_pressure", 0f)
            val now = System.currentTimeMillis()
            if (recording == Recording.STARTING.ordinal) {
                startTime = now
                periodStartTime = now
                createAltitudeSample(
                    altitudeSampleDao,
                    event.values[0],
                    seaLevelPressure,
                    sessionId,
                    0L,
                    )
            }
            if (now - periodStartTime > 60 * 1000) {
                periodStartTime = now
                createAltitudeSample(
                    altitudeSampleDao,
                    event.values[0],
                    seaLevelPressure,
                    sessionId,
                    now - startTime)
            }
            if (recording == Recording.STARTING.ordinal) {
                sharedPreferences.edit {
                    putInt("altitude_recording", Recording.ON.ordinal)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.cancel(notificationId)
        stopSelf()
    }
}

fun createAltitudeSample(
    altitudeSampleDao: AltitudeSampleDao,
    pressure: Float,
    seaLevelPressure: Float,
    sessionId: Long,
    time: Long
) {
    val a = SensorManager.getAltitude(
        seaLevelPressure, //SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
        pressure
    )
    val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    serviceScope.launch {
        try {
            altitudeSampleDao.insert(
                AltitudeSample(
                    sessionId = sessionId,
                    time = time,
                    altitude = a
                )
            )
        } catch (e: Exception) {
            Log.e("Location and Compass", "altitude sample insert failed", e)
        }
    }
}

//val intent = Intent("com.example.compassandlocation.altitude_steps")
//intent.putExtra("type", updateSteps)
//intent.putExtra("steps", event.values[0].toLong())
//sendBroadcast(intent)

//val intent = Intent("com.example.compassandlocation.altitude_steps")
//intent.putExtra("type", updateAltitude)
//intent.putExtra("altitude", a.toInt()) // - startAltitude)
//sendBroadcast(intent)

/*if (startAltitude == 0) {
    startAltitude = a.toInt()
}*/

//import com.chrisbrossard.trailcompanion.data.StepSample
//import com.chrisbrossard.trailcompanion.data.StepSampleDao
//var periodicStartTime = 0L
//var stepsTime = 0L
//val updateSteps = 0
//val updateAltitude = 1
//private lateinit var stepSampleDao: StepSampleDao
//var startSteps = 0L
//var sessionIdViewModel = SessionIdViewModel(application)
//stepSampleDao = AppDatabase.getInstance(applicationContext).stepSampleDao()

/*if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {

    val sharedPreferences = getSharedPreferences("my_app", MODE_PRIVATE)
    val recording = sharedPreferences.getInt("step_recording",
        Recording.OFF.ordinal)
    if (recording == Recording.OFF.ordinal) {
        return
    }

    val now = System.currentTimeMillis()
    if (stepsTime == 0L) {
        stepsTime = now
    }
    val normalizedTime = now - stepsTime
    if (startSteps == 0L) {
        startSteps = event.values[0].toLong()
    }
    val normalizedSteps = event.values[0].toLong() - startSteps

    val sessionId = sharedPreferences.getLong("step_session_id", 0)

    val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    serviceScope.launch {
        try {
            stepSampleDao.insert(
                StepSample(
                    sessionId = sessionId,
                    time = normalizedTime,
                    steps = normalizedSteps
                )
            )
        } catch (e: Exception) {
            Log.e("Location and Compass", "step sample insert failed", e)
        }
    }
} else*/