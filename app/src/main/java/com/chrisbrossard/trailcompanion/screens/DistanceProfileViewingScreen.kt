package com.chrisbrossard.trailcompanion.screens

import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.location.Location

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.chrisbrossard.trailcompanion.viewmodel.LocationListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.LocationSessionIdViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


//@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun DistanceProfileViewingScreen(
    //steps: ArrayDeque<Long>,
    //stepsTimes: ArrayDeque<Long>,
    //stepSampleDao: StepSampleDao,
    locationListViewModel: LocationListViewModel,
    //locationRecordingViewModel: LocationRecordingViewModel,
    //navController: NavHostController,
    locationSessionIdViewModel: LocationSessionIdViewModel,
    //locationSampleViewModel: LocationSampleViewModel,
    //navigationViewModel: NavigationViewModel
) {
    //val viewModel: MainActivity.StepListViewModel = viewModel()
    val rowList by locationListViewModel.rowList.collectAsState(initial = emptyList())
    //var clickedText by remember { mutableStateOf("Stop Recording") }
    val sessionId = locationSessionIdViewModel.getSessionId()

    /*BackHandler(enabled = true) {
        locationRecordingViewModel.updateRecording(MainActivity.Recording.OFF.ordinal)
        navController.popBackStack()
    }*/

    /*Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    stepRecordingViewModel.updateRecording(false)
                    clickedText = if (clickedText == "Stop Recording") "Recording Stopped"
                    else "Stop Recording"
                }
            ) {
                Text("+")
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
            Column {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        LineChart(context)
                    },
                    update = { chart ->
                        //if (steps.isNotEmpty()) {
                        /*val entries: List<Entry> = stepsTimes.zip(steps).map { (x, y) ->
                            Entry(x.toFloat() / (1000f * 60f), y.toFloat()) // to minutes
                        }
                        val dataSet = LineDataSet(entries, "set").apply {
                        }*/
                        //val samples = stepSampleDao.getAll()
                        val entries = ArrayList<Entry>()
                        var flag = false
                        for (sample in rowList) {
                            if (sessionId == sample.sessionId) {
                                flag = true
                                break
                            }
                        }
                        if (flag) {
                            var distance = 0f
                            var location = Location("")
                            for (sample in rowList) { //samples) {
                                if (sessionId == sample.sessionId) {
                                    val newLocation = Location("")
                                    newLocation.latitude = sample.latitude
                                    newLocation.longitude = sample.longitude
                                    if (location.latitude != 0.0) {
                                        distance += location.distanceTo(newLocation)
                                    }
                                    location = newLocation
                                    val entry = Entry(
                                        sample.time.toFloat() /
                                                (MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE),
                                        distance
                                    )
                                    entries.add(entry)
                                }
                            }
                            val dataSet = LineDataSet(entries, "set").apply {
                            }
                            dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                            dataSet.label = "distance m"
                            //dataSet.setDrawFilled(true)
                            //dataSet.fillColor = 0x964B00
                            //dataSet.fillAlpha = 128
                            //dataSet.setDrawCircles(false)
                            dataSet.setDrawValues(false)
                            //dataSet.setCircleColor(Color.RED)
                            dataSet.lineWidth = 4.0f
                            chart.data = LineData(dataSet)
                            chart.setScaleEnabled(true)
                            val description = Description()
                            description.text = "Distance Profile"
                            chart.description = description
                            /*chart.setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
                                override fun onValueSelected(e: Entry?, h: Highlight?) {
                                    //var sampleId = 0
                                    if (e != null) {
                                        val index = dataSet.getEntryIndex(e)
                                        //sampleId = e.data as Int
                                        val serviceScope =
                                            CoroutineScope(SupervisorJob() + Dispatchers.IO)
                                        serviceScope.launch {
                                            val sample =
                                                //locationSampleViewModel.getSample(sampleId)
                                                locationSampleViewModel.getSample(index)
                                            navigationViewModel.setWaypoint(
                                                sample.latitude,
                                                sample.longitude
                                            )
                                            navigationViewModel.navigating = true
                                            //h?.dataIndex
                                        }
                                    }
                                }

                                override fun onNothingSelected() {
                                }
                            })*/
                            /*chart.zoom(
                            1 / stepsTimes.size.toFloat(),
                            1f,
                            stepsTimes.last().toFloat(),
                            steps.last().toFloat(),
                            YAxis.AxisDependency.RIGHT
                        )*/
                            chart.xAxis.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.axisLeft.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.axisRight.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.legend.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.description.typeface =
                                Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.invalidate()
                        }
                        //}
                    }
                )
            }
        }
        Box(
            Modifier
                .weight(0.1f)
                .fillMaxSize(),
        )
    }
}
//}
