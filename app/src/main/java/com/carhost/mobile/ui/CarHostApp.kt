package com.carhost.mobile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carhost.mobile.data.model.AlertLevel
import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.ColorTheme
import com.carhost.mobile.data.model.ChartPoint
import com.carhost.mobile.data.model.CommandEmphasis
import com.carhost.mobile.data.model.CustomChartDef
import com.carhost.mobile.data.model.QuickButtonDef
import com.carhost.mobile.data.model.CustomChartType
import com.carhost.mobile.data.model.CustomCommandDef
import com.carhost.mobile.ui.BuiltInChartDef
import com.carhost.mobile.ui.BuiltInChartState
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.model.LogEntry
import com.carhost.mobile.data.model.ReturnReason
import com.carhost.mobile.data.model.TelemetrySnapshot
import com.carhost.mobile.data.model.TrackPoint
import com.carhost.mobile.data.model.VehicleCommand
import com.carhost.mobile.data.model.VehicleState
import com.carhost.mobile.ui.components.LineChartCard
import com.carhost.mobile.ui.components.RfidFlowCard
import com.carhost.mobile.ui.components.TrackBinaryCard
import com.carhost.mobile.ui.theme.CarHostTheme
import com.carhost.mobile.ui.theme.themeSwatches

@Composable
fun CarHostApp(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CarHostTheme(
        useDynamicColor = uiState.preferences.useDynamicColor,
        colorTheme = uiState.preferences.colorTheme,
        contrastLevel = uiState.preferences.contrastLevel,
    ) {
        KeepScreenOnEffect(enabled = uiState.preferences.keepScreenOn)
        CarHostScaffold(
            uiState = uiState,
            onIntent = viewModel::dispatch,
        )
    }
}

