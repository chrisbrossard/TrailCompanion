package com.chrisbrossard.trailcompanion.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.chrisbrossard.trailcompanion.R
import com.chrisbrossard.trailcompanion.viewmodel.VerticalSpeedViewModel

@Composable
fun VerticalSpeedScreen(
    //altitudeSlope: Double,
    verticalSpeedViewModel: VerticalSpeedViewModel
) {
    val verticalSpeed by verticalSpeedViewModel.verticalSpeed.collectAsState()

    Box {
        val style = TextStyle()
        val measuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(
                stringResource(
                    R.string.vertical_speed
                )
            ),
            style = style
        )
        val metersPerSecondMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString(
                stringResource(
                    R.string.meters_per_second
                )
            ),
            style = style
        )
        val zeroMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("0"),
            style = style
        )
        val minusTwoMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("-2"),
            style = style
        )
        val twoMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("+2"),
            style = style
        )
        val plusMinusFourMeasuredText = rememberTextMeasurer().measure(
            text = AnnotatedString("+-4"),
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
            for (i in 45..315 step 90) {
                rotate(i.toFloat()) {
                    drawLine(
                        brush = SolidColor(Color.Black),
                        start = Offset(
                            center.x,
                            center.y - size.minDimension / 2
                        ),
                        end = Offset(
                            center.x,
                            center.y - size.minDimension / 2 + 25f
                        ),
                        strokeWidth = 2f,
                        alpha = 1f,
                    )
                }
            }

            var angle = 0f

            if (verticalSpeed * 1000 >= -4 && verticalSpeed * 1000 <= 4) {
                angle = verticalSpeed * 1000 * 180 / 4
            }
            rotate(angle) {
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
            drawText(
                textLayoutResult = measuredText,
                topLeft = Offset(
                    center.x - measuredText.size.width / 2,
                    center.y + (size.minDimension / 2 - 50) / 2 -
                            measuredText.size.height
                )
            )
            drawText(
                textLayoutResult = metersPerSecondMeasuredText,
                topLeft = Offset(
                    center.x - metersPerSecondMeasuredText.size.width / 2,
                    center.y + (size.minDimension / 2 - 50) / 2
                )
            )
            drawText(
                textLayoutResult = zeroMeasuredText,
                topLeft = Offset(
                    center.x - zeroMeasuredText.size.width / 2,
                    center.y - size.minDimension / 2 + 50
                )
            )
            drawText(
                textLayoutResult = minusTwoMeasuredText,
                topLeft = Offset(
                    center.x - size.minDimension / 2 + 50,
                    center.y - minusTwoMeasuredText.size.height / 2
                )
            )
            drawText(
                textLayoutResult = twoMeasuredText,
                topLeft = Offset(
                    center.x + size.minDimension / 2 - 50 - twoMeasuredText.size.width,
                    center.y - twoMeasuredText.size.height / 2
                )
            )
            drawText(
                textLayoutResult = plusMinusFourMeasuredText,
                topLeft = Offset(
                    center.x - plusMinusFourMeasuredText.size.width / 2,
                    center.y + size.minDimension / 2 - 50 -
                            plusMinusFourMeasuredText.size.height
                )
            )
        }
    }
}
