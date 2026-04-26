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
import com.chrisbrossard.trailcompanion.viewmodel.StepListViewModel
import com.chrisbrossard.trailcompanion.viewmodel.StepSessionIdViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun StepsProfileViewingScreen(
    stepListViewModel: StepListViewModel,
    stepSessionIdViewModel: StepSessionIdViewModel
) {
    val rowList by stepListViewModel.rowList.collectAsState(initial = emptyList())
    val sessionId = stepSessionIdViewModel.getSessionId()

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
                        val entries = ArrayList<Entry>()
                        var flag = false
                        for (sample in rowList) {
                            if (sessionId == sample.sessionId) {
                                flag = true
                                break
                            }
                        }
                        if (flag) {
                            for (sample in rowList) { //samples) {
                                val entry = Entry(
                                    sample.time.toFloat() /
                                            (MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE),
                                    sample.steps.toFloat()
                                )
                                if (sample.sessionId == sessionId) {
                                    entries.add(entry)
                                }
                            }
                            val dataSet = LineDataSet(entries, "set").apply {
                            }
                            dataSet.mode = LineDataSet.Mode.LINEAR
                            dataSet.label = "Steps"
                            dataSet.setDrawFilled(true)
                            dataSet.fillColor = 0x964B00
                            dataSet.fillAlpha = 128
                            dataSet.setDrawCircles(false)
                            dataSet.setDrawValues(false)
                            chart.data = LineData(dataSet)
                            chart.setScaleEnabled(true)
                            val description = Description()
                            description.text = "Steps Profile"
                            chart.description = description
                            chart.xAxis.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.axisLeft.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.axisRight.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.legend.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.description.typeface =
                                Typeface.defaultFromStyle(Typeface.BOLD)
                            chart.invalidate()
                        }
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

//steps: ArrayDeque<Long>,
//stepsTimes: ArrayDeque<Long>,
//stepSampleDao: StepSampleDao,
//stepRecordingViewModel: StepRecordingViewModel,
//val viewModel: MainActivity.StepListViewModel = viewModel()

//if (steps.isNotEmpty()) {
/*val entries: List<Entry> = stepsTimes.zip(steps).map { (x, y) ->
    Entry(x.toFloat() / (1000f * 60f), y.toFloat()) // to minutes
}
val dataSet = LineDataSet(entries, "set").apply {
}*/
//val samples = stepSampleDao.getAll()

/*chart.zoom(
1 / stepsTimes.size.toFloat(),
1f,
stepsTimes.last().toFloat(),
steps.last().toFloat(),
YAxis.AxisDependency.RIGHT
)*/
