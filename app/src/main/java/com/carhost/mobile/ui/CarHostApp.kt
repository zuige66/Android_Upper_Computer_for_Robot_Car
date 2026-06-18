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
                    Text("Host Computer")
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
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onIntent(MainIntent.RestoreDefaultOverview) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("恢复", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onIntent(MainIntent.ResetOverviewData) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
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
            }
        }

        item {
            FullOverviewCard(uiState = uiState)
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
                TextButton(onClick = {
                    onIntent(MainIntent.EditOverviewItem(editingItemId, editingTitle, editingFieldPath))
                    showEditDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消") }
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
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onIntent(MainIntent.RestoreDefaultCharts) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("恢复", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onIntent(MainIntent.ClearMonitorData) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { openAddCustomDialog() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("自定义", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("取消") }
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
                TextButton(onClick = {
                    onIntent(MainIntent.UpdateBuiltInChart(editingBuiltInId, builtInName.trim(), builtInFieldPath.trim()))
                    showBuiltInDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showBuiltInDialog = false }) { Text("取消") }
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
    val defaultRows = defaultButtons.chunked(2)
    var quickSendText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card {
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
                        OutlinedButton(onClick = { onIntent(MainIntent.SaveEndpoint) }) {
                            Text("保存参数")
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Button(onClick = { onIntent(MainIntent.ToggleConnection) }) {
                            Text(if (uiState.connectionActive) "断开连接" else "开始连接")
                        }
                    }
                }
            }
        }

        item {
            Card {
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
                                            if (cmd != null) onIntent(MainIntent.SendCommand(cmd))
                                        },
                                        onLongClick = {
                                            val cmd = uiState.availableCommands.find { it.name == id }
                                            if (cmd != null) openEditDefault(cmd)
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
                        Button(
                            onClick = {
                                if (quickSendText.isNotBlank()) {
                                    onIntent(MainIntent.SendRawCommand(quickSendText.trim()))
                                }
                            },
                            enabled = quickSendText.isNotBlank(),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "发送", modifier = Modifier.size(18.dp))
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
                TextButton(onClick = {
                    onIntent(MainIntent.UpdateCustomCommand(
                        CustomCommandDef(
                            commandId = editingCommandId,
                            customLabel = editLabel.trim(),
                            customWireValue = editWireValue.trim(),
                        )
                    ))
                    showEditDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消") }
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
            Card {
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
                    Button(
                        onClick = { onIntent(MainIntent.ClearHistory) },
                        enabled = uiState.logs.isNotEmpty(),
                    ) {
                        Text("清除所有数据")
                    }
                }
            }
        }

        if (uiState.logs.isEmpty()) {
            item {
                Card {
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
            Card {
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
                contrastLevel = uiState.preferences.contrastLevel,
                onThemeSelected = { onIntent(MainIntent.SetColorTheme(it)) },
                onContrastChanged = { onIntent(MainIntent.SetContrastLevel(it)) },
            )
        }

        // JSON viewer section
        item {
            Card {
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
                        Button(
                            onClick = {
                                if (customSendText.isNotBlank()) {
                                    onIntent(MainIntent.SendRawCommand(customSendText.trim()))
                                }
                            },
                            enabled = customSendText.isNotBlank(),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "发送", modifier = Modifier.size(18.dp))
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
            )
        }

        item {
            Card {
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
                TextButton(onClick = {
                    when (defaultEditKind) {
                        "overview" -> onIntent(MainIntent.EditOverviewItem(defaultEditId, defaultEditName.trim(), defaultEditJson.trim()))
                        "chart" -> onIntent(MainIntent.UpdateBuiltInChart(defaultEditId, defaultEditName.trim(), defaultEditJson.trim()))
                        "command" -> onIntent(MainIntent.UpdateCustomCommand(CustomCommandDef(defaultEditId, defaultEditName.trim(), defaultEditJson.trim())))
                    }
                    defaultEditKind = ""
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { defaultEditKind = "" }) { Text("取消") }
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
) {
    val overviewDefaults = listOf(
        "vehicle_state" to "车辆状态",
        "link_state" to "连接链路",
        "rfid_tag" to "RFID 点位",
        "battery" to "电池余量",
        "environment" to "环境概况",
        "obstacle" to "防撞状态",
    )

    Card {
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

            Text("总览默认", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            overviewDefaults.forEach { (id, fallbackName) ->
                val state = uiState.overviewItemStates[id]
                val name = state?.customTitle ?: fallbackName
                val json = state?.customFieldPath ?: defaultOverviewFieldPath(id)
                DefaultSettingRow(name = name, json = json, onClick = { onEditOverview(id, name, json) })
            }

            Text("监控图表默认", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            uiState.builtInCharts.forEach { chart ->
                DefaultSettingRow(
                    name = chart.defaultName,
                    json = chart.defaultFieldPath,
                    onClick = { onEditChart(chart.id, chart.defaultName, chart.defaultFieldPath) },
                )
            }

            Text("快速控制六按钮默认", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            uiState.availableCommands.forEach { command ->
                val custom = uiState.customCommands.find { it.commandId == command.name }
                val name = custom?.customLabel?.ifBlank { null } ?: command.label
                val json = custom?.customWireValue?.ifBlank { null } ?: command.wireValue
                DefaultSettingRow(name = name, json = json, onClick = { onEditCommand(command.name, name, json) })
            }
        }
    }
}

@Composable
private fun DefaultSettingRow(
    name: String,
    json: String,
    onClick: () -> Unit,
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
        Icon(Icons.Outlined.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ColorThemeSection(
    currentTheme: ColorTheme,
    contrastLevel: Float,
    onThemeSelected: (ColorTheme) -> Unit,
    onContrastChanged: (Float) -> Unit,
) {
    Card {
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .then(
                                    if (currentTheme == theme)
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { onThemeSelected(theme) },
                        ) {
                            Canvas(modifier = Modifier.size(44.dp)) {
                                val half = size.height / 2f
                                drawArc(swatch.surface, 180f, 180f, true, Offset.Zero)
                                drawArc(swatch.primary, 0f, 90f, true, Offset(0f, half))
                                drawArc(swatch.tertiary, 90f, 90f, true, Offset(0f, half))
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
                        text = "%.0f%%".format(contrastLevel * 100),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = contrastLevel,
                    onValueChange = onContrastChanged,
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

@Composable
private fun FullOverviewCard(uiState: MainUiState) {
    val data = listOf(
        "车辆状态" to uiState.telemetry.vehicleState.label,
        "连接链路" to uiState.telemetry.linkState.label,
        "当前点位" to uiState.telemetry.positionLabel,
        "RFID 标签" to "${uiState.telemetry.rfidTag} · ${uiState.telemetry.locationDescription}",
        "电池余量" to "${uiState.telemetry.batteryPercent}%",
        "车速" to "%.2f m/s".format(uiState.telemetry.speedMetersPerSecond),
        "环境温度" to "%.1f°C".format(uiState.telemetry.temperatureC),
        "红外目标温度" to "%.1f°C".format(uiState.telemetry.mlxObjectTemperatureC),
        "红外环境温度" to "%.1f°C".format(uiState.telemetry.mlxAmbientTemperatureC),
        "AHT 温度" to "%.1f°C".format(uiState.telemetry.ahtTemperatureC),
        "AHT 湿度" to "%.1f%%".format(uiState.telemetry.ahtHumidityPercent),
        "MQ8 气体" to "%.0f / %.0f%%".format(uiState.telemetry.mq8Raw, uiState.telemetry.gasPercent * 100f),
        "巡迹状态" to "${uiState.telemetry.trackBinary} (${uiState.telemetry.trackValue})",
        "避障测距" to "${uiState.telemetry.obstacleDistanceCm} cm",
        "防撞状态" to if (uiState.telemetry.obstacleDetected) "已触发" else "安全",
        "返航原因" to uiState.telemetry.returnReason.label,
        "归位状态" to if (uiState.telemetry.homeDockReached) "已到位" else "未到位",
        "延迟" to if (uiState.telemetry.latencyMs > 0) "${uiState.telemetry.latencyMs} ms" else "--",
    )

    Card {
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
                text = "包含车辆状态、传感器数据和 RFID 位置信息",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                data.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { (label, value) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = value,
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
    Card {
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
    Card {
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
    val containerColor = when (emphasis) {
        CommandEmphasis.Primary -> MaterialTheme.colorScheme.primaryContainer
        CommandEmphasis.Accent -> MaterialTheme.colorScheme.tertiaryContainer
        CommandEmphasis.Danger -> MaterialTheme.colorScheme.primaryContainer
        CommandEmphasis.Neutral -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .background(containerColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
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
    Card {
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
    Card {
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
