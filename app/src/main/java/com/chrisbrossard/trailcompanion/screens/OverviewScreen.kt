package com.chrisbrossard.trailcompanion.screens

import android.content.Context
//import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.hardware.SensorManager.PRESSURE_STANDARD_ATMOSPHERE
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.chrisbrossard.trailcompanion.MainActivity
import com.chrisbrossard.trailcompanion.data.AltitudeSampleDao
import com.chrisbrossard.trailcompanion.data.AltitudeSession
import com.chrisbrossard.trailcompanion.data.AltitudeSessionDao
import com.chrisbrossard.trailcompanion.data.LocationSampleDao
import com.chrisbrossard.trailcompanion.data.LocationSession
import com.chrisbrossard.trailcompanion.data.LocationSessionDao
import com.chrisbrossard.trailcompanion.requestCurrentLocation
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeDeleteViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.ChartDistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.DistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.HeadingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationViewModel
import com.chrisbrossard.trailcompanion.viewmodel.NavigationViewModel
import com.chrisbrossard.trailcompanion.viewmodel.PressureViewModel
import com.chrisbrossard.trailcompanion.viewmodel.SeaLevelPressureViewModel
import com.chrisbrossard.trailcompanion.viewmodel.VerticalSpeedViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import dev.jamesyox.kastro.sol.calculateSolarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Suppress("UNUSED_PARAMETER")
@Composable
fun OverviewScreen(
    client: FusedLocationProviderClient,
    navController: NavHostController,
    magnetometerAccuracy: Int,
    altitudeSampleDao: AltitudeSampleDao,
    altitudeSessionDao: AltitudeSessionDao,
    altitudeListViewModel: AltitudeListViewModel,
    altitudeSessionCountViewModel: AltitudeSessionCountViewModel,
    altitudeSessionListViewModel: AltitudeSessionListViewModel,
    altitudeSessionIdViewModel: AltitudeSessionIdViewModel,
    altitudeRecordingViewModel: AltitudeRecordingViewModel,
    altitudeDeleteViewModel: AltitudeDeleteViewModel,
    onNavigateToAltitudeRecording: () -> Unit,
    headingViewModel: HeadingViewModel,
    verticalSpeedViewModel: VerticalSpeedViewModel,
    pressureViewModel: PressureViewModel,
    distanceViewModel: DistanceViewModel,
    locationListViewModel: LocationListViewModel,
    locationRecordingViewModel: LocationRecordingViewModel,
    locationSessionIdViewModel: LocationSessionIdViewModel,
    locationSessionDao: LocationSessionDao,
    locationSessionListViewModel: LocationSessionListViewModel,
    locationSampleDao: LocationSampleDao,
    locationSessionCountViewModel: LocationSessionCountViewModel,
    navigationViewModel: NavigationViewModel,
    chartDistanceViewModel: ChartDistanceViewModel,
    locationViewModel: LocationViewModel,
    seaLevelPressureViewModel: SeaLevelPressureViewModel
) {
    var sunMoonOctant by remember { mutableStateOf("-") }
    var compassOctant by remember { mutableStateOf("-") }
    var location1 by remember { mutableStateOf(Location("")) }
    val altitudeSessionRowCount by altitudeSessionCountViewModel.rowCount.collectAsState(initial = 0)
    val locationSessionRowCount by locationSessionCountViewModel.rowCount.collectAsState(initial = 0)
    val altitudeSessionList by altitudeSessionListViewModel.rowList.collectAsState(initial = emptyList())
    val locationSessionList by locationSessionListViewModel.rowList.collectAsState(initial = emptyList())
    val sheetState = rememberModalBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val heading by headingViewModel.heading.collectAsState()
    val verticalSpeed by verticalSpeedViewModel.verticalSpeed.collectAsState()
    val vmPressure by pressureViewModel.pressure.collectAsState(initial = 0f)
    val distance by distanceViewModel.distance.collectAsState()
    val seaLevelPressure by seaLevelPressureViewModel.pressure.collectAsState(
        initial = PRESSURE_STANDARD_ATMOSPHERE)

    val context = LocalContext.current
    LaunchedEffect(context) {
        requestCurrentLocation(
            client
        ) { location ->
            location1 = location
        }
        sheetState.partialExpand()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 40.dp,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle()
        },
        sheetContent = {
            Column {
                // altitude profiles
                if (altitudeSessionRowCount != 0) {
                    Text(
                        text = "Altitude Profiles",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                LazyColumn {
                    items(
                        items = altitudeSessionList,
                        key = { it.sessionId }
                    ) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            initialValue = SwipeToDismissBoxValue.Settled,
                            positionalThreshold = { totalDistance ->
                                totalDistance * 0.75f
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromEndToStart = false,
                            backgroundContent = {
                            },
                            onDismiss = {
                                val serviceScope =
                                    CoroutineScope(SupervisorJob() + Dispatchers.IO)
                                serviceScope.launch {
                                    try {
                                        altitudeSampleDao.deleteBySessionId(item.sessionId)
                                        altitudeSessionDao.deleteBySessionId(item.sessionId)
                                    } catch (e: Exception) {
                                        Log.e(
                                            "Trail Companion",
                                            "altitude delete failed",
                                            e
                                        )
                                    }
                                }
                            }
                        ) {
                            val formatted = java.time.Instant.ofEpochMilli(item.startTime)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("MMM d, h.mm a"))
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        altitudeSessionIdViewModel.setSessionId(item.sessionId)
                                        navController.navigate("altitude_profile_viewing")
                                    }
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    //modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(formatted)
                                }
                            }
                        }
                    }
                }
                // distance profiles
                if (locationSessionRowCount != 0) {
                    Text(
                        text = "Distance Profiles",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                LazyColumn {
                    items(
                        items = locationSessionList,
                        key = { it.sessionId }
                    ) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            initialValue = SwipeToDismissBoxValue.Settled,
                            positionalThreshold = { totalDistance ->
                                totalDistance * 0.75f
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromEndToStart = false,
                            backgroundContent = {
                            },
                            onDismiss = {
                                val serviceScope =
                                    CoroutineScope(SupervisorJob() + Dispatchers.IO)
                                serviceScope.launch {
                                    try {
                                        locationSampleDao.deleteBySessionId(item.sessionId)
                                        locationSessionDao.deleteBySessionId(item.sessionId)
                                    } catch (e: Exception) {
                                        Log.e(
                                            "Trail Companion",
                                            "location delete failed",
                                            e
                                        )
                                    }
                                }
                            }
                        ) {
                            val formatted = java.time.Instant.ofEpochMilli(item.startTime)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("MMM d, h.mm a"))
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        locationSessionIdViewModel.setSessionId(item.sessionId)
                                        navController.navigate("distance_profile_viewing")
                                    }
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    //modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(formatted)
                                }
                            }
                        }
                    }
                }
            }
        }
        // main screen
    ) { innerPadding ->
        Column {
            val grey = Color(250, 250, 250)
            // first row
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .background(grey),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    // distance
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(grey)
                            .fillMaxSize()
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val y = size.height - strokeWidth / 2
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val s: String = if (distance < 1000) {
                                distance.toInt().toString()
                            } else if (distance < 10000) {
                                String.format(Locale.US, "%.1f", distance / 1000)
                            } else {
                                (distance / 1000).toInt().toString()
                            }
                            BasicText(
                                modifier = Modifier
                                    .clickable {
                                        val serviceScope =
                                            CoroutineScope(SupervisorJob() + Dispatchers.IO)
                                        serviceScope.launch {
                                            try {
                                                val sessionId = locationSessionDao.insert(
                                                    LocationSession(
                                                        startTime = System.currentTimeMillis(),
                                                        endTime = 0L
                                                    )
                                                )
                                                locationSessionIdViewModel.setSessionId(sessionId)
                                                chartDistanceViewModel
                                                    .updateDistance(0f)
                                                locationRecordingViewModel.updateRecording(
                                                    MainActivity.Recording.STARTING.ordinal)
                                            } catch (e: Exception) {
                                                Log.e("Trail Companion", "insert failed", e)
                                            }
                                        }

                                        navController.navigate("distance_profile_recording")
                                    },
                                text = s,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(),
                            )
                            if (distance < 1000) {
                                Text("Distance m")
                            } else {
                                Text("Distance km")
                            }
                        }
                    }
                    // vertical speed
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(grey)
                            .fillMaxSize()
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val y = size.height - strokeWidth / 2
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val s = String.format(
                            Locale.US,
                            "%.0f",
                            verticalSpeed * 1000 * 60
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BasicText(
                                modifier = Modifier.clickable {
                                    navController.navigate("vertical_speed")
                                },
                                text = s,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased()
                            )
                            Text("Vertical Speed m/hr")
                        }
                    }
                }
            }
            // second row
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .background(grey)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    // Altitudes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(grey)
                            .fillMaxSize()
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val y = size.height - strokeWidth / 2
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (seaLevelPressure == PRESSURE_STANDARD_ATMOSPHERE &&
                            location1.altitude != 0.0 &&
                            vmPressure != 0f) { //gPSAltitude != 0.0) {
                            val newPressure =
                            (vmPressure /
                                    (1 - location1.altitude / 44330.0).pow(5.255)).toFloat()
                            seaLevelPressureViewModel.updatePressure(newPressure)
                            val sharedPreferences: SharedPreferences =
                                LocalContext.current.getSharedPreferences("my_app", Context.MODE_PRIVATE)
                            sharedPreferences.edit {
                                putFloat("sea_level_pressure", newPressure)
                            }
                        }
                        val a = SensorManager.getAltitude(
                            seaLevelPressure,
                            vmPressure
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BasicText(
                                modifier = Modifier
                                    //.weight(1.0f)
                                    .clickable {
                                        val serviceScope =
                                            CoroutineScope(SupervisorJob() + Dispatchers.IO)
                                        serviceScope.launch {
                                            try {
                                                val id = altitudeSessionDao.insert(
                                                    AltitudeSession(
                                                        startTime = System.currentTimeMillis(),
                                                        endTime = 0L
                                                    )
                                                )
                                                altitudeSessionIdViewModel.setSessionId(id)
                                                chartDistanceViewModel
                                                    .updateDistance(0f)
                                                altitudeRecordingViewModel.updateRecording(
                                                    MainActivity.Recording.STARTING.ordinal
                                                )

                                            } catch (e: Exception) {
                                                Log.e(
                                                    "Trail Companion",
                                                    "altitude session insert failed",
                                                    e
                                                )
                                            }
                                        }

                                        onNavigateToAltitudeRecording()
                                        //navController.navigate("altitude_profile_recording")
                                    },
                                text = a.roundToInt().toString(),
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased()
                            )
                            Text("Altitude m")
                        }
                    }
                    // Sun/Moon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(grey)
                            .fillMaxSize()
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val y = size.height - strokeWidth / 2
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val currentMillis = System.currentTimeMillis()
                        val currentInstant = Instant.fromEpochMilliseconds(
                            currentMillis
                        )
                        val currentSolarState = currentInstant.calculateSolarState(
                            location1.latitude,
                            location1.longitude
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (currentSolarState.altitude < 0) {
                                sunMoonOctant = "-"
                            } else {
                                when (currentSolarState.azimuth.roundToInt()) {
                                    in 0..45 / 2 -> {
                                        sunMoonOctant = "N"
                                    }

                                    in 45 / 2..45 + 45 / 2 -> {
                                        sunMoonOctant = "NE"
                                    }

                                    in 90 - 45 / 2..90 + 45 / 2 -> {
                                        sunMoonOctant = "E"
                                    }

                                    in 135 - 45 / 2..135 + 90 / 2 -> {
                                        sunMoonOctant = "SE"
                                    }

                                    in 180 - 45 / 2..180 + 45 / 2 -> {
                                        sunMoonOctant = "S"
                                    }

                                    in 225 - 45 / 2..225 + 45 / 2 -> {
                                        sunMoonOctant = "SW"
                                    }

                                    in 270 - 45 / 2..270 + 45 / 2 -> {
                                        sunMoonOctant = "W"
                                    }

                                    in 315 - 45 / 2..315 + 45 / 2 -> {
                                        sunMoonOctant = "NW"
                                    }

                                    in 315 + 45 / 2..360 -> {
                                        sunMoonOctant = "N"
                                    }
                                }
                            }
                            BasicText(
                                modifier = Modifier.clickable {
                                    navController.navigate("sun_moon")
                                },
                                text = sunMoonOctant,
                                autoSize = TextAutoSize.StepBased()
                            )
                            Text("Sun Direction")
                        }
                    }
                }
            }
            // Third Row
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .background(grey)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (compassOctant) {
                    "-" -> {
                        when (heading.roundToInt()) {
                            in 0..45 / 2 -> {
                                compassOctant = "N"
                            }

                            in 45 / 2..45 + 45 / 2 -> {
                                compassOctant = "NE"
                            }

                            in 90 - 45 / 2..90 + 45 / 2 -> {
                                compassOctant = "E"
                            }

                            in 135 - 45 / 2..135 + 45 / 2 -> {
                                compassOctant = "SE"
                            }

                            in 180 - 45 / 2..180 + 45 / 2 -> {
                                compassOctant = "S"
                            }

                            in 225 - 45 / 2..225 + 45 / 2 -> {
                                compassOctant = "SW"
                            }

                            in 270 - 45 / 2..270 + 45 / 2 -> {
                                compassOctant = "W"
                            }

                            in 315 - 45 / 2..315 + 45 / 2 -> {
                                compassOctant = "NW"
                            }

                            in 315 + 45 / 2..359 -> {
                                compassOctant = "N"
                            }
                        }
                    }

                    "N" -> {
                        if (heading > 45 / 2 + 10 && heading < 45 + 45 / 2) {
                            compassOctant = "NE"
                        } else if (heading < 360 - 45 / 2 - 10 && heading > 315 - 45 / 2) {
                            compassOctant = "NW"
                        }
                    }

                    "NE" -> {
                        if (heading > 45 + 45 / 2 + 10) {
                            compassOctant = "E"
                        } else if (heading < 45 - 45 / 2 - 10) {
                            compassOctant = "N"
                        }
                    }

                    "E" -> {
                        if (heading > 90 + 45 / 2 + 10) {
                            compassOctant = "SE"
                        } else if (heading < 90 - 45 / 2 - 10) {
                            compassOctant = "NE"
                        }
                    }

                    "SE" -> {
                        if (heading > 135 + 45 / 2 + 10) {
                            compassOctant = "S"
                        } else if (heading < 135 - 45 / 2 - 10) {
                            compassOctant = "E"
                        }
                    }

                    "S" -> {
                        if (heading > 180 + 45 / 2 + 10) {
                            compassOctant = "SW"
                        } else if (heading < 180 - 45 / 2 - 10) {
                            compassOctant = "SE"
                        }
                    }

                    "SW" -> {
                        if (heading > 225 + 45 / 2 + 10) {
                            compassOctant = "W"
                        } else if (heading < 225 - 45 / 2 - 10) {
                            compassOctant = "S"
                        }
                    }

                    "W" -> {
                        if (heading > 270 + 45 / 2 + 10) {
                            compassOctant = "NW"
                        } else if (heading < 270 - 45 / 2 - 10) {
                            compassOctant = "SW"
                        }
                    }

                    "NW" -> {
                        if (heading > 315 + 45 / 2 + 10) {
                            compassOctant = "N"
                        } else if (heading < 315 - 45 / 2 - 10) {
                            compassOctant = "W"
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BasicText(
                        modifier = Modifier.clickable {
                            navController.navigate("compass")
                        },
                        text = compassOctant,
                        autoSize = TextAutoSize.StepBased()
                    )
                    Text(heading.toInt().toString() + "\u00b0")
                    Text("Heading")
                }
            }
        }
    }
}

