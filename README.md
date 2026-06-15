# Android Car Host MVP

这个目录是给无人工厂巡检小车准备的安卓上位机骨架，目标不是复刻桌面 `Qt_Car`，而是先落一版适合手机使用的移动控制台：

- `总览`：状态、电量、延迟、当前工位、报警摘要
- `监控`：温度曲线、气体浓度、电池、速度、避障距离
- `控制`：连接管理、返航、急停、暂停、手动复位
- `历史`：报警/事件/指令记录
- `设置`：动态取色、常亮、通知开关、连接参数
- `RFID / 防撞 / 返航`：已经接入假数据状态流，可直接替换为真实报文

## 为什么这里没有先上 Retrofit

任务书和现有 Qt 上位机都明确指向 `ESP8266 + TCP 协议 + JSON 行消息`。这类“手机直接连车”的场景更适合先做原生 TCP 通道，而不是强行套 REST API。

所以这版骨架采取了两层设计：

- `VehicleRepository`：对上暴露统一状态流和控制命令
- `FakeVehicleRepository`：先用本地仿真把 UI、状态流和历史记录跑起来

后面接真实硬件时，你只需要新增一个真实的 `TcpVehicleRepository` 或 `VehicleSocketDataSource`，把当前 Qt 的字段映射接进去即可。

## 与当前 Qt 上位机对齐的关键点

- 默认主机和端口沿用了 Qt 版本：`192.168.4.1:8899`
- 命令字保持一致：
  - `start_patrol`
  - `pause`
  - `evacuate`
  - `emergency_stop`
  - `manual_reset`
  - `return_home`
- 遥测字段也按 Qt 现有模型收敛：
  - `state`
  - `position`
  - `temperature`
  - `gas`
  - `battery`
  - `speed`
  - `latencyMs`
- 为任务书扩展的安卓端状态位：
  - `rfidTag`
  - `locationDescription`
  - `obstacleDistanceCm`
  - `obstacleDetected`
  - `returnReason`
  - `homeDockReached`

## 下一步怎么接真实小车

1. 先确认 STM32/ESP8266 当前上报 JSON 的实际格式。
2. 把 `FakeVehicleRepository` 替换成真实 TCP 数据源。
3. 在连接层补上断线重连、心跳和 ACK 超时。
4. 如果你们后面加云端中转，再补 Retrofit/OkHttp 的 REST 网关层。

## 当前限制

- 这个工作区里没有 Android SDK / Gradle Wrapper，所以我先把源码和构建脚本补齐了，但没有在本机完成 APK 构建验证。
- 图片加载和推送通知还没接真实能力，先预留了结构位。
