package com.chrisbrossard.trailcompanion.screens

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import com.chrisbrossard.trailcompanion.R
import dev.jamesyox.kastro.luna.calculateLunarIllumination
import dev.jamesyox.kastro.luna.calculateLunarPosition
import dev.jamesyox.kastro.sol.calculateSolarState
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.ranges.step
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SunMoonScreen(
    location: Location
) {
    var startTimeAngle by remember { mutableIntStateOf(0) }

    Box {
        val style = TextStyle()
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color(255, 165, 0))) {
                append(stringResource(R.string.sun))
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("/")
            }
            withStyle(style = SpanStyle(color = Color.Blue)) {
                append(stringResource(R.string.moon))
            }
        }
        val measuredText = rememberTextMeasurer().measure(
            text = annotatedString,
            style = style
        )
        val sixAmMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.six_am)),
            style = style
        )
        val noonMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.noon)),
            style = style
        )
        val sixPmMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(stringResource(R.string.six_pm)),
            style = style
        )
        val midnightMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("midnight"),
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
                alpha = 1f,
                style = Stroke(width = 8f),
            )

            val currentDateTimeInZone = ZonedDateTime.now(ZoneId.systemDefault())
            val currentHours = currentDateTimeInZone.hour
            val currentMinutes = currentDateTimeInZone.minute
            val totalMinutes = currentHours * 60 + currentMinutes
            val currentTimeAngle = totalMinutes * 360 / 1440

            if (startTimeAngle == 0) {
                startTimeAngle = currentTimeAngle
            }

            rotate(startTimeAngle.toFloat()) {
                drawLine(
                    brush = SolidColor(Color.Red),
                    start = Offset(center.x, center.y),
                    end = Offset(
                        center.x,
                        center.y - size.minDimension / 2
                    ),
                    strokeWidth = 1f,
                    alpha = 1f,
                )
            }

            rotate(currentTimeAngle.toFloat()) {
                drawLine(
                    brush = SolidColor(Color.Red),
                    start = Offset(center.x, center.y),
                    end = Offset(
                        center.x,
                        center.y - size.minDimension / 2
                    ),
                    strokeWidth = 8f,
                    alpha = 1f,
                )
            }
            for (i in 0..270 step 90) {
                rotate(i.toFloat()) {
                    drawLine(
                        brush = SolidColor(Color.Black),
                        start = Offset(
                            center.x,
                            center.y - size.minDimension / 2
                        ),
                        end = Offset(
                            center.x,
                            center.y - size.minDimension / 2 + 50f
                        ),
                        strokeWidth = 4f,
                        alpha = 1f,
                    )
                }
            }

            drawText(
                textLayoutResult = measuredText,
                topLeft = Offset(
                    center.x - measuredText.size.width / 2,
                    center.y - size.minDimension / 4
                )
            )
            drawText(
                textLayoutResult = sixAmMeasuredText,
                topLeft = Offset(
                    center.x + size.minDimension / 2 - 50 - sixAmMeasuredText.size.width,
                    center.y - sixAmMeasuredText.size.height / 2
                )
            )
            drawText(
                textLayoutResult = noonMeasuredText,
                topLeft = Offset(
                    center.x - noonMeasuredText.size.width / 2,
                    center.y + size.minDimension / 2 - 50 - noonMeasuredText.size.height
                )
            )
            drawText(
                textLayoutResult = sixPmMeasuredText,
                topLeft = Offset(
                    center.x - size.minDimension / 2 + 50,
                    center.y - sixPmMeasuredText.size.height / 2
                )
            )
            drawText(
                textLayoutResult = midnightMeasuredText,
                topLeft = Offset(
                    center.x - midnightMeasuredText.size.width / 2,
                    center.y - size.minDimension / 2 + 50
                )
            )

            if (location.latitude != 0.0) {
                val zoneId = ZoneId.systemDefault()
                val today = LocalDate.now(zoneId)
                val epochSecondsAtStartOfDay =
                    today.atStartOfDay(zoneId).toEpochSecond()
                val instantOfStartOfDay = Instant.fromEpochMilliseconds(
                    epochSecondsAtStartOfDay * 1000
                )
                val solarState = instantOfStartOfDay.calculateSolarState(
                    location.latitude,
                    location.longitude
                )
                var up = if (solarState.altitude > 0) true else false
                var startSeconds = epochSecondsAtStartOfDay
                var size1 = Size(
                    size.minDimension - 40,
                    size.minDimension - 40
                )
                // sample the sun's elevation at five minute intervals for today
                // When we detect a sunset, draw an arc from the latest sunrise
                // (or start of day) to now.
                // If we get to the end of the day and the sun is still up
                // draw an arc from the latest sunrise to the end of the day!
                for (epochSeconds in epochSecondsAtStartOfDay..
                        epochSecondsAtStartOfDay + 24 * 3600 step 5 * 60) {
                    val currentInstant = Instant.fromEpochMilliseconds(
                        epochSeconds * 1000
                    )
                    val currentSolarState = currentInstant.calculateSolarState(
                        location.latitude,
                        location.longitude
                    )
                    if (up) {
                        if (currentSolarState.altitude < 0) {
                            val startAngle =
                                (startSeconds - epochSecondsAtStartOfDay) /
                                        60 * 360 / 1440
                            val endAngle =
                                (epochSeconds - epochSecondsAtStartOfDay) / 60 * 360 / 1440
                            drawArc(
                                brush = SolidColor(Color(255, 165, 0)),
                                startAngle = startAngle.toFloat() - 90,
                                sweepAngle = (endAngle - startAngle).toFloat(),
                                useCenter = false,
                                topLeft = Offset(
                                    center.x - size.minDimension / 2 + 20f,
                                    center.y - size.minDimension / 2 + 20f
                                ),
                                size = size1,
                                alpha = 0.3f,
                                style = Stroke(width = 32f),
                                colorFilter = ColorFilter.tint(Color(255, 165, 0)),
                                blendMode = BlendMode.Darken
                            )
                            up = false
                        }
                    } else {
                        if (currentSolarState.altitude > 0) {
                            startSeconds = epochSeconds
                            up = true
                        }
                    }
                }
                // if sun is still up at end of day
                // draw arc from latest sunrise to end of day
                if (up) {
                    val startAngle =
                        (startSeconds - epochSecondsAtStartOfDay) / 60 * 360 / 1440
                    drawArc(
                        brush = SolidColor(Color(255, 165, 0)),
                        startAngle = startAngle.toFloat() - 90,
                        sweepAngle = (360 - startAngle).toFloat(),
                        useCenter = false,
                        topLeft = Offset(
                            center.x - size.minDimension / 2 + 20,
                            center.y - size.minDimension / 2 + 20
                        ),
                        size = size1,
                        alpha = 0.3f,
                        style = Stroke(width = 32f),
                    )
                }

                val lunarPosition = instantOfStartOfDay.calculateLunarPosition(
                    location.latitude,
                    location.longitude
                )
                up = if (lunarPosition.altitude > 0) true else false
                val lunarIllumination =
                    instantOfStartOfDay.calculateLunarIllumination()
                val lunarStrokeWidth = lunarIllumination.fraction * 32
                startSeconds = epochSecondsAtStartOfDay

                size1 = Size(
                    size.minDimension - 120,
                    size.minDimension - 120
                )
                // sample the moon's elevation at five minute intervals for today
                // When we detect a moonset, draw an arc from the latest moonrise
                // (or start of day) to now.
                // If we get to the end of the day and the moon is still up
                // draw an arc from the latest moonrise to the end of the day!
                for (epochSeconds in epochSecondsAtStartOfDay..
                        epochSecondsAtStartOfDay + 24 * 3600 step 5 * 60) {
                    val i = Instant.fromEpochMilliseconds(
                        epochSeconds * 1000
                    )
                    val currentLunarPosition = i.calculateLunarPosition(
                        location.latitude,
                        location.longitude
                    )
                    if (up) {
                        if (currentLunarPosition.altitude < 0) {
                            val startAngle =
                                (startSeconds - epochSecondsAtStartOfDay) /
                                        60 * 360 / 1440
                            val endAngle =
                                (epochSeconds - epochSecondsAtStartOfDay) / 60 * 360 / 1440
                            drawArc(
                                brush = SolidColor(Color.Blue),
                                startAngle = startAngle.toFloat() - 90,
                                sweepAngle = (endAngle - startAngle).toFloat(),
                                useCenter = false,
                                topLeft = Offset(
                                    center.x - size.minDimension / 2 + 60f,
                                    center.y - size.minDimension / 2 + 60f
                                ),
                                size = size1,
                                alpha = 0.3f,
                                style = Stroke(width = lunarStrokeWidth.toFloat()),
                            )
                            up = false
                        }
                    } else {
                        if (currentLunarPosition.altitude > 0) {
                            startSeconds = epochSeconds
                            up = true
                        }
                    }
                }
                // if moon is still up at end of day
                // draw arc from latest moonrise to end of day
                if (up) {
                    val startAngle =
                        (startSeconds - epochSecondsAtStartOfDay) / 60 * 360 / 1440
                    drawArc(
                        brush = SolidColor(Color.Blue),
                        startAngle = startAngle.toFloat() - 90,
                        sweepAngle = (360 - startAngle).toFloat(),
                        useCenter = false,
                        topLeft = Offset(
                            center.x - size.minDimension / 2 + 60f,
                            center.y - size.minDimension / 2 + 60f
                        ),
                        size = size1,
                        alpha = 0.3f,
                        style = Stroke(width = lunarStrokeWidth.toFloat()),
                    )
                }
            }
        }
    }
}