// calculate bearing of line between two points on a sphere
/*fun twoPointBearing(
    latitude1: Double,
    latitude2: Double,
    longitude1: Double,
    longitude2: Double): Double {
    val latitude1Radians = toRadians(latitude1)
    val latitude2Radians = toRadians(latitude2)
    val longitude1Radians = toRadians(longitude1)
    val longitude2Radians = toRadians(longitude2)
    //val deltaLatitude = latitude2Radians - latitude1Radians
    val deltaLongitude = longitude2Radians - longitude1Radians

    // haversine
    val x = cos(latitude1Radians) * sin(latitude2Radians) -
            sin(latitude1Radians) * cos(latitude2Radians) *
            cos(deltaLongitude)
    val y = sin(deltaLongitude) * cos(latitude2Radians)
    val angle = atan2(y, x)

    /*val x = deltaLongitude * cos(deltaLatitude / 2)
    val angle = (x.pow(2) + deltaLatitude.pow(2)).pow(0.5)*/

    /*val a = sin(deltaLatitude / 2).pow(2) +
            cos(latitude1Radians) * cos(latitude2Radians) *
            sin(deltaLongitude / 2).pow(2)
    val angle = 2 * atan2(a.pow(0.5),(1 - a).pow(0.5))*/
    //val angle = 2 * asin(a.pow(0.5))

    /*val angle = atan2(
        sin(deltaLongitude) * cos(latitude2Radians),
        cos(latitude1Radians) * sin(latitude2Radians -
        sin(latitude1Radians) * cos(latitude2Radians) * cos(deltaLongitude)))*/

    return angle
}*/

