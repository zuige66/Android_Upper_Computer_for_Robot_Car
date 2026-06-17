# Android Upper Computer for Robot Car

无人工厂巡检小车的 Android 上位机应用，通过 TCP 直连 ESP32/ESP8266，实时接收遥测数据、发送控制命令。

## 功能概览

### 总览
- 实时显示车辆状态、连接链路、电池余量、环境温湿度
- RFID 路线映射（A1 → A2 → B2 → B3 → …）
- 防撞状态、红外目标温度、MQ8 气体浓度
- 自动告警等级判定（预警 / 告警 / 紧急撤离）

### 监控
- **内置图表**：温度、气体、电池、速度、MLX 红外、AHT 温湿度、MQ8、循迹通道
- **自定义图表**：输入任意 JSON 字段路径即可绘图，支持编辑/删除/隐藏
- **循迹二值图**：四路循迹通道（L1/L2/R1/R2）时间序列可视化
- **RFID 流程图**：水平滚动的位置节点流，当前位置高亮
- Y 轴固定、自动滚动到最新数据、坐标轴箭头

### 控制
- 6 个默认快捷按钮：开始巡检、待机任务、紧急撤离、温度预警、温度警告、返航回桩
- 自定义快捷按钮：添加 / 编辑 / 删除 / 恢复默认
- 长按编辑按钮名称和发送字段
- 自定义 JSON 发送（设置页）

### 历史
- 事件/报警/命令日志，Room 数据库持久化
- 支持清除历史

### 设置
- 动态取色（Android 12+ Material You）
- 巡检页常亮
- 报警通知开关（POST_NOTIFICATIONS 权限请求）
- JSON 数据查看器（最近接收 RX / 发送 TX）
- 自定义 JSON 发送

## 通信协议

TCP 连接，JSON 行消息格式，默认地址 `192.168.4.1:8080`。

### 接收（小车 → 上位机）

```json
{
  "type": "telemetry",
  "MQ8": 120,
  "AHT_temp": 28.5,
  "AHT_hum": 65.2,
  "MLX_obj": 45.0,
  "MLX_amb": 26.3,
  "dist": 50,
  "bat": 85,
  "rfid": 3,
  "rfid_loc": "A2",
  "track": 6,
  "track_bin": "0110",
  "state": "patrol"
}
```

| 字段 | 说明 |
|------|------|
| `type` | `telemetry` / `alert` / `rfid` / `ack` |
| `MQ8` | MQ8 气体传感器原始值 |
| `AHT_temp` | AHT20 温度 (°C) |
| `AHT_hum` | AHT20 湿度 (%) |
| `MLX_obj` | MLX90614 红外目标温度 (°C) |
| `MLX_amb` | MLX90614 环境温度 (°C) |
| `dist` | 超声波避障距离 (cm) |
| `bat` | 电池电量 (0-100) |
| `rfid` | RFID 标签编号 |
| `rfid_loc` | RFID 位置名称 |
| `track` | 循迹值 (0-15) |
| `track_bin` | 循迹二值字符串 (如 "0110") |
| `state` | 车辆状态 |

### 发送（上位机 → 小车）

| 命令 | wire 值 | 说明 |
|------|---------|------|
| 开始巡检 | `start_patrol` | 进入巡检模式 |
| 待机任务 | `idle` | 待机状态 |
| 紧急撤离 | `evacuate` | 紧急撤离 |
| 温度预警 | `temp_warning` | 触发温度预警 |
| 温度警告 | `temp_alarm` | 触发温度警告 |
| 返航回桩 | `return_home` | 返回充电桩 |

## 技术栈

| 组件 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + MVI (Sealed Intent) |
| DI | Hilt |
| 数据库 | Room (日志持久化) |
| 偏好存储 | DataStore Preferences |
| 异步 | Kotlin Coroutines + Flow |
| 通信 | 原生 TCP Socket |
| 图表 | Canvas 自绘折线图 |
| 通知 | Android NotificationManager |

## 项目结构

```
app/src/main/java/com/carhost/mobile/
├── CarHostApplication.kt          # Hilt Application
├── MainActivity.kt                # 入口 Activity，权限请求
├── data/
│   ├── local/
│   │   ├── PreferencesRepository.kt  # DataStore 偏好读写
│   │   └── db/
│   │       ├── AppDatabase.kt        # Room 数据库
│   │       ├── LogRecordDao.kt       # 日志 DAO
│   │       └── LogRecordEntity.kt    # 日志实体
│   ├── model/
│   │   └── Models.kt                 # 数据模型、枚举、状态
│   ├── notify/
│   │   └── AlertNotifier.kt          # 通知管理
│   └── repository/
│       ├── VehicleRepository.kt      # Repository 接口
│       ├── TcpVehicleRepository.kt   # TCP 真实实现
│       └── FakeVehicleRepository.kt  # 仿真数据（调试用）
├── di/
│   └── AppModule.kt                  # Hilt 模块
└── ui/
    ├── CarHostApp.kt                 # 全部页面 Composable
    ├── MainIntent.kt                 # MVI Intent 定义
    ├── MainUiState.kt                # UI 状态定义
    ├── MainViewModel.kt              # ViewModel 状态管理
    ├── components/
    │   ├── LineChartCard.kt          # Canvas 折线图
    │   ├── RfidFlowCard.kt           # RFID 位置流程图
    │   └── TrackBinaryCard.kt        # 循迹二值图
    └── theme/
        └── Theme.kt                  # Material 3 主题
```

## 告警等级

| 等级 | 触发条件 |
|------|----------|
| 预警 | MLX ≥ 60°C 或 MQ8 ≥ 45% |
| 告警 | MLX ≥ 70°C 或 MQ8 ≥ 65% |
| 紧急 | MLX > 80°C 或 MQ8 ≥ 80% |

告警状态下自动进入撤离模式，温度恢复正常 5 秒后自动返回巡检。

## 构建

```bash
./gradlew assembleDebug
```

- minSdk 29 (Android 10)
- targetSdk 36
- Kotlin + Compose Compiler

## UI 风格

统一 Material 3 深色主题，所有页面使用 `surfaceContainerLow` 灰色胶囊卡片，适配深色模式蓝灰色调。
