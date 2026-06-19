# AGENTS.md — Android_Car

## Build & Run

```bash
./gradlew assembleDebug        # single-module app
```

- **minSdk 29** / **targetSdk 36** / Kotlin 2.3.21 / AGP 8.13.2
- No lint config — standard `kotlin.code.style=official`
- No tests found in repo

## Architecture

Single-module Android app, package `com.carhost.mobile`.

```
CarHostApplication (@HiltAndroidApp)
  → MainActivity (@AndroidEntryPoint, ComponentActivity, enableEdgeToEdge)
    → CarHostApp (root Composable)
      → 5 tabs: Overview / Monitor / Control / History / Settings
        → MainViewModel (@HiltViewModel, single MVI viewmodel)
```

**MVVM + MVI**: all user actions → `MainIntent` sealed interface → `MainViewModel.dispatch()` → state in `MainUiState` data class. One ViewModel for the entire app.

**DI**: Hilt. `AppModule.kt` binds `VehicleRepository → TcpVehicleRepository` (always — no debug toggle). Room DB `AppDatabase` with `fallbackToDestructiveMigration()`. DataStore file: `operator_preferences.preferences_pb`.

## Key Mechanics

| Concern | Implementation |
|---|---|
| State | Single `MainUiState` (data class) built via `combine()` in ViewModel |
| Communication | TCP Socket → JSON lines, default `192.168.4.1:8080` |
| Real data | `TcpVehicleRepository` — binds socket to WiFi via `ConnectivityManager` |
| Sim data | `FakeVehicleRepository` — not wired in DI (only for reference) |
| DB | Room, table `log_records`, max 200 rows (trimmed on insert) |
| Charts | Canvas self-drawn (`LineChartCard.kt`), max 24 samples rolling |
| Permissions | `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS` |
| Themes | 5 dark-only palettes + contrast slider; `useDynamicColor` for Android 12+ |
| Default tab | **Control** (not Overview) |

## TCP Protocol

- **Receive**: JSON lines — fields `MQ8`, `AHT_temp`, `AHT_hum`, `MLX_obj`, `MLX_amb`, `dist`, `bat`, `rfid`, `rfid_loc`, `track`, `track_bin`, `state`. Type field: `telemetry` / `alert` / `rfid` / `ack`.
- **Fallback field names**: `updateTelemetry()` also accepts `h2` (=MQ8), `ATH_temp`/`temp` (=AHT_temp), `cab` (=MLX_obj), `hum` (=AHT_hum).
- **Send**: plain wire strings: `start`, `idle`, `evacuate`, `temp_warning`, `temp_alarm`, `return_home`.
- **Gas normalization**: `normalizeGas()` handles 3 ranges — ≤1 (raw), ≤100 (/100), else (/4095).

## Auto‑Recovery

After `Warning`/`Alarm`/`Evacuating` state, when both `MLX_obj < 60°C` and `gasPercent < 0.45` persist for **5 seconds**, `StartPatrol` is sent automatically. Canceled if condition breaks.

## Gotchas

- Room uses **destructive migration** (`fallbackToDestructiveMigration()`) — DB version bump drops data.
- `AlertNotifier.showAlert()` silently ignores if `POST_NOTIFICATIONS` not granted (no crash).
- Selected tab is **in-memory only** — resets to Control on process death.
- `PortDraft` input filtered to digits only, max 5 chars (`MainIntent.UpdatePortDraft`).
- All chart/track histories keep **max 24 entries** (rolling window).