//Text("world")
//}

/*if (showBottomSheet) {
    ModalBottomSheet(
        onDismissRequest = {
            showBottomSheet = false
        },
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Text("hello")
        Text("hello")
        Text("hello")
        Text("hello")
        Text("hello")
        Text("hello")
        Text("hello")
        Text("hello")
        Text("hello")


    }
}*/

/*Column(
horizontalAlignment = Alignment.CenterHorizontally
) {
val s = String.format(Locale.US, "%.1f", stepsSpeed)
BasicText(
    modifier = Modifier
        .clickable {
            //navController.navigate("steps_profile")
        },
    text = s,
    maxLines = 1,
    autoSize = TextAutoSize.StepBased(),
)
Text("Step Speed steps/s")
}*/

//import androidx.core.content.ContextCompat
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionDao
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionIdViewModel
//import com.chrisbrossard.trailcompanion.data.GPSAltitudeSession
//import com.chrisbrossard.trailcompanion.data.StepSampleDao
//import com.chrisbrossard.trailcompanion.data.StepSessionDao
//import com.chrisbrossard.trailcompanion.service.AltitudeStepsService
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeRecordingViewModel
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeViewModel
/*import com.chrisbrossard.trailcompanion.viewmodel.StepRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepDeleteViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepViewModel*/
//import java.lang.Math.toRadians
//import kotlin.math.atan2
//import kotlin.math.cos
//import kotlin.math.sin

