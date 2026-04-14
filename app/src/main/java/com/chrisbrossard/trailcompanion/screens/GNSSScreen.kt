package com.chrisbrossard.trailcompanion.screens

import android.location.GnssStatus
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.chrisbrossard.trailcompanion.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GNSSScreen(status: GnssStatus?) {
    Box {
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Green)) {
                append("GPS")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("/")
            }
            withStyle(style = SpanStyle(color = Color.Blue)) {
                append("Galileo")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("/")
            }
            withStyle(style = SpanStyle(color = Color.Magenta)) {
                append("GLONASS")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append(" Satellites")
            }
        }
        val measuredText = rememberTextMeasurer().measure(
            text = annotatedString,
            style = TextStyle()
        )
        val fontSizeInSp = 30.sp
        val style = TextStyle(fontSize = fontSizeInSp)
        val northStyle = TextStyle(
            fontSize = fontSizeInSp,
            fontWeight = FontWeight.Bold
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
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 2,
                center = center,
                alpha = 8f,
                style = Stroke(width = 8f),
            )
            for (i in 0 until 360 step 90) {
                rotate(i.toFloat()) {
                    drawLine(
                        brush = SolidColor(Color.Black),
                        start = Offset(center.x, center.y - size.minDimension / 2),
                        end = Offset(center.x, center.y - size.minDimension / 2 + 100f),
                        strokeWidth = 6f,
                        alpha = 1f,
                    )
                }
            }
            if (status != null) {
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(
                        center.x - measuredText.size.width / 2,
                        center.y - size.minDimension / 4
                    )
                )
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
                        center.y + size.minDimension / 2 - 100 -
                                southMeasuredText.size.height
                    )
                )
                drawText(
                    textLayoutResult = eastMeasuredText,
                    topLeft = Offset(
                        center.x + size.minDimension / 2 - 100 -
                                eastMeasuredText.size.width,
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
                for (i in 0 until status.satelliteCount) {
                    val correctedRadius = cos(
                        Math.toRadians(status.getElevationDegrees(i).toDouble())
                    ) * size.width / 2
                    val x = sin(Math.toRadians(status.getAzimuthDegrees(i).toDouble())) *
                            correctedRadius
                    val y = cos(Math.toRadians(status.getAzimuthDegrees(i).toDouble())) *
                            correctedRadius
                    var color = Color.Black
                    when (status.getConstellationType(i)) {
                        GnssStatus.CONSTELLATION_GPS -> {
                            color = Color.Green
                        }

                        GnssStatus.CONSTELLATION_GLONASS -> {
                            color = Color.Magenta
                        }

                        GnssStatus.CONSTELLATION_GALILEO -> {
                            color = Color.Blue
                        }

                        GnssStatus.CONSTELLATION_BEIDOU -> {
                            color = Color.Red
                        }

                        GnssStatus.CONSTELLATION_QZSS -> {
                            color = Color.Yellow
                        }

                        GnssStatus.CONSTELLATION_IRNSS -> {
                            color = Color.Cyan
                        }

                        GnssStatus.CONSTELLATION_SBAS -> {
                            color = Color.DarkGray
                        }

                        GnssStatus.CONSTELLATION_UNKNOWN -> {
                            color = Color.Black
                        }
                    }
                    var style: DrawStyle = Stroke(width = 2f)
                    if (status.usedInFix(i)) {
                        style = Fill
                    }
                    drawCircle(
                        color = color,
                        radius = status.getCn0DbHz(i),
                        center = Offset(
                            (x + size.width / 2 - 10).toFloat(),
                            (size.height / 2 - y - 10).toFloat()
                        ),
                        alpha = 1f,
                        style = style, //Fill, //Stroke(width = 2f),
                    )
                }
            }
        }
    }
}
