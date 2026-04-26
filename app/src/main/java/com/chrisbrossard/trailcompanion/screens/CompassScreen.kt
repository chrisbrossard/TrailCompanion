package com.chrisbrossard.trailcompanion.screens

//import android.Manifest
//import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.chrisbrossard.trailcompanion.R
import com.chrisbrossard.trailcompanion.viewmodel.HeadingViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalTime::class)
@Composable
fun CompassScreen(
    navController: NavHostController,
    location: Location,
    magnetometerAccuracy: Int,
    headingViewModel: HeadingViewModel
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    val state = rememberTransformableState { _, _, rotationChange ->
        rotation += rotationChange
    }
    var latitudeString = doubleToDMSString(abs(location.latitude))
    var longitudeString = doubleToDMSString(abs(location.longitude))
    val heading by headingViewModel.heading.collectAsState()

    var trend = " ${stringResource(R.string.south)}"
    if (location.latitude > 0f) {
        trend = " ${stringResource(R.string.north)}"
    }
    latitudeString += trend
    trend = " ${stringResource(R.string.west)}"
    if (location.longitude > 0f) {
        trend = " ${stringResource(R.string.east)}"
    }
    longitudeString += trend

    Box(
        modifier = Modifier
            .graphicsLayer(rotationZ = rotation)
            .transformable(state = state)
            .clickable {
                navController.navigate("gnss")
            }
    ) {

        val fontSizeInSp = 30.sp
        val northStyle = TextStyle(
            fontSize = fontSizeInSp,
            fontWeight = FontWeight.Bold
        )
        val style = TextStyle(fontSize = fontSizeInSp)
        val compassAnnotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Green)) {
                append(stringResource(R.string.gps))
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("/")
            }
            withStyle(style = SpanStyle(color = Color.Blue)) {
                append(stringResource(R.string.magnetic))
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append(" " + stringResource(R.string.compass))
            }
        }
        val compassMeasuredText = rememberTextMeasurer().measure(
            text = compassAnnotatedString,
            style = TextStyle()
        )
        val northMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.north)),
            style = northStyle
        )
        val southMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.south)),
            style = style
        )
        val eastMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.east)),
            style = style
        )
        val westMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.west)),
            style = style
        )
        val latitudeMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(latitudeString),
            style = TextStyle()
        )
        val longitudeMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(longitudeString),
            style = TextStyle()
        )
        val geomagneticField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            Clock.System.now().toEpochMilliseconds()
        )
        var declinationTrend = stringResource(R.string.east)
        if (geomagneticField.declination < 0) {
            declinationTrend = stringResource(R.string.west)
        }
        val declinationMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(
                stringResource(R.string.magnetic_north) + " " + geomagneticField.declination.toInt()
                    .toString() + "\u00B0 " +
                        " " + declinationTrend
            ),
            style = TextStyle()
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 2,
                center = center,
                alpha = 8f,
                style = Stroke(width = 8f)
            )
            for (i in 0 until 360 step 90) {
                rotate(i.toFloat()) {
                    drawLine(
                        brush = SolidColor(Color.Black),
                        start = Offset(center.x, center.y - size.minDimension / 2),
                        end = Offset(center.x, center.y - size.minDimension / 2 + 100f),
                        strokeWidth = 6f,
                        alpha = 1f
                    )
                }
            }
            for (i in 45 until 360 step 90) {
                rotate(i.toFloat()) {
                    drawLine(
                        brush = SolidColor(Color.Black),
                        start = Offset(center.x, center.y - size.minDimension / 2),
                        end = Offset(center.x, center.y - size.minDimension / 2 + 50f),
                        strokeWidth = 4f,
                        alpha = 1f
                    )
                }
            }
            generateSequence(22.5f) { it + 45.0f }
                .takeWhile { it < 360.0f }
                .forEach {
                    rotate(it) {
                        drawLine(
                            brush = SolidColor(Color.Black),
                            start = Offset(center.x, center.y - size.minDimension / 2),
                            end = Offset(center.x, center.y - size.minDimension / 2 + 25f),
                            strokeWidth = 2f,
                            alpha = 1f
                        )
                    }
                }
            drawText(
                textLayoutResult = northMeasuredText,
                topLeft = Offset(
                    center.x - northMeasuredText.size.width / 2,
                    center.y - size.minDimension / 2 + 100
                )
            )
            drawText(
                textLayoutResult = southMeasuredText,
                topLeft = Offset(
                    center.x - southMeasuredText.size.width / 2,
                    center.y + size.minDimension / 2 - 100 - southMeasuredText.size.height
                )
            )
            drawText(
                textLayoutResult = eastMeasuredText,
                topLeft = Offset(
                    center.x + size.minDimension / 2 - 100 - eastMeasuredText.size.width,
                    center.y - eastMeasuredText.size.height / 2
                )
            )
            drawText(
                textLayoutResult = westMeasuredText,
                topLeft = Offset(
                    center.x - size.minDimension / 2 + 100f,
                    center.y - eastMeasuredText.size.height / 2
                )
            )
            if (location.latitude != 0.0) {
                drawText(
                    textLayoutResult = declinationMeasuredText,
                    topLeft = Offset(
                        center.x - declinationMeasuredText.size.width / 2,
                        center.y - size.minDimension / 4 +
                                compassMeasuredText.size.height
                    )
                )
                drawText(
                    textLayoutResult = latitudeMeasuredText,
                    topLeft = Offset(
                        center.x - latitudeMeasuredText.size.width / 2,
                        center.y + size.minDimension / 4 -
                                latitudeMeasuredText.size.height * 2
                    )
                )
                drawText(
                    textLayoutResult = longitudeMeasuredText,
                    topLeft = Offset(
                        center.x - longitudeMeasuredText.size.width / 2,
                        center.y + size.minDimension / 4 -
                                longitudeMeasuredText.size.height
                    )
                )
            }
            var delta = 0
            when (magnetometerAccuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    delta = 20
                }

                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    delta = 15
                }

                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    delta = 10
                }

                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    delta = 5
                }
                else -> {
                    delta = 45
                }
            }
            rotate(degrees = -heading - rotation) {
                val size1 = Size(
                    size.minDimension,
                    size.minDimension
                )
                drawArc(
                    brush = SolidColor(Color.Blue),
                    startAngle = -delta.toFloat() / 2 - 90,
                    sweepAngle = delta.toFloat(),
                    useCenter = true,
                    topLeft = Offset(
                        center.x - size.minDimension / 2,
                        center.y - size.minDimension / 2
                    ),
                    size = size1,
                    alpha = 0.329f,
                    style = Fill,
                )
            }
        }
    }
}

private fun doubleToDMSString(value: Double): String {
    var s = ""
    s += value.toInt().toString()
    s += "\u00b0 "
    val minutes = value - value.toInt().toDouble()
    s += (minutes * 60).roundToInt().toString()
    s += "\u2032 "
    val seconds = (minutes * 60) - (minutes * 60).toInt().toDouble()
    s += seconds.roundToInt().toString()
    s += "\u2033"
    return s
}