//altitudeSlope: Double,
//pressure: Float,
//azimuth: Float,
//altitudes: ArrayDeque<Int>,
//stepsDeque: ArrayDeque<Long>,
//stepsSpeed: Float,
//stepSampleDao: StepSampleDao,
//stepSessionDao: StepSessionDao,
//steps: Int,
/*stepCountViewModel: StepCountViewModel,
stepListViewMode: StepListViewModel,
stepSessionCountViewModel: StepSessionCountViewModel,
stepSessionListViewModel: StepSessionListViewModel,
stepSessionIdViewModel: StepSessionIdViewModel,
stepRecordingViewModel: StepRecordingViewModel,
stepDeleteViewModel: StepDeleteViewModel,*/
//stepViewModel: StepViewModel,
/*gPSAltitudeViewModel: GPSAltitudeViewModel,
gPSAltitudeSessionDao: GPSAltitudeSessionDao,
gPSAltitudeSessionIdViewModel: GPSAltitudeSessionIdViewModel,
gPSAltitudeRecordingViewModel: GPSAltitudeRecordingViewModel,*/

//val stepRowCount by stepCountViewModel.rowCount.collectAsState(initial = 0)
//val stepSessionRowCount by stepSessionCountViewModel.rowCount.collectAsState(initial = 0)
//val stepSessionList by stepSessionListViewModel.rowList.collectAsState(initial = emptyList())
//var showBottomSheet by rememberSaveable { mutableStateOf(false) }
//val stepSessionId by remember { mutableLongStateOf(stepSessionIdViewModel.stepSessionId) }
//val altitudeSessionId by remember { mutableLongStateOf(altitudeSessionIdViewModel.getSessionId()) }
//var stepRecording by remember { mutableStateOf(stepRecordingViewModel.recording) }
//var altitudeRecording by remember { mutableStateOf(altitudeRecordingViewModel.recording) }
//val scope = rememberCoroutineScope()
//val vmSteps by stepViewModel.steps.collectAsState()
//val chartDistance by chartDistanceViewModel.distance.collectAsState()
//val gPSAltitude by gPSAltitudeViewModel.altitude.collectAsState()
//val location by locationViewModel.location.collectAsState()

