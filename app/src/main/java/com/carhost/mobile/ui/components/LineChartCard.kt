package com.carhost.mobile.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carhost.mobile.data.model.ChartPoint
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun LineChartCard(
    title: String,
    valueText: String,
    values: List<ChartPoint>,
    yAxisLabel: String,
    xAxisLabel: String = "时间",
    valueFormatter: (Float) -> String = { "%.1f".format(it) },
    yRange: ClosedFloatingPointRange<Float>? = null,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = axisColor,
                )
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Y-axis label (fixed)
            Text(
                text = yAxisLabel,
                style = MaterialTheme.typography.labelSmall,
                color = axisColor,
                fontWeight = FontWeight.Bold,
            )

            // Row: fixed Y-axis + scrollable chart
            Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                // Fixed Y-axis area (never scrolls)
                Canvas(
                    modifier = Modifier
                        .width(52.dp)
                        .height(190.dp),
                ) {
                    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = axisColor.toArgb()
                        textSize = 10.sp.toPx()
                    }
                    val topPadding = 8.dp.toPx()
                    val bottomPadding = 24.dp.toPx()
                    val chartBottom = size.height - bottomPadding
                    val chartHeight = chartBottom - topPadding

                    // Y-axis line
                    drawLine(
                        color = outlineColor,
                        start = Offset(size.width, topPadding),
                        end = Offset(size.width, chartBottom),
                        strokeWidth = 1f,
                    )
                    // Upward arrow
                    drawAxisArrow(size.width, topPadding, isVertical = true, color = axisColor)

                    // Tick labels and grid hints
                    val minValue = yRange?.start ?: if (values.isNotEmpty()) floor(values.minOf { it.value }) else 0f
                    val maxValue = if (yRange != null) {
                        yRange.endInclusive
                    } else if (values.isNotEmpty()) {
                        ceil(values.maxOf { it.value })
                    } else {
                        1f
                    }.takeIf { it > minValue } ?: (minValue + 1f)
                    val range = maxValue - minValue

                    for (step in 0..4) {
                        val fraction = step / 4f
                        val y = chartBottom - chartHeight * fraction
                        val tickValue = minValue + range * fraction
                        drawContext.canvas.nativeCanvas.drawText(
                            valueFormatter(tickValue),
                            4.dp.toPx(),
                            y + 3.5.dp.toPx(),
                            labelPaint,
                        )
                    }
                }

                // Scrollable chart area
                val scrollState = rememberScrollState()
                LaunchedEffect(values.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState),
                ) {
                    val chartWidth = (values.size * 36).coerceAtLeast(280)
                    Canvas(
                        modifier = Modifier
                            .width(chartWidth.dp)
                            .height(190.dp),
                    ) {
                        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = axisColor.toArgb()
                            textSize = 10.sp.toPx()
                        }

                        val leftPadding = 4.dp.toPx()
                        val rightPadding = 12.dp.toPx()
                        val topPadding = 8.dp.toPx()
                        val bottomPadding = 24.dp.toPx()
                        val chartLeft = leftPadding
                        val chartRight = size.width - rightPadding
                        val chartBottom = size.height - bottomPadding
                        val chartHeight = chartBottom - topPadding
                        val drawableWidth = chartRight - chartLeft

                        if (values.size < 2 || drawableWidth <= 0f || chartHeight <= 0f) {
                            // No data: draw bare axis lines with arrows
                            drawLine(
                                color = outlineColor,
                                start = Offset(chartLeft, chartBottom),
                                end = Offset(chartRight, chartBottom),
                                strokeWidth = 1f,
                            )
                            drawAxisArrow(chartRight, chartBottom, isVertical = false, color = axisColor)
                            return@Canvas
                        }

                        val minValue = yRange?.start ?: floor(values.minOf { it.value })
                        val maxValue = (yRange?.endInclusive ?: ceil(values.maxOf { it.value }))
                            .takeIf { it > minValue } ?: (minValue + 1f)
                        val range = maxValue - minValue

                        // Horizontal grid lines
                        for (step in 0..4) {
                            val fraction = step / 4f
                            val y = chartBottom - chartHeight * fraction
                            drawLine(
                                color = outlineColor,
                                start = Offset(chartLeft, y),
                                end = Offset(chartRight, y),
                                strokeWidth = 1.5f,
                            )
                        }

                        // Data line
                        val path = Path()
                        values.forEachIndexed { index, point ->
                            val x = chartLeft + drawableWidth * index / values.lastIndex.coerceAtLeast(1)
                            val normalized = ((point.value - minValue) / range).coerceIn(0f, 1f)
                            val y = chartBottom - normalized * chartHeight

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 5f, cap = StrokeCap.Round),
                        )

                        // X-axis line
                        drawLine(
                            color = outlineColor,
                            start = Offset(chartLeft, chartBottom),
                            end = Offset(chartRight, chartBottom),
                            strokeWidth = 1f,
                        )
                        // Rightward arrow
                        drawAxisArrow(chartRight, chartBottom, isVertical = false, color = axisColor)

                        // X-axis time labels
                        val timeIndexes = listOf(0, values.lastIndex / 2, values.lastIndex).distinct()
                        timeIndexes.forEach { index ->
                            val point = values[index]
                            val x = chartLeft + drawableWidth * index / values.lastIndex.coerceAtLeast(1)
                            drawLine(
                                color = outlineColor,
                                start = Offset(x, chartBottom),
                                end = Offset(x, chartBottom + 4.dp.toPx()),
                                strokeWidth = 1f,
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                point.timeLabel,
                                (x - 22.dp.toPx()).coerceIn(0f, size.width - 46.dp.toPx()),
                                size.height - 8.dp.toPx(),
                                labelPaint,
                            )
                        }
                    }
                }
            }

            // X-axis label (fixed below chart)
            Text(
                text = xAxisLabel,
                style = MaterialTheme.typography.labelSmall,
                color = axisColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun DrawScope.drawAxisArrow(
    x: Float,
    y: Float,
    isVertical: Boolean,
    color: Color,
) {
    val arrowLen = 8.dp.toPx()
    val arrowWidth = 4.dp.toPx()
    if (isVertical) {
        // Upward arrow at (x, y)
        drawLine(color, Offset(x, y), Offset(x - arrowWidth, y + arrowLen), strokeWidth = 2f)
        drawLine(color, Offset(x, y), Offset(x + arrowWidth, y + arrowLen), strokeWidth = 2f)
    } else {
        // Rightward arrow at (x, y)
        drawLine(color, Offset(x, y), Offset(x - arrowLen, y - arrowWidth), strokeWidth = 2f)
        drawLine(color, Offset(x, y), Offset(x - arrowLen, y + arrowWidth), strokeWidth = 2f)
    }
}
