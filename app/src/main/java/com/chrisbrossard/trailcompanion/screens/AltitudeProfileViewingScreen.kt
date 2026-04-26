package com.chrisbrossard.trailcompanion.screens

import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.AltitudeSessionIdViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun AltitudeProfileViewingScreen(
    altitudeListViewModel: AltitudeListViewModel,
    altitudeSessionIdViewModel: AltitudeSessionIdViewModel,
) {
    val rowList by altitudeListViewModel.rowList.collectAsState(initial = emptyList())
    val sessionId = altitudeSessionIdViewModel.getSessionId()

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
                    val entries = ArrayList<Entry>()
                    var flag1 = false
                    for (sample in rowList) {
                        if (sessionId == sample.sessionId) {
                            flag1 = true
                            break
                        }
                    }
                    if (flag1) {
                        for (sample in rowList) {
                            val entry = Entry(
                                sample.time.toFloat() / (1000 * 60),
                                sample.altitude)
                            if (sample.sessionId == sessionId) {
                                entries.add(entry)
                            }
                        }
                        val dataSet1 = LineDataSet(entries, "baro altitudes").apply {
                        }
                        dataSet1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        dataSet1.label = "Altitudes (m)"
                        dataSet1.lineWidth = 4.0f
                        dataSet1.setDrawValues(false)


                        chart.data = LineData(dataSet1) //, dataSet2)
                        chart.setScaleEnabled(true)
                        val description = Description()
                        description.text = "Altitude Profile"
                        chart.description = description
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

//import android.graphics.Color
//import android.hardware.SensorManager
//import android.hardware.SensorManager.PRESSURE_STANDARD_ATMOSPHERE
//import android.location.Location
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeListViewModel
//import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionIdViewModel
//import com.chrisbrossard.trailcompanion.viewmodel.SeaLevelPressureViewModel
//import kotlin.math.pow

//altitudes: ArrayDeque<Int>,
//gPSAltitudeListViewModel: GPSAltitudeListViewModel,
//gPSAltitudeSessionIdViewModel: GPSAltitudeSessionIdViewModel,
//location: Location,
//seaLevelPressureViewModel: SeaLevelPressureViewModel

//val gPSRowList by gPSAltitudeListViewModel.rowList.collectAsState(initial = emptyList())
//val gPSSessionId = gPSAltitudeSessionIdViewModel.getSessionId()
//val seaLevelPressure by seaLevelPressureViewModel.pressure.collectAsState()

//val altitudeViewModel: AltitudeViewModel = viewModel()

//if (altitudes.isNotEmpty()) {
/*var index = 0f
for (value in altitudes) {
    entries.add(Entry(index, value.toFloat()))
    index++
}*/
/*var flag2 = false
for (sample in gPSRowList) {
    if (gPSSessionId == sample.sessionId) {
        flag2 = true
        break
    }
}*/

/*var seaLevelPressure = PRESSURE_STANDARD_ATMOSPHERE
if (location.altitude != 0.0) { //gPSAltitude != 0.0) {
    seaLevelPressure = (sample.pressure /
            (1 - location.altitude / 44330.0).pow(5.255)).toFloat()
}*/
/*val a = SensorManager.getAltitude(
    seaLevelPressure, //SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
    sample.pressure
)*/

//chart.data = LineData(dataSet)
//dataSet.setDrawFilled(true)
//dataSet.fillColor = 0x00FF00
//dataSet.fillAlpha = 128
//dataSet1.setDrawCircles(false)

/*entries = ArrayList()
for (sample in gPSRowList) { //samples) {

    val entry = Entry(
        sample.time.toFloat() / (1000 * 60),
        sample.altitude)
    if (sample.sessionId == gPSSessionId) {
        entries.add(entry)
    }
}
val dataSet2 = LineDataSet(entries, "gps_altitudes").apply {
}
//chart.data = LineData(dataSet)
dataSet2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
dataSet2.label = "GPS Altitudes (m)"
//dataSet.setDrawFilled(true)
//dataSet.fillColor = 0x00FF00
//dataSet.fillAlpha = 128
dataSet2.setColor(Color.MAGENTA)
daataSet2.setCircleColor(Color.MAGENTA)
dataSet2.lineWidth = 4.0f
//dataSet2.setDrawCircles(false)
dataSet2.setDrawValues(false)*/

/*chart.zoom(
    1 / altitudes.size.toFloat(),
    1f,
    index,
    altitudes.last().toFloat(),
    YAxis.AxisDependency.RIGHT
)*/