/*if (seaLevelPressure == PRESSURE_STANDARD_ATMOSPHERE && vmPressure != 0f) {
    val newPressure =
        (vmPressure /
                (1 - location.altitude / 44330.0).pow(5.255)).toFloat()
    seaLevelPressureViewModel.updatePressure(newPressure)
    val sharedPreferences =
        context.getSharedPreferences("my_app", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putFloat("sea_level_pressure", newPressure)
    }
}*/
/*val stepCount = stepSessionRowCount
val altitudeCount = altitudeSessionRowCount
if (stepCount != 0) {
    showBottomSheet = true
}
if (altitudeCount != 0) {
    showBottomSheet = true
}*/

//showBottomSheet = true

// step profiles
/*if (stepSessionRowCount != 0) {
    Text(
        text = "Step Profiles",
        style = MaterialTheme.typography.titleMedium
    )
}
LazyColumn {
    items(
        items = stepSessionList,
        key = { it.sessionId }
    ) { item ->
        val dismissState = rememberSwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = { totalDistance ->
                totalDistance * 0.75f
            }
        )
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromEndToStart = false,
            backgroundContent = {
            },
            onDismiss = {
                val serviceScope =
                    CoroutineScope(SupervisorJob() + Dispatchers.IO)
                serviceScope.launch {
                    try {
                        stepSampleDao.deleteBySessionId(item.sessionId)
                        stepSessionDao.deleteBySessionId(item.sessionId)
                    } catch (e: Exception) {
                        Log.e(
                            "Location and Compass",
                            "step delete failed",
                            e
                        )
                    }
                }
            }
        ) {
            val formatted = java.time.Instant.ofEpochMilli(item.startTime)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM d, h.mm a"))
            Row(
                modifier = Modifier
                    .clickable {
                        stepSessionIdViewModel.setSessionId(item.sessionId)
                        navController.navigate("steps_profile_viewing")
                    }
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    //modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(formatted)
                }
                /*Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = {
                        }
                    ) {
                        Text("Delete")
                    }
                }*/
            }
        }
    }
}*/

