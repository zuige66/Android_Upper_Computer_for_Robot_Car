package com.carhost.mobile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carhost.mobile.data.model.AlertLevel
import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.CommandEmphasis
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.model.VehicleCommand
import com.carhost.mobile.ui.components.LineChartCard
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
                        Text("无人工厂巡检移动上位机")
                        Text(
                            text = "面向巡检、报警、返航和人工干预的手机控制台",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                when (uiState.telemetry.linkState) {
                                    LinkState.Online -> "在线 ${uiState.telemetry.latencyMs} ms"
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
                AppTab.Monitor -> MonitorScreen(uiState = uiState)
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
                    supporting = "往返延迟 ${uiState.telemetry.latencyMs} ms",
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
                    supporting = "温度、气体双阈值联动",
                )
                MetricRow(
                    title = "防撞状态",
                    value = if (uiState.telemetry.obstacleDetected) "已触发" else "安全",
                    supporting = "前方距离 ${uiState.telemetry.obstacleDistanceCm} cm，仅在巡检/撤离时启用",
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "任务书对应能力",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChecklistLine("WiFi/TCP 连接状态可视化")
                    ChecklistLine("温度、气体、电量、速度实时展示")
                    ChecklistLine("返航、急停、暂停、复位等关键命令直达")
                    ChecklistLine("报警和状态历史可追溯")
                    ChecklistLine("RFID 点位、防撞上报、低电返航已经接入状态层")
                }
            }
        }

        item {
            RouteStripCard(uiState = uiState)
        }
    }
}

@Composable
private fun MonitorScreen(uiState: MainUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            LineChartCard(
                title = "温度曲线",
                valueText = "${uiState.telemetry.temperatureC.toInt()}°C",
                values = uiState.telemetry.temperatureHistory,
            )
        }
        item {
            LineChartCard(
                title = "气体浓度",
                valueText = "${(uiState.telemetry.gasPercent * 100).toInt()}%",
                values = uiState.telemetry.gasHistory.map { it * 100f },
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricRow("车速", "${uiState.telemetry.speedMetersPerSecond} m/s", "弯道与撤离状态自动切速")
                MetricRow("当前点位", uiState.telemetry.positionLabel, uiState.telemetry.locationDescription)
                MetricRow("RFID 标签", uiState.telemetry.rfidTag, "可直接映射工位、路口和告警位置信息")
                MetricRow(
                    "避障测距",
                    "${uiState.telemetry.obstacleDistanceCm} cm",
                    if (uiState.telemetry.obstacleDetected) "已触发避障停车" else "前方净空正常",
                )
                MetricRow(
                    "返航状态",
                    uiState.telemetry.returnReason.label,
                    if (uiState.telemetry.homeDockReached) "已回到起点 / 充电桩" else "尚未回桩",
                )
                MetricRow("链路延迟", "${uiState.telemetry.latencyMs} ms", "后续可接入 ACK 超时判断")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ControlScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { onIntent(MainIntent.SaveEndpoint) }) {
                            Text("保存参数")
                        }
                        OutlinedButton(onClick = { onIntent(MainIntent.ToggleConnection) }) {
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
                    Text(
                        text = "快速控制",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        uiState.availableCommands.forEach { command ->
                            CommandChip(
                                command = command,
                                onClick = { onIntent(MainIntent.SendCommand(command)) },
                            )
                        }
                    }
                }
            }
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "事件与报警历史",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "目前已接入本地 Room 存储，后续可替换为正式告警库",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = { onIntent(MainIntent.ClearHistory) }) {
                    Text("清空")
                }
            }
        }

        items(uiState.logs, key = { it.id }) { entry ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
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
                title = "报警通知预留",
                description = "结构已留，后续接入本地通知/推送",
                checked = uiState.preferences.notificationsEnabled,
                onCheckedChange = { onIntent(MainIntent.SetNotifications(it)) },
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
                        text = "这版安卓端优先做移动监控和关键控制，不强行复刻桌面 Qt 的所有图表。因为任务书明确写的是 ESP8266 + TCP，上位机直连小车时先走 Socket 会更合适，等你们后面有云端再补 Retrofit/OkHttp 网关。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "当前假数据已经覆盖 RFID 点位识别、超声波防撞停车、低电返航和回桩确认，后面你只要把真实报文字段对上这些状态即可。",
                        style = MaterialTheme.typography.bodyMedium,
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

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
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
private fun ChecklistLine(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(999.dp),
                )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CommandChip(
    command: VehicleCommand,
    onClick: () -> Unit,
) {
    val containerColor = when (command.emphasis) {
        CommandEmphasis.Primary -> MaterialTheme.colorScheme.primaryContainer
        CommandEmphasis.Accent -> MaterialTheme.colorScheme.tertiaryContainer
        CommandEmphasis.Danger -> MaterialTheme.colorScheme.errorContainer
        CommandEmphasis.Neutral -> MaterialTheme.colorScheme.surfaceVariant
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(
            text = command.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