@Composable
private fun KeepScreenOnEffect(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled, view) {
        view.keepScreenOn = enabled
        onDispose {
            view.keepScreenOn = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarHostScaffold(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text("Bot Host")
                },
                actions = {
                    val linkText = when (uiState.telemetry.linkState) {
                        LinkState.Online,
                        LinkState.Connected -> "已连接"
                        LinkState.Connecting -> "连接中"
                        LinkState.Fault -> "链路故障"
                        LinkState.Offline -> "离线"
                    }
                    val linkColor = when (uiState.telemetry.linkState) {
                        LinkState.Fault -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                    val linkTextColor = when (uiState.telemetry.linkState) {
                        LinkState.Fault -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(linkColor)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = linkText,
                            style = MaterialTheme.typography.labelMedium,
                            color = linkTextColor,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding().height(96.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
            ) {
                tabItems().forEach { item ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == item.tab,
                        onClick = { onIntent(MainIntent.SelectTab(item.tab)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.tab.label,
                            )
                        },
                        label = { Text(item.tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                when (uiState.selectedTab) {
                    AppTab.Overview -> OverviewScreen(uiState = uiState, onIntent = onIntent)
                    AppTab.Monitor -> MonitorScreen(uiState = uiState, onIntent = onIntent)
                    AppTab.Control -> ControlScreen(uiState = uiState, onIntent = onIntent)
                    AppTab.History -> HistoryScreen(uiState = uiState, onIntent = onIntent)
                    AppTab.Settings -> SettingsScreen(uiState = uiState, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun OverviewScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItemId by remember { mutableStateOf("") }
    var editingTitle by remember { mutableStateOf("") }
    var editingFieldPath by remember { mutableStateOf("") }

    val overviewItems = listOf(
        "vehicle_state" to Triple("车辆状态", uiState.telemetry.vehicleState.label, "当前工位 ${uiState.telemetry.positionLabel} · ${uiState.telemetry.locationDescription}"),
        "link_state" to Triple("连接链路", uiState.telemetry.linkState.label, "TCP Socket 当前连接状态"),
        "rfid_tag" to Triple("RFID 点位", uiState.telemetry.rfidTag, "最近一次识别：${uiState.telemetry.locationDescription}"),
        "battery" to Triple("电池余量", "${uiState.telemetry.batteryPercent}%", "返航原因：${uiState.telemetry.returnReason.label}"),
        "environment" to Triple("环境概况", "${uiState.telemetry.temperatureC.toInt()}°C / ${(uiState.telemetry.gasPercent * 100).toInt()}%", "红外目标温度与 MQ8 原始值换算结果"),
        "obstacle" to Triple("防撞状态", if (uiState.telemetry.obstacleDetected) "已触发" else "安全", "前方距离 ${uiState.telemetry.obstacleDistanceCm} cm，仅在巡检/撤离时启用"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AnimatedVisibility(visible = uiState.activeAlert != AlertLevel.Normal) {
                AlertBanner(uiState = uiState)
            }
        }

        // Action bar: restore + clear (capsule style)
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .clickable { onIntent(MainIntent.RestoreDefaultOverview) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("恢复", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .clickable { onIntent(MainIntent.ResetOverviewData) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            FullOverviewCard(
                uiState = uiState,
                onItemClick = { itemId, title, fieldPath ->
                    editingItemId = itemId
                    editingTitle = title
                    editingFieldPath = fieldPath
                    showEditDialog = true
                },
                onItemDelete = { onIntent(MainIntent.DeleteOverviewItem(it)) },
            )
        }

        // Overview metric items
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                overviewItems.forEach { (itemId, triple) ->
                    val (defaultTitle, value, supporting) = triple
                    val state = uiState.overviewItemStates[itemId]
                    if (state == null || !state.deleted) {
                        val displayTitle = state?.customTitle ?: defaultTitle
                        EditableMetricRow(
                            title = displayTitle,
                            value = value,
                            supporting = supporting,
                            onEdit = {
                                editingItemId = itemId
                                editingTitle = displayTitle
                                editingFieldPath = state?.customFieldPath ?: defaultOverviewFieldPath(itemId)
                                showEditDialog = true
                            },
                            onDelete = { onIntent(MainIntent.DeleteOverviewItem(itemId)) },
                        )
                    }
                }
                val hardcodedIds = overviewItems.map { it.first }.toSet()
                uiState.overviewItemStates.forEach { (itemId, state) ->
                    if (itemId !in hardcodedIds && !state.deleted) {
                        val displayTitle = state.customTitle ?: itemId
                        EditableMetricRow(
                            title = displayTitle,
                            value = "自定义",
                            supporting = "自定义项",
                            onEdit = {
                                editingItemId = itemId
                                editingTitle = displayTitle
                                editingFieldPath = state.customFieldPath ?: itemId
                                showEditDialog = true
                            },
                            onDelete = { onIntent(MainIntent.DeleteOverviewItem(itemId)) },
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑标题") },
            text = {
                OutlinedTextField(
                    value = editingTitle,
                    onValueChange = { editingTitle = it },
                    label = { Text("显示名称") },
                    singleLine = true,
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        onIntent(MainIntent.DeleteOverviewItem(editingItemId))
                        showEditDialog = false
                    }) { Text("删除", color = Color.White, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = {
                        onIntent(MainIntent.EditOverviewItem(editingItemId, editingTitle, editingFieldPath))
                        showEditDialog = false
                    }) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消", color = Color.White.copy(alpha = 0.7f)) }
            },
        )
    }
}

@Composable
private fun MonitorScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var editingCustomChart by remember { mutableStateOf<CustomChartDef?>(null) }
    var customName by remember { mutableStateOf("") }
    var customFieldPath by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("") }
    var customType by remember { mutableStateOf(CustomChartType.Line) }

    var showBuiltInDialog by remember { mutableStateOf(false) }
    var editingBuiltInId by remember { mutableStateOf("") }
    var builtInName by remember { mutableStateOf("") }
    var builtInFieldPath by remember { mutableStateOf("") }

    fun openAddCustomDialog() {
        editingCustomChart = null
        customName = ""
        customFieldPath = ""
        customUnit = ""
        customType = CustomChartType.Line
        showCustomDialog = true
    }

    fun openEditCustomDialog(chart: CustomChartDef) {
        editingCustomChart = chart
        customName = chart.name
        customFieldPath = chart.fieldPath
        customUnit = chart.unit
        customType = chart.type
        showCustomDialog = true
    }

    fun openEditBuiltInDialog(chart: BuiltInChartDef) {
        editingBuiltInId = chart.id
        builtInName = chart.defaultName
        builtInFieldPath = chart.defaultFieldPath
        showBuiltInDialog = true
    }

    val hasAnyHistory = uiState.telemetry.mlxObjectHistory.isNotEmpty() ||
        uiState.telemetry.ahtTemperatureHistory.isNotEmpty() ||
        uiState.telemetry.customChartHistory.isNotEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Unified action bar: restore + clear + add (capsule style)
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .clickable { onIntent(MainIntent.RestoreDefaultCharts) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("恢复", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .clickable { onIntent(MainIntent.ClearMonitorData) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .clickable { openAddCustomDialog() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("自定义", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }
        }

        // Custom charts
        items(uiState.customCharts.size, key = { "custom_${uiState.customCharts[it].id}" }) { index ->
            val chart = uiState.customCharts[index]
            val history = uiState.telemetry.customChartHistory[chart.id] ?: emptyList()
            val currentValue = history.lastOrNull()?.value
            GrayChartCard(
                title = chart.name,
                subtitle = "字段: ${chart.fieldPath}" + if (chart.unit.isNotBlank()) "  单位: ${chart.unit}" else "",
                onEdit = { openEditCustomDialog(chart) },
                onDelete = { onIntent(MainIntent.DeleteCustomChart(chart.id)) },
            ) {
                LineChartCard(
                    title = chart.name,
                    valueText = currentValue?.let { "%.2f%s".format(it, chart.unit) } ?: "--",
                    values = history,
                    yAxisLabel = chart.unit.ifBlank { chart.fieldPath },
                )
            }
        }

        // Built-in charts (from data-driven list, skip deleted)
        items(
            uiState.builtInCharts.filter { chart ->
                val state = uiState.builtInChartStates[chart.id] ?: BuiltInChartState()
                !state.deleted
            }.size,
            key = { index ->
                val visibleCharts = uiState.builtInCharts.filter { chart ->
                    val state = uiState.builtInChartStates[chart.id] ?: BuiltInChartState()
                    !state.deleted
                }
                "builtin_${visibleCharts[index].id}"
            },
        ) { index ->
            val visibleCharts = uiState.builtInCharts.filter { chart ->
                val state = uiState.builtInChartStates[chart.id] ?: BuiltInChartState()
                !state.deleted
            }
            val chart = visibleCharts[index]
            val state = uiState.builtInChartStates[chart.id] ?: BuiltInChartState()

            if (state.visible) {
                val chartContent = @Composable {
                    when (chart.id) {
                        "mlx_obj" -> LineChartCard(
                            title = chart.defaultName,
                            valueText = "%.1f°C".format(uiState.telemetry.mlxObjectTemperatureC),
                            values = uiState.telemetry.mlxObjectHistory,
                            yAxisLabel = chart.defaultFieldPath,
                        )
                        "aht_temp" -> LineChartCard(
                            title = chart.defaultName,
                            valueText = "%.1f°C".format(uiState.telemetry.ahtTemperatureC),
                            values = uiState.telemetry.ahtTemperatureHistory,
                            yAxisLabel = chart.defaultFieldPath,
                        )
                        "aht_hum" -> LineChartCard(
                            title = chart.defaultName,
                            valueText = "%.1f%%".format(uiState.telemetry.ahtHumidityPercent),
                            values = uiState.telemetry.ahtHumidityHistory,
                            yAxisLabel = chart.defaultFieldPath,
                        )
                        "mq8" -> LineChartCard(
                            title = chart.defaultName,
                            valueText = uiState.telemetry.mq8Raw.toInt().toString(),
                            values = uiState.telemetry.mq8History,
                            yAxisLabel = chart.defaultFieldPath,
                            valueFormatter = { it.toInt().toString() },
                            yRange = 0f..4095f,
                        )
                        "track" -> TrackBinaryCard(
                            title = chart.defaultName,
                            currentBinary = uiState.telemetry.trackBinary,
                            values = uiState.telemetry.trackHistory,
                        )
                        "rfid_flow" -> RfidFlowCard(
                            currentLabel = uiState.telemetry.positionLabel,
                            locations = uiState.telemetry.rfidHistory,
                        )
                    }
                }
                GrayChartCard(
                    title = chart.defaultName,
                    subtitle = chart.subtitle,
                    onEdit = { openEditBuiltInDialog(chart) },
                    onHide = { onIntent(MainIntent.ToggleBuiltInChartVisible(chart.id)) },
                    onDelete = { onIntent(MainIntent.DeleteBuiltInChart(chart.id)) },
                ) {
                    chartContent()
                }
            } else {
                // Hidden chart: collapsed row with restore button
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = chart.defaultName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        IconButton(onClick = { onIntent(MainIntent.ToggleBuiltInChartVisible(chart.id)) }) {
                            Icon(
                                Icons.Outlined.VisibilityOff,
                                contentDescription = "恢复显示",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Metric rows
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricRow("车速", "${uiState.telemetry.speedMetersPerSecond} m/s", "当前为状态推导值，不是编码器实测值")
                MetricRow("当前点位", uiState.telemetry.positionLabel, uiState.telemetry.locationDescription)
                MetricRow("RFID 标签", uiState.telemetry.rfidTag, "可直接映射工位、路口和告警位置信息")
                MetricRow(
                    "避障测距",
                    "${uiState.telemetry.obstacleDistanceCm} cm",
                    if (uiState.telemetry.obstacleDetected) "已触发避障停车" else "前方净空正常",
                )
                MetricRow("MLX 环境温度", "%.1f°C".format(uiState.telemetry.mlxAmbientTemperatureC), "MLX90614 传感器自身环境温度")
                MetricRow("循迹数值", "${uiState.telemetry.trackValue} / ${uiState.telemetry.trackBinary}", "四路循迹 bit 合成结果")
                MetricRow(
                    "返航状态",
                    uiState.telemetry.returnReason.label,
                    if (uiState.telemetry.homeDockReached) "已回到起点 / 充电桩" else "尚未回桩",
                )
                MetricRow("链路延迟", "${uiState.telemetry.latencyMs} ms", "当前未做真实 RTT 测量，显示值仅为占位")
            }
        }
    }

    // Custom chart editor dialog
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text(if (editingCustomChart == null) "新建自定义图表" else "编辑图表") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("图表名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = customFieldPath,
                        onValueChange = { customFieldPath = it },
                        label = { Text("JSON 字段路径") },
                        placeholder = { Text("例如: AHT_temp 或 data.sensors[0].value") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = customUnit,
                        onValueChange = { customUnit = it },
                        label = { Text("单位（可选）") },
                        placeholder = { Text("例如: °C, %, ADC") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Text("图表类型", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = customType == CustomChartType.Line, onClick = { customType = CustomChartType.Line }, label = { Text("折线图") })
                        FilterChip(selected = customType == CustomChartType.Bar, onClick = { customType = CustomChartType.Bar }, label = { Text("柱状图") })
                    }
                    Text(
                        text = "JSON 字段路径说明：直接填字段名如 AHT_temp，嵌套用点号如 data.temp，数组用 [0] 如 sensors[0].value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val chart = CustomChartDef(
                        id = editingCustomChart?.id ?: java.util.UUID.randomUUID().toString(),
                        name = customName.trim(),
                        fieldPath = customFieldPath.trim(),
                        unit = customUnit.trim(),
                        type = customType,
                    )
                    if (chart.name.isNotBlank() && chart.fieldPath.isNotBlank()) {
                        if (editingCustomChart == null) onIntent(MainIntent.AddCustomChart(chart))
                        else onIntent(MainIntent.UpdateCustomChart(chart))
                        showCustomDialog = false
                    }
                }) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("取消", color = Color.White.copy(alpha = 0.7f)) }
            },
        )
    }

    // Built-in chart editor dialog
    if (showBuiltInDialog) {
        AlertDialog(
            onDismissRequest = { showBuiltInDialog = false },
            title = { Text("编辑内置图表") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = builtInName,
                        onValueChange = { builtInName = it },
                        label = { Text("图表名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = builtInFieldPath,
                        onValueChange = { builtInFieldPath = it },
                        label = { Text("JSON 字段路径") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Text(
                        text = "修改字段路径可以改变此图表读取的 JSON 字段",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        onIntent(MainIntent.DeleteBuiltInChart(editingBuiltInId))
                        showBuiltInDialog = false
                    }) { Text("删除", color = Color.White, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = {
                        onIntent(MainIntent.UpdateBuiltInChart(editingBuiltInId, builtInName.trim(), builtInFieldPath.trim()))
                        showBuiltInDialog = false
                    }) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuiltInDialog = false }) { Text("取消", color = Color.White.copy(alpha = 0.7f)) }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ControlScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editingCommandId by remember { mutableStateOf("") }
    var editLabel by remember { mutableStateOf("") }
    var editWireValue by remember { mutableStateOf("") }

    fun openEditDefault(command: VehicleCommand) {
        editingCommandId = command.name
        val custom = uiState.customCommands.find { it.commandId == command.name }
        editLabel = custom?.customLabel?.ifBlank { null } ?: command.label
        editWireValue = custom?.customWireValue?.ifBlank { null } ?: command.wireValue
        showEditDialog = true
    }

    val defaultButtons = uiState.availableCommands.map { cmd ->
        val custom = uiState.customCommands.find { it.commandId == cmd.name }
        Triple(
            custom?.customLabel?.ifBlank { null } ?: cmd.label,
            cmd.emphasis,
            cmd.name,
        )
    }
    // Add quick buttons added via "+"
    val customCommandNames = uiState.availableCommands.map { it.name }.toSet()
    val quickButtonItems = uiState.quickButtons
        .filter { it.id !in customCommandNames }
        .map { btn -> Triple(btn.label, CommandEmphasis.Neutral, btn.id) }
    val allButtons = defaultButtons + quickButtonItems
    val defaultRows = allButtons.chunked(2)
    var quickSendText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "连接配置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = uiState.endpointDraftHost,
                        onValueChange = { onIntent(MainIntent.UpdateHostDraft(it)) },
                        label = { Text("小车 IP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.endpointDraftPort,
                        onValueChange = { onIntent(MainIntent.UpdatePortDraft(it)) },
                        label = { Text("端口") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                .clickable { onIntent(MainIntent.SaveEndpoint) }
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("保存参数", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                .clickable { onIntent(MainIntent.ToggleConnection) }
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(if (uiState.connectionActive) "断开连接" else "开始连接", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "快速控制",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(onClick = { onIntent(MainIntent.RestoreDefaultCommands) }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "恢复默认")
                        }
                    }

                    Text(
                        text = "长按按钮可编辑或删除",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // 2-column grid
                    defaultRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { (label, emphasis, id) ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CommandChip(
                                        label = label,
                                        emphasis = emphasis,
                                        onClick = {
                                            val cmd = uiState.availableCommands.find { it.name == id }
                                            if (cmd != null) {
                                                onIntent(MainIntent.SendCommand(cmd))
                                            } else {
                                                // Quick button - send wireValue as raw command
                                                val quickBtn = uiState.quickButtons.find { it.id == id }
                                                if (quickBtn != null) {
                                                    onIntent(MainIntent.SendRawCommand(quickBtn.wireValue))
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            val cmd = uiState.availableCommands.find { it.name == id }
                                            if (cmd != null) {
                                                openEditDefault(cmd)
                                            } else {
                                                // Quick button - open edit dialog
                                                val quickBtn = uiState.quickButtons.find { it.id == id }
                                                if (quickBtn != null) {
                                                    editingCommandId = quickBtn.id
                                                    editLabel = quickBtn.label
                                                    editWireValue = quickBtn.wireValue
                                                    showEditDialog = true
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                            // Fill empty space if odd number
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Text(
                        text = "快捷发送",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = quickSendText,
                            onValueChange = { quickSendText = it },
                            label = { Text("输入要发送的内容") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        val sendEnabled = quickSendText.isNotBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, if (sendEnabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
                                .clickable(enabled = sendEnabled) {
                                    onIntent(MainIntent.SendRawCommand(quickSendText.trim()))
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "发送", modifier = Modifier.size(18.dp), tint = if (sendEnabled) Color.White else Color.White.copy(alpha = 0.38f))
                        }
                    }
                }
            }
        }
    }

    // Edit command dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑按钮") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editLabel,
                        onValueChange = { editLabel = it },
                        label = { Text("按钮名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editWireValue,
                        onValueChange = { editWireValue = it },
                        label = { Text("发送字段") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    val cmd = uiState.availableCommands.find { it.name == editingCommandId }
                    if (cmd != null) {
                        Text(
                            text = "默认: ${cmd.label} / ${cmd.wireValue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val isQuickButton = uiState.quickButtons.any { it.id == editingCommandId }
                        if (isQuickButton) {
                            onIntent(MainIntent.DeleteQuickButton(editingCommandId))
                        } else {
                            onIntent(MainIntent.UpdateCustomCommand(
                                CustomCommandDef(
                                    commandId = editingCommandId,
                                    customLabel = "",
                                    customWireValue = "",
                                )
                            ))
                        }
                        showEditDialog = false
                    }) { Text("删除", color = Color.White, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = {
                        val isQuickButton = uiState.quickButtons.any { it.id == editingCommandId }
                        if (isQuickButton) {
                            onIntent(MainIntent.UpdateQuickButton(
                                QuickButtonDef(
                                    id = editingCommandId,
                                    label = editLabel.trim(),
                                    wireValue = editWireValue.trim(),
                                )
                            ))
                        } else {
                            onIntent(MainIntent.UpdateCustomCommand(
                                CustomCommandDef(
                                    commandId = editingCommandId,
                                    customLabel = editLabel.trim(),
                                    customWireValue = editWireValue.trim(),
                                )
                            ))
                        }
                        showEditDialog = false
                    }) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消", color = Color.White.copy(alpha = 0.7f)) }
            },
        )
    }

}

@Composable
private fun HistoryScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "历史数据",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "包含历史连接、接收 RX、发送 CMD、ACK、ERR 和告警记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    val clearEnabled = uiState.logs.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, if (clearEnabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
                            .clickable(enabled = clearEnabled) { onIntent(MainIntent.ClearHistory) }
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("清除所有数据", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (clearEnabled) Color.White else Color.White.copy(alpha = 0.38f))
                    }
                }
            }
        }

        if (uiState.logs.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Text(
                        text = "暂无历史数据",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(uiState.logs, key = { it.id }) { entry ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = entry.level,
                            style = MaterialTheme.typography.labelLarge,
                            color = levelColor(entry.level),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = entry.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = entry.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
    var customSendText by remember { mutableStateOf("") }
    var defaultEditTitle by remember { mutableStateOf("") }
    var defaultEditName by remember { mutableStateOf("") }
    var defaultEditJson by remember { mutableStateOf("") }
    var defaultEditKind by remember { mutableStateOf("") }
    var defaultEditId by remember { mutableStateOf("") }

    fun openDefaultEdit(kind: String, id: String, title: String, name: String, json: String) {
        defaultEditKind = kind
        defaultEditId = id
        defaultEditTitle = title
        defaultEditName = name
        defaultEditJson = json
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingToggle(
                title = "动态取色",
                description = "Android 12+ 跟随系统壁纸主题",
                checked = uiState.preferences.useDynamicColor,
                onCheckedChange = { onIntent(MainIntent.SetDynamicColor(it)) },
            )
        }
        item {
            SettingToggle(
                title = "巡检页常亮",
                description = "长时间值守时避免熄屏",
                checked = uiState.preferences.keepScreenOn,
                onCheckedChange = { onIntent(MainIntent.SetKeepScreenOn(it)) },
            )
        }
        item {
            SettingToggle(
                title = "报警通知",
                description = "收到 alert 报文时给手机发送系统通知",
                checked = uiState.preferences.notificationsEnabled,
                onCheckedChange = { onIntent(MainIntent.SetNotifications(it)) },
            )
        }

        // Color theme picker
        item {
            ColorThemeSection(
                currentTheme = uiState.preferences.colorTheme,
                vibrancyLevel = uiState.preferences.contrastLevel,
                onThemeSelected = { onIntent(MainIntent.SetColorTheme(it)) },
                onVibrancyChanged = { onIntent(MainIntent.SetContrastLevel(it)) },
            )
        }

        // JSON viewer section
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "JSON 数据格式",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "查看最近一次接收和发送的 JSON 数据，了解数据格式后可在监控页自定义图表",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Received JSON
                    Text(
                        text = "最近接收 (RX)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Text(
                            text = uiState.telemetry.lastReceivedJson.ifBlank { "暂无数据" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        )
                    }

                    // Sent JSON
                    Text(
                        text = "最近发送 (TX)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Text(
                            text = uiState.telemetry.lastSentJson.ifBlank { "暂无数据" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        )
                    }

                    // Custom send
                    Text(
                        text = "自定义发送",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = customSendText,
                            onValueChange = { customSendText = it },
                            label = { Text("输入要发送的内容") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        val customSendEnabled = customSendText.isNotBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, if (customSendEnabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
                                .clickable(enabled = customSendEnabled) {
                                    onIntent(MainIntent.SendRawCommand(customSendText.trim()))
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "发送", modifier = Modifier.size(18.dp), tint = if (customSendEnabled) Color.White else Color.White.copy(alpha = 0.38f))
                        }
                    }
                }
            }
        }


        item {
            DefaultSettingsCard(
                uiState = uiState,
                onEditOverview = { id, name, json -> openDefaultEdit("overview", id, "编辑默认总览", name, json) },
                onEditChart = { id, name, json -> openDefaultEdit("chart", id, "编辑默认图表", name, json) },
                onEditCommand = { id, name, json -> openDefaultEdit("command", id, "编辑默认命令", name, json) },
                onDeleteOverview = { onIntent(MainIntent.DeleteOverviewItem(it, saveAsDefault = true)) },
                onDeleteChart = { chartId ->
                    val isBuiltIn = uiState.builtInCharts.any { it.id == chartId }
                    if (isBuiltIn) {
                        onIntent(MainIntent.DeleteBuiltInChart(chartId, saveAsDefault = true))
                    } else {
                        onIntent(MainIntent.DeleteCustomChart(chartId))
                    }
                },
                onDeleteCommand = { commandId ->
                    val isQuickButton = uiState.quickButtons.any { it.id == commandId }
                    if (isQuickButton) {
                        onIntent(MainIntent.DeleteQuickButton(commandId, saveAsDefault = true))
                    } else {
                        onIntent(MainIntent.UpdateCustomCommand(CustomCommandDef(commandId, "", ""), saveAsDefault = true))
                    }
                },
                onAddOverview = { openDefaultEdit("overview", "", "添加默认总览项", "", "") },
                onAddChart = { openDefaultEdit("chart", "", "添加默认图表", "", "") },
                onAddCommand = { openDefaultEdit("command", "", "添加默认按钮", "", "") },
            )
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "当前架构说明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "动态取色在 Android 12 及以上会读取系统 Material You 颜色，因此如果系统壁纸参与取色，App 颜色会随之变化；低版本则回退到内置配色。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "报警通知开关打开后，收到 alert 报文会给手机发系统通知；Android 13+ 还需要系统允许通知权限。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "版本 v1.0",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }

    if (defaultEditKind.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { defaultEditKind = "" },
            title = { Text(defaultEditTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = defaultEditName,
                        onValueChange = { defaultEditName = it },
                        label = { Text("名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = defaultEditJson,
                        onValueChange = { defaultEditJson = it },
                        label = { Text("字段路径 JSON") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        if (defaultEditId.isNotEmpty()) {
                            when (defaultEditKind) {
                                "overview" -> onIntent(MainIntent.DeleteOverviewItem(defaultEditId, saveAsDefault = true))
                                "chart" -> {
                                    val isBuiltIn = uiState.builtInCharts.any { it.id == defaultEditId }
                                    if (isBuiltIn) {
                                        onIntent(MainIntent.DeleteBuiltInChart(defaultEditId, saveAsDefault = true))
                                    } else {
                                        onIntent(MainIntent.DeleteCustomChart(defaultEditId))
                                    }
                                }
                                "command" -> {
                                    val isQuickButton = uiState.quickButtons.any { it.id == defaultEditId }
                                    if (isQuickButton) {
                                        onIntent(MainIntent.DeleteQuickButton(defaultEditId, saveAsDefault = true))
                                    } else {
                                        onIntent(MainIntent.UpdateCustomCommand(CustomCommandDef(defaultEditId, "", ""), saveAsDefault = true))
                                    }
                                }
                            }
                        }
                        defaultEditKind = ""
                    }) { Text("删除", color = Color.White, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = {
                        val trimmedName = defaultEditName.trim()
                        val trimmedJson = defaultEditJson.trim()
                        if (defaultEditId.isEmpty() && trimmedName.isNotBlank()) {
                            when (defaultEditKind) {
                                "overview" -> {
                                    val newId = java.util.UUID.randomUUID().toString()
                                    onIntent(MainIntent.EditOverviewItem(newId, trimmedName, trimmedJson, saveAsDefault = true))
                                }
                                "chart" -> onIntent(MainIntent.AddCustomChart(CustomChartDef(name = trimmedName, fieldPath = trimmedJson)))
                                "command" -> onIntent(MainIntent.AddQuickButton(QuickButtonDef(label = trimmedName, wireValue = trimmedJson), saveAsDefault = true))
                            }
                        } else {
                            when (defaultEditKind) {
                                "overview" -> onIntent(MainIntent.EditOverviewItem(defaultEditId, trimmedName, trimmedJson, saveAsDefault = true))
                                "chart" -> {
                                    val isBuiltIn = uiState.builtInCharts.any { it.id == defaultEditId }
                                    if (isBuiltIn) {
                                        onIntent(MainIntent.UpdateBuiltInChart(defaultEditId, trimmedName, trimmedJson, saveAsDefault = true))
                                    } else {
                                        onIntent(MainIntent.UpdateCustomChart(CustomChartDef(id = defaultEditId, name = trimmedName, fieldPath = trimmedJson)))
                                    }
                                }
                                "command" -> {
                                    val isQuickButton = uiState.quickButtons.any { it.id == defaultEditId }
                                    if (isQuickButton) {
                                        onIntent(MainIntent.UpdateQuickButton(QuickButtonDef(id = defaultEditId, label = trimmedName, wireValue = trimmedJson), saveAsDefault = true))
                                    } else {
                                        onIntent(MainIntent.UpdateCustomCommand(CustomCommandDef(defaultEditId, trimmedName, trimmedJson), saveAsDefault = true))
                                    }
                                }
                            }
                        }
                        defaultEditKind = ""
                    }) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                TextButton(onClick = { defaultEditKind = "" }) { Text("取消", color = Color.White.copy(alpha = 0.7f)) }
            },
        )
    }
}

@Composable
private fun DefaultSettingsCard(
    uiState: MainUiState,
    onEditOverview: (String, String, String) -> Unit,
    onEditChart: (String, String, String) -> Unit,
    onEditCommand: (String, String, String) -> Unit,
    onDeleteOverview: ((String) -> Unit)? = null,
    onDeleteChart: ((String) -> Unit)? = null,
    onDeleteCommand: ((String) -> Unit)? = null,
    onAddOverview: () -> Unit = {},
    onAddChart: () -> Unit = {},
    onAddCommand: () -> Unit = {},
) {
    val overviewDefaults = listOf(
        "vehicle_state" to "车辆状态",
        "link_state" to "连接链路",
        "rfid_tag" to "RFID 点位",
        "battery" to "电池余量",
        "environment" to "环境概况",
        "obstacle" to "防撞状态",
    )

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "默认设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "点进默认项可修改名称和对应 JSON 数据字段",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("总览默认", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onAddOverview) {
                    Icon(Icons.Outlined.Add, contentDescription = "添加", modifier = Modifier.size(20.dp), tint = Color.White)
                }
            }
            overviewDefaults.forEach { (id, fallbackName) ->
                val state = uiState.overviewItemStates[id]
                val name = state?.customTitle ?: fallbackName
                val json = state?.customFieldPath ?: defaultOverviewFieldPath(id)
                DefaultSettingRow(name = name, json = json, onClick = { onEditOverview(id, name, json) }, onDelete = { onDeleteOverview?.invoke(id) })
            }
            // Show custom overview items added via "+"
            val hardcodedOverviewIds = overviewDefaults.map { it.first }.toSet()
            uiState.overviewItemStates.forEach { (itemId, state) ->
                if (itemId !in hardcodedOverviewIds && !state.deleted) {
                    val name = state.customTitle ?: itemId
                    val json = state.customFieldPath ?: ""
                    DefaultSettingRow(name = name, json = json, onClick = { onEditOverview(itemId, name, json) }, onDelete = { onDeleteOverview?.invoke(itemId) })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("监控图表默认", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onAddChart) {
                    Icon(Icons.Outlined.Add, contentDescription = "添加", modifier = Modifier.size(20.dp), tint = Color.White)
                }
            }
            uiState.builtInCharts.forEach { chart ->
                DefaultSettingRow(
                    name = chart.defaultName,
                    json = chart.defaultFieldPath,
                    onClick = { onEditChart(chart.id, chart.defaultName, chart.defaultFieldPath) },
                    onDelete = { onDeleteChart?.invoke(chart.id) },
                )
            }
            // Show custom charts added via "+"
            val builtInChartIds = uiState.builtInCharts.map { it.id }.toSet()
            uiState.customCharts.forEach { chart ->
                if (chart.id !in builtInChartIds) {
                    DefaultSettingRow(
                        name = chart.name,
                        json = chart.fieldPath,
                        onClick = { onEditChart(chart.id, chart.name, chart.fieldPath) },
                        onDelete = { onDeleteChart?.invoke(chart.id) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("快速控制按钮默认", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onAddCommand) {
                    Icon(Icons.Outlined.Add, contentDescription = "添加", modifier = Modifier.size(20.dp), tint = Color.White)
                }
            }
            uiState.availableCommands.forEach { command ->
                val custom = uiState.customCommands.find { it.commandId == command.name }
                val name = custom?.customLabel?.ifBlank { null } ?: command.label
                val json = custom?.customWireValue?.ifBlank { null } ?: command.wireValue
                DefaultSettingRow(name = name, json = json, onClick = { onEditCommand(command.name, name, json) }, onDelete = { onDeleteCommand?.invoke(command.name) })
            }
            // Show quick buttons added via "+"
            val commandNames = uiState.availableCommands.map { it.name }.toSet()
            uiState.quickButtons.forEach { btn ->
                if (btn.id !in commandNames) {
                    DefaultSettingRow(
                        name = btn.label,
                        json = btn.wireValue,
                        onClick = { onEditCommand(btn.id, btn.label, btn.wireValue) },
                        onDelete = { onDeleteCommand?.invoke(btn.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultSettingRow(
    name: String,
    json: String,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = json,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除", modifier = Modifier.size(18.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun ColorThemeSection(
    currentTheme: ColorTheme,
    vibrancyLevel: Float,
    onThemeSelected: (ColorTheme) -> Unit,
    onVibrancyChanged: (Float) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "颜色主题",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // 3-color Morandi swatches row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorTheme.entries.forEach { theme ->
                    val swatch = themeSwatches[theme] ?: return@forEach
                    val gearColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                    val gearInnerColor = MaterialTheme.colorScheme.surfaceContainer
                    val selectedRingColor = MaterialTheme.colorScheme.onSurface
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clickable { onThemeSelected(theme) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.size(54.dp)) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val gearRadius = size.minDimension / 2f - 2f
                                val toothOuter = gearRadius
                                val toothInner = gearRadius - 4f
                                val teeth = 18
                                val gear = Path()
                                for (i in 0 until teeth * 2) {
                                    val angle = (-90f + i * 360f / (teeth * 2)) * kotlin.math.PI.toFloat() / 180f
                                    val radius = if (i % 2 == 0) toothOuter else toothInner
                                    val point = Offset(
                                        center.x + kotlin.math.cos(angle) * radius,
                                        center.y + kotlin.math.sin(angle) * radius,
                                    )
                                    if (i == 0) gear.moveTo(point.x, point.y) else gear.lineTo(point.x, point.y)
                                }
                                gear.close()
                                drawPath(gear, gearColor)
                                drawCircle(gearInnerColor, radius = toothInner - 2f, center = center)

                                val inset = 9f
                                val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                                val arcTopLeft = Offset(inset, inset)
                                drawArc(swatch.surface, -90f, 120f, true, arcTopLeft, arcSize)
                                drawArc(swatch.primary, 30f, 120f, true, arcTopLeft, arcSize)
                                drawArc(swatch.tertiary, 150f, 120f, true, arcTopLeft, arcSize)
                                drawCircle(
                                    color = if (currentTheme == theme) selectedRingColor else gearInnerColor,
                                    radius = arcSize.width / 2f,
                                    center = center,
                                    style = Stroke(width = if (currentTheme == theme) 3.5f else 1.5f),
                                )
                            }
                        }
                        Text(
                            text = theme.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }

            // Contrast slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "鲜艳度",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "%.0f%%".format(vibrancyLevel * 100),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = vibrancyLevel,
                    onValueChange = onVibrancyChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AlertBanner(uiState: MainUiState) {
    val color = when (uiState.activeAlert) {
        AlertLevel.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        AlertLevel.Alarm -> MaterialTheme.colorScheme.secondaryContainer
        AlertLevel.Critical -> MaterialTheme.colorScheme.errorContainer
        AlertLevel.Normal -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "当前告警级别：${uiState.activeAlert.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "温度 ${uiState.telemetry.temperatureC.toInt()}°C，气体 ${(uiState.telemetry.gasPercent * 100).toInt()}%，状态 ${uiState.telemetry.vehicleState.label}，返航 ${uiState.telemetry.returnReason.label}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun defaultOverviewFieldPath(itemId: String): String = when (itemId) {
    "vehicle_state" -> "vehicle_state"
    "link_state" -> "link_state"
    "rfid_tag" -> "rfid"
    "battery" -> "battery"
    "environment" -> "MLX_obj / MQ8"
    "obstacle" -> "obstacle_distance"
    else -> itemId
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullOverviewCard(
    uiState: MainUiState,
    onItemClick: (itemId: String, title: String, fieldPath: String) -> Unit,
    onItemDelete: (itemId: String) -> Unit,
) {
    data class FullItem(val id: String, val label: String, val value: String)

    val items = listOf(
        FullItem("full_vstate", "车辆状态", uiState.telemetry.vehicleState.label),
        FullItem("full_link", "连接链路", uiState.telemetry.linkState.label),
        FullItem("full_pos", "当前点位", uiState.telemetry.positionLabel),
        FullItem("full_rfid", "RFID 标签", uiState.telemetry.rfidTag),
        FullItem("full_bat", "电池余量", "${uiState.telemetry.batteryPercent}%"),
        FullItem("full_speed", "车速", "%.2f m/s".format(uiState.telemetry.speedMetersPerSecond)),
        FullItem("full_envtemp", "环境温度", "%.1f°C".format(uiState.telemetry.temperatureC)),
        FullItem("full_mlxobj", "红外目标温度", "%.1f°C".format(uiState.telemetry.mlxObjectTemperatureC)),
        FullItem("full_mlxamb", "红外环境温度", "%.1f°C".format(uiState.telemetry.mlxAmbientTemperatureC)),
        FullItem("full_ahttemp", "AHT 温度", "%.1f°C".format(uiState.telemetry.ahtTemperatureC)),
        FullItem("full_ahtrh", "AHT 湿度", "%.1f%%".format(uiState.telemetry.ahtHumidityPercent)),
        FullItem("full_mq8", "MQ8 气体", "%.0f / %.0f%%".format(uiState.telemetry.mq8Raw, uiState.telemetry.gasPercent * 100f)),
        FullItem("full_track", "巡迹状态", "${uiState.telemetry.trackBinary} (${uiState.telemetry.trackValue})"),
        FullItem("full_dist", "避障测距", "${uiState.telemetry.obstacleDistanceCm} cm"),
        FullItem("full_collision", "防撞状态", if (uiState.telemetry.obstacleDetected) "已触发" else "安全"),
        FullItem("full_return", "返航原因", uiState.telemetry.returnReason.label),
        FullItem("full_home", "归位状态", if (uiState.telemetry.homeDockReached) "已到位" else "未到位"),
        FullItem("full_latency", "延迟", if (uiState.telemetry.latencyMs > 0) "${uiState.telemetry.latencyMs} ms" else "--"),
    )

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "全览指标",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "长按各项可编辑或删除",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.filter { item ->
                    val state = uiState.overviewItemStates[item.id]
                    state == null || !state.deleted
                }.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { item ->
                            val state = uiState.overviewItemStates[item.id]
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = { },
                                        onLongClick = {
                                            val title = state?.customTitle ?: item.label
                                            val fieldPath = state?.customFieldPath ?: item.id
                                            onItemClick(item.id, title, fieldPath)
                                        },
                                    ),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = item.value,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    title: String,
    value: String,
    supporting: String,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EditableMetricRow(
    title: String,
    value: String,
    supporting: String,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$title: $value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Row {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp))
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommandChip(
    label: String,
    emphasis: CommandEmphasis,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun GrayChartCard(
    title: String,
    subtitle: String,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onHide: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (onEdit != null || onDelete != null || onHide != null) {
                    Row {
                        if (onEdit != null) {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Outlined.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
                            }
                        }
                        if (onHide != null) {
                            IconButton(onClick = onHide) {
                                Icon(Icons.Outlined.Visibility, contentDescription = "隐藏", modifier = Modifier.size(18.dp))
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Outlined.Delete, contentDescription = "删除", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            content()
        }
    }
}

private data class TabItem(
    val tab: AppTab,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private fun tabItems(): List<TabItem> = listOf(
    TabItem(AppTab.Overview, Icons.Outlined.DashboardCustomize),
    TabItem(AppTab.Monitor, Icons.Outlined.MonitorHeart),
    TabItem(AppTab.Control, Icons.Outlined.SettingsRemote),
    TabItem(AppTab.History, Icons.Outlined.History),
    TabItem(AppTab.Settings, Icons.Outlined.Settings),
)

@Composable
private fun levelColor(level: String): Color = when (level) {
    "CRITICAL" -> MaterialTheme.colorScheme.error
    "ALARM", "WARN", "RETURN", "OBST" -> MaterialTheme.colorScheme.tertiary
    "ACK", "RFID" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
}

@Preview(
    name = "主界面预览",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun CarHostScaffoldPreview() {
    CarHostTheme(useDynamicColor = false) {
        Surface {
            CarHostScaffold(
                uiState = MainUiState(
                    selectedTab = AppTab.Control,
                    telemetry = TelemetrySnapshot(
                        linkState = LinkState.Connected,
                        vehicleState = VehicleState.Patrol,
                        positionLabel = "RFID-03",
                        rfidTag = "RFID-03",
                        locationDescription = "三号巡检柜",
                        temperatureC = 36.5f,
                        gasPercent = 1234f / 4095f,
                        mq8Raw = 1234f,
                        ahtTemperatureC = 25f,
                        ahtHumidityPercent = 60f,
                        mlxObjectTemperatureC = 36.5f,
                        mlxAmbientTemperatureC = 25f,
                        trackValue = 3,
                        trackBinary = "0011",
                        batteryPercent = 86,
                        speedMetersPerSecond = 0.35f,
                        latencyMs = 42,
                        obstacleDistanceCm = 68,
                        returnReason = ReturnReason.None,
                        temperatureHistory = listOf(
                            ChartPoint("14:20:01", 31.8f),
                            ChartPoint("14:20:03", 32.4f),
                            ChartPoint("14:20:05", 33.1f),
                            ChartPoint("14:20:07", 34.9f),
                            ChartPoint("14:20:09", 36.5f),
                        ),
                        gasHistory = listOf(
                            ChartPoint("14:20:01", 0.19f),
                            ChartPoint("14:20:03", 0.22f),
                            ChartPoint("14:20:05", 0.25f),
                            ChartPoint("14:20:07", 0.28f),
                            ChartPoint("14:20:09", 1234f / 4095f),
                        ),
                        batteryHistory = listOf(
                            ChartPoint("14:20:01", 90f),
                            ChartPoint("14:20:03", 89f),
                            ChartPoint("14:20:05", 88f),
                            ChartPoint("14:20:07", 87f),
                            ChartPoint("14:20:09", 86f),
                        ),
                        mlxObjectHistory = listOf(
                            ChartPoint("14:20:01", 31.8f),
                            ChartPoint("14:20:03", 32.4f),
                            ChartPoint("14:20:05", 33.1f),
                            ChartPoint("14:20:07", 34.9f),
                            ChartPoint("14:20:09", 36.5f),
                        ),
                        ahtTemperatureHistory = listOf(
                            ChartPoint("14:20:01", 24.6f),
                            ChartPoint("14:20:03", 24.8f),
                            ChartPoint("14:20:05", 24.9f),
                            ChartPoint("14:20:07", 25.0f),
                            ChartPoint("14:20:09", 25.0f),
                        ),
                        ahtHumidityHistory = listOf(
                            ChartPoint("14:20:01", 58f),
                            ChartPoint("14:20:03", 59f),
                            ChartPoint("14:20:05", 59.5f),
                            ChartPoint("14:20:07", 60f),
                            ChartPoint("14:20:09", 60f),
                        ),
                        mq8History = listOf(
                            ChartPoint("14:20:01", 820f),
                            ChartPoint("14:20:03", 920f),
                            ChartPoint("14:20:05", 1030f),
                            ChartPoint("14:20:07", 1160f),
                            ChartPoint("14:20:09", 1234f),
                        ),
                        trackHistory = listOf(
                            TrackPoint("14:20:01", "0001"),
                            TrackPoint("14:20:03", "0011"),
                            TrackPoint("14:20:05", "0110"),
                            TrackPoint("14:20:07", "0010"),
                            TrackPoint("14:20:09", "0011"),
                        ),
                    ),
                    logs = listOf(
                        LogEntry(1, "14:20:01", "ACK", "TCP 通道已建立，等待 telemetry 数据"),
                        LogEntry(2, "14:20:03", "RX", "{\"type\":\"telemetry\",\"MQ8\":1234,\"AHT_temp\":25,\"MLX_obj\":36.5}"),
                    ),
                    customCharts = emptyList(),
                ),
                onIntent = {},
            )
        }
    }
}