/*Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.CenterEnd
) {
    Button(
        onClick = {
        }
    ) {
        Text("Delete")
    }
}*/

/*Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.CenterEnd
) {
    Button(
        onClick = {
        }
    ) {
        Text("Delete")
    }
}*/


/*try {
    val id = gPSAltitudeSessionDao.insert(
        GPSAltitudeSession(
            //sessionId = altitudeSessionId,
            startTime = System.currentTimeMillis(),
            endTime = 0L
        )
    )
    gPSAltitudeSessionIdViewModel.setSessionId(id)
    gPSAltitudeRecordingViewModel.updateRecording(
        MainActivity.Recording.STARTING.ordinal
    )
} catch (e: Exception) {
    Log.e(
        "Trail Companion",
        "GPS altitude session insert failed",
        e
    )
}*/

/*BasicText(
    modifier = Modifier
        .clickable {
            val serviceScope =
                CoroutineScope(SupervisorJob() + Dispatchers.IO)
            serviceScope.launch {
                try {
                    val id = altitudeSessionDao.insert(
                        AltitudeSession(
                            //sessionId = altitudeSessionId,
                            startTime = System.currentTimeMillis(),
                            endTime = 0L
                        )
                    )
                    altitudeRecordingViewModel.updateRecording(
                        MainActivity.Recording.STARTING.ordinal
                    )
                    altitudeSessionIdViewModel.setSessionId(id)
                    chartDistanceViewModel
                        .updateDistance(0f)
                } catch (e: Exception) {
                    Log.e(
                        "Trail Companion",
                        "altitude session insert failed",
                        e
                    )
                }
                try {
                    val id = gPSAltitudeSessionDao.insert(
                        GPSAltitudeSession(
                            //sessionId = altitudeSessionId,
                            startTime = System.currentTimeMillis(),
                            endTime = 0L
                        )
                    )
                    gPSAltitudeSessionIdViewModel.setSessionId(id)
                    gPSAltitudeRecordingViewModel.updateRecording(
                        MainActivity.Recording.STARTING.ordinal
                    )
                } catch (e: Exception) {
                    Log.e(
                        "Trail Companion",
                        "GPS altitude session insert failed",
                        e
                    )
                }
            }
            onNavigateToAltitudeRecording()
        }
        .weight(1.0f),
    //text = gPSAltitude.toInt().toString(),
    text = location1.altitude.toInt().toString(),
    maxLines = 1,
    autoSize = TextAutoSize.StepBased()
)*/

