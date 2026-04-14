package com.chrisbrossard.trailcompanion

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chrisbrossard.trailcompanion.data.AltitudeSampleDao
import com.chrisbrossard.trailcompanion.data.AltitudeSessionDao
import com.chrisbrossard.trailcompanion.data.LocationSampleDao
import com.chrisbrossard.trailcompanion.data.LocationSessionDao
import com.chrisbrossard.trailcompanion.data.StepSampleDao
import com.chrisbrossard.trailcompanion.data.StepSessionDao
import com.chrisbrossard.trailcompanion.screens.AltitudeProfileRecordingScreen
import com.chrisbrossard.trailcompanion.screens.AltitudeProfileViewingScreen
import com.chrisbrossard.trailcompanion.screens.CompassScreen
import com.chrisbrossard.trailcompanion.screens.DistanceProfileRecordingScreen
import com.chrisbrossard.trailcompanion.screens.DistanceProfileViewingScreen
import com.chrisbrossard.trailcompanion.screens.GNSSScreen
import com.chrisbrossard.trailcompanion.screens.OverviewScreen
import com.chrisbrossard.trailcompanion.screens.StepsProfileRecordingScreen
import com.chrisbrossard.trailcompanion.screens.StepsProfileViewingScreen
import com.chrisbrossard.trailcompanion.screens.SunMoonScreen
import com.chrisbrossard.trailcompanion.screens.VerticalSpeedScreen
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeDeleteViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.ChartDistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.HeadingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.PressureViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepDeleteViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepViewModel
import com.chrisbrossard.trailcompanion.viewmodel.VerticalSpeedViewModel
import com.chrisbrossard.trailcompanion.viewmodel.DistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionDao
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSampleViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.NavigationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import kotlin.time.ExperimentalTime

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@SuppressLint()
@Composable
fun Navigation(
    client: FusedLocationProviderClient,
    //azimuth: Float,
    //pressure: Float,
    gnssStatus: GnssStatus?,
    magnetometerAccuracy: Int,
    //altitudeSlope: Double,
    //sampledAltitudes: ArrayDeque<Int>,
    //stepsDeque: ArrayDeque<Long>,
    //stepsTimesDeque: ArrayDeque<Long>,
    //stepsSpeed: Float,
    altitudeSampleDao: AltitudeSampleDao,
    stepSampleDao: StepSampleDao,
    stepSessionDao: StepSessionDao,
    altitudeSessionDao: AltitudeSessionDao,
    //steps: Int,
    stepCountViewModel: StepCountViewModel,
    stepListViewModel: StepListViewModel,
    stepSessionCountViewModel: StepSessionCountViewModel,
    stepSessionListViewModel: StepSessionListViewModel,
    stepSessionIdViewModel: StepSessionIdViewModel,
    stepRecordingViewModel: StepRecordingViewModel,
    stepDeleteViewModel: StepDeleteViewModel,
    altitudeListViewModel: AltitudeListViewModel,
    altitudeSessionCountViewModel: AltitudeSessionCountViewModel,
    altitudeSessionListViewModel: AltitudeSessionListViewModel,
    altitudeSessionIdViewModel: AltitudeSessionIdViewModel,
    altitudeRecordingViewModel: AltitudeRecordingViewModel,
    altitudeDeleteViewModel: AltitudeDeleteViewModel,
    navController: NavHostController,
    headingViewModel: HeadingViewModel,
    stepViewModel: StepViewModel,
    verticalSpeedViewModel: VerticalSpeedViewModel,
    pressureViewModel: PressureViewModel,
    distanceViewModel: DistanceViewModel,
    gPSAltitudeViewModel: GPSAltitudeViewModel,
    gPSAltitudeSessionDao: GPSAltitudeSessionDao,
    gPSAltitudeSessionIdViewModel: GPSAltitudeSessionIdViewModel,
    gPSAltitudeListViewModel: GPSAltitudeListViewModel,
    gPSAltitudeRecordingViewModel: GPSAltitudeRecordingViewModel,
    locationListViewModel: LocationListViewModel,
    locationRecordingViewModel: LocationRecordingViewModel,
    locationSessionIdViewModel: LocationSessionIdViewModel,
    locationSessionDao: LocationSessionDao,
    locationSessionListViewModel: LocationSessionListViewModel,
    locationSampleDao: LocationSampleDao,
    locationSessionCountViewModel: LocationSessionCountViewModel,
    locationSampleViewModel: LocationSampleViewModel,
    navigationViewModel: NavigationViewModel,
    chartDistanceViewModel: ChartDistanceViewModel
) {
    var location1 by remember { mutableStateOf(Location("")) }

    Log.d("Trail Companion", "Navigation() called")

    LaunchedEffect(Unit) {
        requestCurrentLocation(
            client
        ) { location ->
            location1 = location
        }
    }
    NavHost(
        navController = navController,
        startDestination = "overview"
    ) {
        composable("overview") {
            OverviewScreen(
                client,
                navController,
                //altitudeSlope,
                //pressure,
                //azimuth,
                magnetometerAccuracy,
                //sampledAltitudes,
                //stepsDeque,
                //stepsSpeed,
                altitudeSampleDao,
                stepSampleDao,
                stepSessionDao,
                altitudeSessionDao,
                //steps,
                stepCountViewModel,
                stepListViewModel,
                stepSessionCountViewModel,
                stepSessionListViewModel,
                stepSessionIdViewModel,
                stepRecordingViewModel,
                stepDeleteViewModel,
                altitudeListViewModel,
                altitudeSessionCountViewModel,
                altitudeSessionListViewModel,
                altitudeSessionIdViewModel,
                altitudeRecordingViewModel,
                altitudeDeleteViewModel,
                onNavigateToAltitudeRecording = {
                    navController.navigate("altitude_profile_recording") },
                headingViewModel = headingViewModel,
                stepViewModel = stepViewModel,
                verticalSpeedViewModel = verticalSpeedViewModel,
                pressureViewModel = pressureViewModel,
                distanceViewModel = distanceViewModel,
                gPSAltitudeViewModel = gPSAltitudeViewModel,
                gPSAltitudeSessionDao,
                gPSAltitudeSessionIdViewModel,
                gPSAltitudeRecordingViewModel,
                locationListViewModel,
                locationRecordingViewModel,
                locationSessionIdViewModel,
                locationSessionDao,
                locationSessionListViewModel,
                locationSampleDao,
                locationSessionCountViewModel,
                navigationViewModel,
                chartDistanceViewModel = chartDistanceViewModel
            )
        }
        composable("sun_moon") {
            SunMoonScreen(
                location1
            )
        }
        composable("compass") {
            CompassScreen(
                navController,
                location1,
                //azimuth,
                magnetometerAccuracy,
                headingViewModel
            )
        }
        composable("gnss") {
            GNSSScreen(gnssStatus)
        }
        composable("altitude_profile_recording") {
            AltitudeProfileRecordingScreen(
                //sampledAltitudes,
                altitudeListViewModel,
                altitudeRecordingViewModel,
                navController,
                altitudeSessionIdViewModel,
                gPSAltitudeListViewModel = gPSAltitudeListViewModel,
                gPSAltitudeSessionIdViewModel,
                gPSAltitudeRecordingViewModel
            )
        }
        composable("altitude_profile_viewing") {
            AltitudeProfileViewingScreen(
                //sampledAltitudes,
                altitudeListViewModel,
                altitudeSessionIdViewModel,
                gPSAltitudeListViewModel,
                gPSAltitudeSessionIdViewModel
            )
        }
        composable("steps_profile_recording") {
            StepsProfileRecordingScreen(
                //stepsDeque,
                //stepsTimesDeque,
                //stepSampleDao,
                stepListViewModel,
                stepRecordingViewModel,
                navController,
                stepSessionIdViewModel
            )
        }
        composable("steps_profile_viewing") {
            StepsProfileViewingScreen(
                //stepsDeque,
                //stepsTimesDeque,
                //stepSampleDao,
                stepListViewModel,
                //stepRecordingViewModel,
                stepSessionIdViewModel
            )
        }
        composable("distance_profile_recording") {
            DistanceProfileRecordingScreen(
                //stepsDeque,
                //stepsTimesDeque,
                //stepSampleDao,
                locationListViewModel,
                locationRecordingViewModel,
                navController,
                locationSessionIdViewModel,
                locationSampleViewModel
            )
        }
        composable("distance_profile_viewing") {
            DistanceProfileViewingScreen(
                //stepsDeque,
                //stepsTimesDeque,
                //stepSampleDao,
                locationListViewModel,
                //stepRecordingViewModel,
                locationSessionIdViewModel,
                locationSampleViewModel,
                navigationViewModel
            )
        }
        composable("vertical_speed") {
            VerticalSpeedScreen(
                //altitudeSlope,
                verticalSpeedViewModel)
        }
    }

    fun onNavigateToAltitudeRecording() {
        navController.navigate("apr")
    }
}
