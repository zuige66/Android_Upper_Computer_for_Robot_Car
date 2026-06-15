package com.carhost.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LineChartCard(
    title: String,
    valueText: String,
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val lineColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (values.size < 2) {
                        return@Canvas
                    }

                    val minValue = values.minOrNull() ?: 0f
                    val maxValue = values.maxOrNull() ?: 1f
                    val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f

                    for (step in 1..3) {
                        val y = size.height * step / 4f
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                        )
                    }

                    val path = Path()
                    values.forEachIndexed { index, value ->
                        val x = size.width * index / (values.lastIndex.coerceAtLeast(1))
                        val normalized = (value - minValue) / range
                        val y = size.height - (normalized * size.height)

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
                }
            }
        }
    }
}
