package com.carhost.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carhost.mobile.data.model.TrackPoint

@Composable
fun TrackBinaryCard(
    title: String,
    currentBinary: String,
    values: List<TrackPoint>,
    modifier: Modifier = Modifier,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val samples = values.takeLast(12)

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = labelColor,
                )
                Text(
                    text = currentBinary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (samples.isEmpty()) {
                Text(
                    text = "等待 track_bin 数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor,
                )
                return@Column
            }

            Text(
                text = "纵向：四路循迹通道    横向：时间采样",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
            )

            listOf("L1", "L2", "R1", "R2").forEachIndexed { bitIndex, channelLabel ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = channelLabel,
                        modifier = Modifier.width(28.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = labelColor,
                    )
                    samples.forEach { point ->
                        val enabled = point.binary.getOrNull(bitIndex) == '1'
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(18.dp)
                                .background(
                                    color = if (enabled) activeColor else inactiveColor,
                                    shape = RoundedCornerShape(6.dp),
                                ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(modifier = Modifier.width(34.dp))
                Text(
                    text = samples.first().timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
                Text(
                    text = "时间",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
                Text(
                    text = samples.last().timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
        }
    }
}
