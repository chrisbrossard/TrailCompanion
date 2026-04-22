package com.chrisbrossard.trailcompanion.screens

import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.chrisbrossard.trailcompanion.MainActivity
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeRecordingViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionIdViewModel
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeListViewModel
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeRecordingViewModel
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionIdViewModel
//import com.chrisbrossard.trailcompanion.viewmodel.SeaLevelPressureViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

//@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AltitudeProfileRecordingScreen(
    //altitudes: ArrayDeque<Int>,
    altitudeListViewModel: AltitudeListViewModel,
    altitudeRecordingViewModel: AltitudeRecordingViewModel,
    navController: NavHostController,
    altitudeSessionIdViewModel: AltitudeSessionIdViewModel,
    //gPSAltitudeListViewModel: GPSAltitudeListViewModel,
    //gPSAltitudeSessionIdViewModel: GPSAltitudeSessionIdViewModel,
    //gPSAltitudeRecordingViewModel: GPSAltitudeRecordingViewModel,
    //location: Location,
    //seaLevelPressureViewModel: SeaLevelPressureViewModel
) {
    //val altitudeListViewModel: AltitudeListViewModel = viewModel()
    val rowList by altitudeListViewModel.rowList.collectAsState(initial = emptyList())
    //val gPSRowList by gPSAltitudeListViewModel.rowList.collectAsState(initial = emptyList())
    //var clickedText by remember { mutableStateOf("Stop Recording") }
    //val altitudeViewModel: AltitudeViewModel = viewModel()
    val sessionId = altitudeSessionIdViewModel.getSessionId()
    //val gPSSessionId = gPSAltitudeSessionIdViewModel.getSessionId()
    //val seaLevelPressure by seaLevelPressureViewModel.pressure.collectAsState()

        BackHandler {
        altitudeRecordingViewModel.updateRecording(MainActivity.Recording.OFF.ordinal)
        //gPSAltitudeRecordingViewModel.updateRecording(MainActivity.Recording.OFF.ordinal)

        navController.popBackStack("overview", false)
    }

    /*Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    altitudeRecordingViewModel.updateRecording(false)
                    clickedText =  if (clickedText == "Stop Recording") "Stop Recording" else
                        "Recording Stopped"
                }
            ) {
            }
        }
    ) { innerPadding ->*/
    Column {
        Box(
            Modifier
                .weight(0.1f)
                .fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    LineChart(context)
                },
                update = { chart ->
                    //if (altitudes.isNotEmpty()) {
                    val entries = ArrayList<Entry>()
                    /*var index = 0f
                        for (value in altitudes) {
                            entries.add(Entry(index, value.toFloat()))
                            index++
                        }*/
                    var flag1 = false
                    for (sample in rowList) {
                        if (sessionId == sample.sessionId) {
                            flag1 = true
                            break
                        }
                    }
                    /*var flag2 = false
                    for (sample in gPSRowList) {
                        if (sessionId == sample.sessionId) {
                            flag2 = true
                            break
                        }
                    }*/
                    if (flag1) { // || flag2) {

                        for (sample in rowList) { //samples) {
                            /*var seaLevelPressure = PRESSURE_STANDARD_ATMOSPHERE
                            if (location.altitude != 0.0) { //gPSAltitude != 0.0) {
                                seaLevelPressure = (sample.pressure /
                                        (1 - location.altitude / 44330.0).pow(5.255)).toFloat()
                            }*/

                            /*val a = SensorManager.getAltitude(
                                seaLevelPressure, //SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                                sample.pressure
                            )*/
                            val entry = Entry(
                                sample.time.toFloat() / (1000 * 60),
                                sample.altitude
                            )
                            if (sample.sessionId == sessionId) {
                                entries.add(entry)
                            }
                        }
                        //entries.sortedBy { it.x }
                        val dataSet1 = LineDataSet(entries, "altitudes").apply {
                        }
                        //chart.data = LineData(dataSet)
                        dataSet1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        dataSet1.label = "Altitudes (m)"
                        //dataSet.setDrawFilled(true)
                        //dataSet.fillColor = 0x00FF00
                        //dataSet.fillAlpha = 128
                        dataSet1.lineWidth = 4.0f
                        //dataSet1.setDrawCircles(false)
                        dataSet1.setDrawValues(false)

                        /*entries = ArrayList()
                        for (sample in gPSRowList) { //samples) {
                            val entry = Entry(
                                sample.time.toFloat() / (1000 * 60),
                                sample.altitude
                            )
                            if (sample.sessionId == gPSSessionId) {
                                entries.add(entry)
                            }
                        }
                        //entries.sortedBy { it.x }
                        val dataSet2 = LineDataSet(entries, "gps_altitudes").apply {
                        }
                        //chart.data = LineData(dataSet)
                        dataSet2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        dataSet2.label = "GPS Altitudes (m)"
                        //dataSet.setDrawFilled(true)
                        //dataSet.fillColor = 0x00FF00
                        //dataSet.fillAlpha = 128
                        dataSet2.lineWidth = 4.0f
                        dataSet2.setColor(Color.MAGENTA)
                        dataSet2.setCircleColor(Color.MAGENTA)
                        //dataSet2.setDrawCircles(false)
                        dataSet2.setDrawValues(false)*/

                        chart.data = LineData(dataSet1)//, dataSet2)
                        chart.setScaleEnabled(true)
                        chart.setDrawGridBackground(false)
                        chart.xAxis.setDrawAxisLine(false)
                        val description = Description()
                        description.text = "Altitude Profile"
                        chart.description = description
                        /*chart.zoom(
                            1 / altitudes.size.toFloat(),
                            1f,
                            index,
                            altitudes.last().toFloat(),
                            YAxis.AxisDependency.RIGHT
                        )*/
                        chart.xAxis.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        chart.axisLeft.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        chart.axisRight.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        chart.legend.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        chart.description.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        chart.invalidate()
                    }
                }
            )
        }
        Box(
            Modifier
                .weight(0.1f)
                .fillMaxSize(),
        )
    }
}