// third row
/*Box(
    modifier = Modifier
        .weight(0.3f)
        .background(Color.Transparent)
        .fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Row {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(grey)
                .fillMaxSize()
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val s = String.format(Locale.US, "%.1f",
                    distance / 1000)
                BasicText(
                    modifier = Modifier.clickable {
                        //navController.navigate("compass")
                    },
                    text = s, //distance.toInt().toString(),
                    autoSize = TextAutoSize.StepBased()
                )
                Text("GPS Distance km")
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .background(grey)
                .fillMaxSize()
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText(
                    modifier = Modifier.clickable {
                        //navController.navigate("compass")
                    },
                    text = gPSAltitude.toInt().toString(),
                    autoSize = TextAutoSize.StepBased()
                )
                Text("GPS Altitude m")
            }
        }
    }
}*/

/*if (navigationViewModel.navigating) {
    val distance = location.distanceTo(navigationViewModel.getWaypoint())
    if (distance < 0.0f) {
        navigationViewModel.navigating = false
    } else {
        val waypoint = navigationViewModel.getWaypoint()
        val bearing = twoPointBearing(
            location.latitude,
            waypoint.latitude,
            location.longitude,
            waypoint.longitude
        ) * 180.0 / PI
        //var bearing = location1
        //    .bearingTo(navigationViewModel.getWaypoint())
        /*if (bearing < 0f) {
            bearing += 360f
        }*/
        /*bearing -= heading
        if (bearing < 0f) {
            bearing += 360f
        }*/
        val s = String.format(
            Locale.US,
            "Bearing: %.0f",
            bearing)
        /*when (bearing) {
            in 0f..22.5f -> {
                s = "Waypoint straight ahead"
            }

            in 22.5f..67.5f -> {
                s = "Waypoint slightly right"
            }

            in 67.5f..112.5f -> {
                s = "Waypoint to the right"
            }

            in 112.5f..247.5f -> {
                s = "Waypoint behind you"
            }

            in 247.5f..292.5f -> {
                s = "Waypoint to your left"
            }

            in 292.5f..337.5f -> {
                s = "Waypoint slightly left"
            }

            in 337.5f..360f -> {
                s = "Waypoint straight ahead"
            }

            else -> {
                s = String.format(Locale.US, "bearing error: %.0f", bearing)
            }
        }*/
        Text(s)
    }
}*/