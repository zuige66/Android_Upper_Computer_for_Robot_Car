package com.carhost.mobile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carhost.mobile.data.model.AlertLevel
import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.ChartPoint
import com.carhost.mobile.data.model.CommandEmphasis
import com.carhost.mobile.data.model.CustomChartDef
import com.carhost.mobile.data.model.CustomChartType
import com.carhost.mobile.data.model.CustomCommandDef
import com.carhost.mobile.data.model.QuickButtonDef
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

@Composable
fun CarHostApp(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CarHostTheme(useDynamicColor = uiState.preferences.useDynamicColor) {
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
                    Column {
                        Text("Host Computer")
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                when (uiState.telemetry.linkState) {
                                    LinkState.Online,
                                    LinkState.Connected -> "已连接"
                                    LinkState.Connecting -> "连接中"
                                    LinkState.Fault -> "链路故障"
                                    LinkState.Offline -> "离线"
                                }
                            )
                        },
                    )
                },
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                tabItems().forEach { item ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == item.tab,
                        onClick = { onIntent(MainIntent.SelectTab(item.tab)) },
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
                AppTab.Overview -> OverviewScreen(uiState = uiState)
                AppTab.Monitor -> MonitorScreen(uiState = uiState, onIntent = onIntent)
                AppTab.Control -> ControlScreen(uiState = uiState, onIntent = onIntent)
                AppTab.History -> HistoryScreen(uiState = uiState, onIntent = onIntent)
                AppTab.Settings -> SettingsScreen(uiState = uiState, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun OverviewScreen(uiState: MainUiState) {
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

        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricRow(
                    title = "车辆状态",
                    value = uiState.telemetry.vehicleState.label,
                    supporting = "当前工位 ${uiState.telemetry.positionLabel} · ${uiState.telemetry.locationDescription}",
                )
                MetricRow(
                    title = "连接链路",
                    value = uiState.telemetry.linkState.label,
                    supporting = "TCP Socket 当前连接状态",
                )
                MetricRow(
                    title = "RFID 点位",
                    value = uiState.telemetry.rfidTag,
                    supporting = "最近一次识别：${uiState.telemetry.locationDescription}",
                )
                MetricRow(
                    title = "电池余量",
                    value = "${uiState.telemetry.batteryPercent}%",
                    supporting = "返航原因：${uiState.telemetry.returnReason.label}",
                )
                MetricRow(
                    title = "环境概况",
                    value = "${uiState.telemetry.temperatureC.toInt()}°C / ${(uiState.telemetry.gasPercent * 100).toInt()}%",
                    supporting = "红外目标温度与 MQ8 原始值换算结果",
                )
                MetricRow(
                    title = "防撞状态",
                    value = if (uiState.telemetry.obstacleDetected) "已触发" else "安全",
                    supporting = "前方距离 ${uiState.telemetry.obstacleDistanceCm} cm，仅在巡检/撤离时启用",
                )
            }
        }

        item {
            RouteStripCard(uiState = uiState)
        }
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
        // Top bar: title + clear button
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
                            text = "实时监控",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "实时数据图表和状态指标",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Button(
                        onClick = { onIntent(MainIntent.ClearMonitorData) },
                        enabled = hasAnyHistory,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text("清除所有数据")
                    }
                }
            }
        }

        // Custom charts header
        item {
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "自定义图表",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = { openAddCustomDialog() }) {
                        Icon(Icons.Outlined.Add, contentDescription = "新建图表")
                    }
                }
            }
        }

        if (uiState.customCharts.isEmpty()) {
            item {
                Card {
                    Text(
                        text = "点击上方 + 新建自定义图表，输入 JSON 字段路径即可绘图",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

        // Built-in charts header
        item {
            Card {
                Text(
                    text = "内置图表",
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Built-in charts (from data-driven list)
        items(uiState.builtInCharts.size, key = { "builtin_${uiState.builtInCharts[it].id}" }) { index ->
            val chart = uiState.builtInCharts[index]
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
                    }
                }
                GrayChartCard(
                    title = chart.defaultName,
                    subtitle = chart.subtitle,
                    onEdit = { openEditBuiltInDialog(chart) },
                    onDelete = { onIntent(MainIntent.DeleteBuiltInChart(chart.id)) },
                ) {
                    chartContent()
                }
            } else {
                // Hidden chart: collapsed row with restore button
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                                Icons.Outlined.MonitorHeart,
                                contentDescription = "恢复显示",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // RFID flow chart
        item {
            RfidFlowCard(
                title = "RFID 位置流程",
                currentLabel = uiState.telemetry.positionLabel,
                locations = uiState.telemetry.rfidHistory,
            )
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ControlScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editingCommandId by remember { mutableStateOf("") }
    var editingIsDefault by remember { mutableStateOf(true) }
    var editLabel by remember { mutableStateOf("") }
    var editWireValue by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var addLabel by remember { mutableStateOf("") }
    var addWireValue by remember { mutableStateOf("") }

    fun openEditDefault(command: VehicleCommand) {
        editingCommandId = command.name
        editingIsDefault = true
        val custom = uiState.customCommands.find { it.commandId == command.name }
        editLabel = custom?.customLabel?.ifBlank { null } ?: command.label
        editWireValue = custom?.customWireValue?.ifBlank { null } ?: command.wireValue
        showEditDialog = true
    }

    fun openEditCustom(btn: QuickButtonDef) {
        editingCommandId = btn.id
        editingIsDefault = false
        editLabel = btn.label
        editWireValue = btn.wireValue
        showEditDialog = true
    }

    // All buttons: default 6 + custom quick buttons
    val allButtons = buildList {
        uiState.availableCommands.forEach { cmd ->
            val custom = uiState.customCommands.find { it.commandId == cmd.name }
            add(Triple(
                custom?.customLabel?.ifBlank { null } ?: cmd.label,
                cmd.emphasis,
                cmd.name,
            ))
        }
        uiState.quickButtons.forEach { btn ->
            add(Triple(btn.label, CommandEmphasis.Neutral, "quick_${btn.id}"))
        }
    }

    // Chunk into rows of 2
    val rows = allButtons.chunked(2)

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
                        Row {
                            IconButton(onClick = {
                                addLabel = ""
                                addWireValue = ""
                                showAddDialog = true
                            }) {
                                Icon(Icons.Outlined.Add, contentDescription = "添加按钮")
                            }
                            IconButton(onClick = { onIntent(MainIntent.RestoreDefaultCommands) }) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "恢复默认")
                            }
                        }
                    }

                    Text(
                        text = "长按按钮可编辑或删除",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // 2-column grid
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { (label, emphasis, id) ->
                                val isDefault = !id.startsWith("quick_")
                                Box(modifier = Modifier.weight(1f)) {
                                    CommandChip(
                                        label = label,
                                        emphasis = emphasis,
                                        onClick = {
                                            if (isDefault) {
                                                val cmd = uiState.availableCommands.find { it.name == id }
                                                if (cmd != null) onIntent(MainIntent.SendCommand(cmd))
                                            } else {
                                                val btnId = id.removePrefix("quick_")
                                                val btn = uiState.quickButtons.find { it.id == btnId }
                                                if (btn != null) onIntent(MainIntent.SendRawCommand(btn.wireValue))
                                            }
                                        },
                                        onLongClick = {
                                            if (isDefault) {
                                                val cmd = uiState.availableCommands.find { it.name == id }
                                                if (cmd != null) openEditDefault(cmd)
                                            } else {
                                                val btnId = id.removePrefix("quick_")
                                                val btn = uiState.quickButtons.find { it.id == btnId }
                                                if (btn != null) openEditCustom(btn)
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
                    if (editingIsDefault) {
                        val cmd = uiState.availableCommands.find { it.name == editingCommandId }
                        if (cmd != null) {
                            Text(
                                text = "默认: ${cmd.label} / ${cmd.wireValue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    if (!editingIsDefault) {
                        TextButton(onClick = {
                            showDeleteConfirm = true
                        }) {
                            Text("删除", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    TextButton(onClick = {
                        if (editingIsDefault) {
                            onIntent(MainIntent.UpdateCustomCommand(
                                CustomCommandDef(
                                    commandId = editingCommandId,
                                    customLabel = editLabel.trim(),
                                    customWireValue = editWireValue.trim(),
                                )
                            ))
                        } else {
                            onIntent(MainIntent.UpdateQuickButton(
                                QuickButtonDef(
                                    id = editingCommandId,
                                    label = editLabel.trim(),
                                    wireValue = editWireValue.trim(),
                                )
                            ))
                        }
                        showEditDialog = false
                    }) { Text("保存") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消") }
            },
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个自定义按钮吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onIntent(MainIntent.DeleteQuickButton(editingCommandId))
                    showDeleteConfirm = false
                    showEditDialog = false
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            },
        )
    }

    // Add button dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加快捷按钮") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = addLabel,
                        onValueChange = { addLabel = it },
                        label = { Text("按钮名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = addWireValue,
                        onValueChange = { addWireValue = it },
                        label = { Text("发送字段") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addLabel.isNotBlank() && addWireValue.isNotBlank()) {
                        onIntent(MainIntent.AddQuickButton(
                            QuickButtonDef(label = addLabel.trim(), wireValue = addWireValue.trim())
                        ))
                        showAddDialog = false
                    }
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RouteStripCard(uiState: MainUiState) {
    val routeNodes = listOf(
        "START" to "起点",
        "A1" to "A1",
        "A2" to "A2",
        "B2" to "B2",
        "B3" to "B3",
    )

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "RFID 路线映射",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                routeNodes.forEach { (code, label) ->
                    val active = uiState.telemetry.positionLabel == code
                    AssistChip(
                        onClick = { },
                        label = { Text("$label · $code") },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = if (active) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            labelColor = if (active) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                    )
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
        CommandEmphasis.Danger -> MaterialTheme.colorScheme.errorContainer
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
                if (onEdit != null || onDelete != null) {
                    Row {
                        if (onEdit != null) {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Outlined.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
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
