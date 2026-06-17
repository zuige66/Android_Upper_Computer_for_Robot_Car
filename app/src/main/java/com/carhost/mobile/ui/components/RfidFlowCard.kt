package com.carhost.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RfidFlowCard(
    title: String,
    currentLabel: String,
    locations: List<String>,
    modifier: Modifier = Modifier,
) {
    val activeColor = MaterialTheme.colorScheme.primaryContainer
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val activeTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val arrowColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val displayLocations = locations.filter { it.isNotBlank() && it.lowercase() != "unknown" }

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
                    text = currentLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (displayLocations.isEmpty()) {
                Text(
                    text = "等待 RFID 位置数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor,
                )
                return@Column
            }

            Text(
                text = "共 ${displayLocations.size} 个位置节点",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
            )

            val scrollState = rememberScrollState()
            LaunchedEffect(displayLocations.size) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                displayLocations.forEachIndexed { index, location ->
                    val isCurrent = location == currentLabel
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(48.dp)
                            .background(
                                color = if (isCurrent) activeColor else inactiveColor,
                                shape = RoundedCornerShape(10.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = location,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) activeTextColor else inactiveTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp),
                        )
                    }

                    if (index < displayLocations.lastIndex) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = arrowColor,
                            modifier = Modifier
                                .width(20.dp)
                                .height(16.dp),
                        )
                    }
                }
            }
        }
    }
}
