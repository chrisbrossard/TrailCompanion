package com.chrisbrossard.trailcompanion

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.Location
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
import com.chrisbrossard.trailcompanion.screens.AltitudeProfileRecordingScreen
import com.chrisbrossard.trailcompanion.screens.AltitudeProfileViewingScreen
import com.chrisbrossard.trailcompanion.screens.CompassScreen
import com.chrisbrossard.trailcompanion.screens.DistanceProfileRecordingScreen
import com.chrisbrossard.trailcompanion.screens.DistanceProfileViewingScreen
import com.chrisbrossard.trailcompanion.screens.GNSSScreen
import com.chrisbrossard.trailcompanion.screens.OverviewScreen
import com.chrisbrossard.trailcompanion.screens.SunMoonScreen
import com.chrisbrossard.trailcompanion.screens.VerticalSpeedScreen
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.ChartDistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.HeadingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.PressureViewModel
import com.chrisbrossard.trailcompanion.viewmodel.VerticalSpeedViewModel
import com.chrisbrossard.trailcompanion.viewmodel.DistanceViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionCountViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionIdViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.SeaLevelPressureViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@SuppressLint()
@Composable
fun Navigation(
    client: FusedLocationProviderClient,
    gnssStatus: GnssStatus?,
    magnetometerAccuracy: Int,
    altitudeSampleDao: AltitudeSampleDao,
    altitudeSessionDao: AltitudeSessionDao,
    altitudeListViewModel: AltitudeListViewModel,
    altitudeSessionCountViewModel: AltitudeSessionCountViewModel,
    altitudeSessionListViewModel: AltitudeSessionListViewModel,
    altitudeSessionIdViewModel: AltitudeSessionIdViewModel,
    altitudeRecordingViewModel: AltitudeRecordingViewModel,
    navController: NavHostController,
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
    chartDistanceViewModel: ChartDistanceViewModel,
    seaLevelPressureViewModel: SeaLevelPressureViewModel
) {
    var location1 by remember { mutableStateOf(Location("")) }

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
               altitudeSampleDao,
               altitudeSessionDao,
                altitudeSessionCountViewModel,
                altitudeSessionListViewModel,
                altitudeSessionIdViewModel,
                altitudeRecordingViewModel,
                onNavigateToAltitudeRecording = {
                    navController.navigate("altitude_profile_recording") },
                headingViewModel = headingViewModel,
                verticalSpeedViewModel = verticalSpeedViewModel,
                pressureViewModel = pressureViewModel,
                distanceViewModel = distanceViewModel,
                locationRecordingViewModel,
                locationSessionIdViewModel,
                locationSessionDao,
                locationSessionListViewModel,
                locationSampleDao,
                locationSessionCountViewModel,
                chartDistanceViewModel = chartDistanceViewModel,
                seaLevelPressureViewModel = seaLevelPressureViewModel
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
                magnetometerAccuracy,
                headingViewModel
            )
        }
        composable("gnss") {
            GNSSScreen(gnssStatus)
        }
        composable("altitude_profile_recording") {
            AltitudeProfileRecordingScreen(
                altitudeListViewModel,
                altitudeRecordingViewModel,
                navController,
                altitudeSessionIdViewModel,
            )
        }
        composable("altitude_profile_viewing") {
            AltitudeProfileViewingScreen(
                altitudeListViewModel,
                altitudeSessionIdViewModel,
            )
        }
        composable("distance_profile_recording") {
            DistanceProfileRecordingScreen(
                locationListViewModel,
                locationRecordingViewModel,
                navController,
                locationSessionIdViewModel,
            )
        }
        composable("distance_profile_viewing") {
            DistanceProfileViewingScreen(
                locationListViewModel,
               locationSessionIdViewModel,
            )
        }
        composable("vertical_speed") {
            VerticalSpeedScreen(
                verticalSpeedViewModel)
        }
    }
    /*fun onNavigateToAltitudeRecording() {
        navController.navigate("apr")
    }*/
}

//azimuth: Float,
//pressure: Float,
//altitudeSlope: Double,
//sampledAltitudes: ArrayDeque<Int>,
//stepsDeque: ArrayDeque<Long>,
//stepsTimesDeque: ArrayDeque<Long>,
//stepsSpeed: Float,
//stepSampleDao: StepSampleDao,
//stepSessionDao: StepSessionDao,
//steps: Int,
/*stepCountViewModel: StepCountViewModel,
stepListViewModel: StepListViewModel,
stepSessionCountViewModel: StepSessionCountViewModel,
stepSessionListViewModel: StepSessionListViewModel,
stepSessionIdViewModel: StepSessionIdViewModel,
stepRecordingViewModel: StepRecordingViewModel,
stepDeleteViewModel: StepDeleteViewModel,*/
//stepViewModel: StepViewModel,
/*gPSAltitudeViewModel: GPSAltitudeViewModel,
gPSAltitudeSessionDao: GPSAltitudeSessionDao,
gPSAltitudeSessionIdViewModel: GPSAltitudeSessionIdViewModel,
gPSAltitudeListViewModel: GPSAltitudeListViewModel,
gPSAltitudeRecordingViewModel: GPSAltitudeRecordingViewModel,*/
//locationSampleViewModel: LocationSampleViewModel,

//altitudeSlope,
//pressure,
//azimuth,
//sampledAltitudes,
//stepsDeque,
//stepsSpeed,
//stepSampleDao,
//stepSessionDao,
//steps,
/*stepCountViewModel,
stepListViewModel,
stepSessionCountViewModel,
stepSessionListViewModel,
stepSessionIdViewModel,
stepRecordingViewModel,
stepDeleteViewModel,*/
//stepViewModel = stepViewModel,
/*gPSAltitudeViewModel = gPSAltitudeViewModel,
gPSAltitudeSessionDao,
gPSAltitudeSessionIdViewModel,
gPSAltitudeRecordingViewModel,*/

//sampledAltitudes,
//gPSAltitudeListViewModel = gPSAltitudeListViewModel,
//gPSAltitudeSessionIdViewModel,
//gPSAltitudeRecordingViewModel,
//location1,
//seaLevelPressureViewModel = seaLevelPressureViewModel

/*composable("steps_profile_recording") {
    StepsProfileRecordingScreen(
        //stepsDeque,
        //stepsTimesDeque,
        //stepSampleDao,
        //stepListViewModel,
        //stepRecordingViewModel,
        navController,
        //stepSessionIdViewModel
    )
}*/
/*composable("steps_profile_viewing") {
    StepsProfileViewingScreen(
        //stepsDeque,
        //stepsTimesDeque,
        //stepSampleDao,
        stepListViewModel,
        //stepRecordingViewModel,
        stepSessionIdViewModel
    )
}*/

//sampledAltitudes,
//gPSAltitudeListViewModel,
//gPSAltitudeSessionIdViewModel,
//location1,
//seaLevelPressureViewModel = seaLevelPressureViewModel

//stepsDeque,
//stepsTimesDeque,
//stepSampleDao,
//locationSampleViewModel

//stepsDeque,
//stepsTimesDeque,
//stepSampleDao,
//stepRecordingViewModel,
//locationSampleViewModel,
//navigationViewModel
