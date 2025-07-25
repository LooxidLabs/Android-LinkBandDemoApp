# Android-LinkBandDemoApp - 코드 예시

LooxidLabs LinkBand 디바이스와의 Bluetooth 연결 및 센서 데이터 수집을 시연하는 Android 데모 앱의 코드 예시입니다.

## SDK Import 설정

LinkBand SDK를 사용하기 위해서는 다음과 같이 import하면 됩니다:

```kotlin
// LinkBand SDK import
import com.example.linkbandsdk.*
```

또는 필요한 클래스만 개별적으로 import:

```kotlin
import com.example.linkbandsdk.BleManager
import com.example.linkbandsdk.SensorType
import com.example.linkbandsdk.EegData
import com.example.linkbandsdk.PpgData
import com.example.linkbandsdk.AccData
import com.example.linkbandsdk.BatteryData
import com.example.linkbandsdk.AccelerometerMode
import com.example.linkbandsdk.CollectionMode
```

## 기본 설정 - 코드 예시

### MainActivity.kt 파일 설정

```kotlin
package com.example.test

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.CsvViewerScreen
import com.example.test.ui.DataScreen
import com.example.test.ui.FileListScreen
import com.example.test.ui.ScanScreen
import com.example.test.ui.theme.TestTheme
import com.example.test.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
// LinkBand SDK import
import com.example.linkbandsdk.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                val navController = rememberNavController()
                
                // BLE 권한 요청
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    listOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } else {
                    listOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
                
                val permissionState = rememberMultiplePermissionsState(permissions)
                
                LaunchedEffect(permissionState.allPermissionsGranted) {
                    if (!permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }
                
                if (permissionState.allPermissionsGranted) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "scan",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("scan") {
                                val scannedDevices by viewModel.scannedDevices.collectAsState(initial = emptyList())
                                val isScanning by viewModel.isScanning.collectAsState(initial = false)
                                val isConnected by viewModel.isConnected.collectAsState(initial = false)
                                val isAutoReconnectEnabled by viewModel.isAutoReconnectEnabled.collectAsState(initial = false)
                                
                                ScanScreen(
                                    scannedDevices = scannedDevices,
                                    isScanning = isScanning,
                                    isConnected = isConnected,
                                    isAutoReconnectEnabled = isAutoReconnectEnabled,
                                    onStartScan = { viewModel.startScan() },
                                    onStopScan = { viewModel.stopScan() },
                                    onConnect = { device -> viewModel.connectToDevice(device) },
                                    onNavigateToData = { 
                                        navController.navigate("data") {
                                            popUpTo("scan") { inclusive = true }
                                        }
                                    },
                                    onEnableAutoReconnect = { viewModel.enableAutoReconnect() },
                                    onDisableAutoReconnect = { viewModel.disableAutoReconnect() },
                                    navController = navController
                                )
                            }
                            
                            composable("data") {
                                // DataScreen 관련 상태들
                                val eegData by viewModel.eegData.collectAsState(initial = emptyList())
                                val ppgData by viewModel.ppgData.collectAsState(initial = emptyList())
                                val accData by viewModel.accData.collectAsState(initial = emptyList())
                                val batteryData by viewModel.batteryData.collectAsState(initial = null)
                                val isConnected by viewModel.isConnected.collectAsState(initial = false)
                                val selectedSensors by viewModel.selectedSensors.collectAsState(initial = emptySet())
                                val isReceivingData by viewModel.isReceivingData.collectAsState(initial = false)
                                val isRecording by viewModel.isRecording.collectAsState(initial = false)
                                
                                DataScreen(
                                    eegData = eegData,
                                    ppgData = ppgData,
                                    accData = accData,
                                    batteryData = batteryData,
                                    isConnected = isConnected,
                                    isEegStarted = viewModel.isEegStarted.collectAsState(initial = false).value,
                                    isPpgStarted = viewModel.isPpgStarted.collectAsState(initial = false).value,
                                    isAccStarted = viewModel.isAccStarted.collectAsState(initial = false).value,
                                    selectedSensors = selectedSensors,
                                    isReceivingData = isReceivingData,
                                    isRecording = isRecording,
                                    isAutoReconnectEnabled = viewModel.isAutoReconnectEnabled.collectAsState(initial = false).value,
                                    connectedDeviceName = viewModel.connectedDeviceName.collectAsState(initial = null).value,
                                    accelerometerMode = viewModel.accelerometerMode.collectAsState(initial = AccelerometerMode.OFF).value,
                                    processedAccData = viewModel.processedAccData.collectAsState(initial = emptyList()).value,
                                    selectedCollectionMode = viewModel.selectedCollectionMode.collectAsState(initial = CollectionMode.REAL_TIME).value,
                                    getSensorConfiguration = { sensor -> viewModel.getSensorConfiguration(sensor) },
                                    onDisconnect = { viewModel.disconnect() },
                                    onNavigateToScan = { navController.navigate("scan") { popUpTo("data") { inclusive = true } } },
                                    onSelectSensor = { sensor -> viewModel.selectSensor(sensor) },
                                    onDeselectSensor = { sensor -> viewModel.deselectSensor(sensor) },
                                    onStartSelectedSensors = { viewModel.startSelectedSensors() },
                                    onStopSelectedSensors = { viewModel.stopSelectedSensors() },
                                    onStartRecording = { viewModel.startRecording() },
                                    onStopRecording = { viewModel.stopRecording() },
                                    onShowFileList = { navController.navigate("csvViewer") { popUpTo("data") { inclusive = true } } },
                                    onToggleAutoReconnect = { viewModel.toggleAutoReconnect() },
                                    onSetAccelerometerMode = { mode -> viewModel.setAccelerometerMode(mode) },
                                    onCollectionModeChange = { mode -> viewModel.setCollectionMode(mode) },
                                    onSampleCountChange = { sensor, count, unit -> viewModel.setSampleCount(sensor, count, unit) },
                                    onSecondsChange = { sensor, seconds, unit -> viewModel.setSeconds(sensor, seconds, unit) },
                                    onMinutesChange = { sensor, minutes, unit -> viewModel.setMinutes(sensor, minutes, unit) },
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

## 권한 설정

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## 완전한 구현 코드

### 1. 링크밴드 디바이스 Bluetooth 스캔

```kotlin
// MainViewModel.kt - Bluetooth 스캔 관련 함수들
package com.example.test.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkbandsdk.BleManager
import com.example.linkbandsdk.SensorType
import com.example.linkbandsdk.AccData
import com.example.linkbandsdk.BatteryData
import com.example.linkbandsdk.EegData
import com.example.linkbandsdk.PpgData
import com.example.linkbandsdk.AccelerometerMode
import com.example.linkbandsdk.ProcessedAccData
import com.example.linkbandsdk.CollectionMode
import com.example.linkbandsdk.SensorBatchConfiguration
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val bleManager = BleManager(application)
    
    // 스캔 관련 StateFlow
    val scannedDevices: StateFlow<List<BluetoothDevice>> = bleManager.scannedDevices
    val isScanning: StateFlow<Boolean> = bleManager.isScanning
    
    // 스캔 시작
    fun startScan() {
        viewModelScope.launch {
            bleManager.startScan()
        }
    }
    
    // 스캔 중지
    fun stopScan() {
        viewModelScope.launch {
            bleManager.stopScan()
        }
    }
    
    // 기타 필요한 StateFlow들
    val isConnected: StateFlow<Boolean> = bleManager.isConnected
    val connectedDeviceName: StateFlow<String?> = bleManager.connectedDeviceName
    val isAutoReconnectEnabled: StateFlow<Boolean> = bleManager.isAutoReconnectEnabled
    val eegData: StateFlow<List<EegData>> = bleManager.eegData
    val ppgData: StateFlow<List<PpgData>> = bleManager.ppgData
    val accData: StateFlow<List<AccData>> = bleManager.accData
    val batteryData: StateFlow<BatteryData?> = bleManager.batteryData
    val selectedSensors: StateFlow<Set<SensorType>> = bleManager.selectedSensors
    val isReceivingData: StateFlow<Boolean> = bleManager.isReceivingData
    val isRecording: StateFlow<Boolean> = bleManager.isRecording
    
    // 기타 필요한 함수들...
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            bleManager.connectToDevice(device)
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            bleManager.disconnect()
        }
    }
    
    fun enableAutoReconnect() {
        viewModelScope.launch {
            bleManager.enableAutoReconnect()
        }
    }
    
    fun disableAutoReconnect() {
        viewModelScope.launch {
            bleManager.disableAutoReconnect()
        }
    }
    
    fun selectSensor(sensor: SensorType) {
        viewModelScope.launch {
            bleManager.selectSensor(sensor)
        }
    }
    
    fun deselectSensor(sensor: SensorType) {
        viewModelScope.launch {
            bleManager.deselectSensor(sensor)
        }
    }
    
    fun startSelectedSensors() {
        Log.d("MainViewModel", "선택된 센서들 시작 요청")
        viewModelScope.launch {
            bleManager.startSelectedSensors()
        }
    }
    
    fun stopSelectedSensors() {
        Log.d("MainViewModel", "선택된 센서들 중지 요청")
        viewModelScope.launch {
            bleManager.stopSelectedSensors()
        }
    }
    
    fun startRecording() {
        Log.d("MainViewModel", "CSV 기록 시작 요청")
        viewModelScope.launch {
            bleManager.startRecording()
        }
    }
    
    fun stopRecording() {
        Log.d("MainViewModel", "CSV 기록 중지 요청")
        viewModelScope.launch {
            bleManager.stopRecording()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        bleManager.disconnect()
    }
}
```

### 2. 스캔된 링크밴드 디바이스 목록 표시 및 연결

```kotlin
// ScanScreen.kt - 디바이스 목록 표시
package com.example.test.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    scannedDevices: List<BluetoothDevice>,
    isScanning: Boolean,
    isConnected: Boolean,
    isAutoReconnectEnabled: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (BluetoothDevice) -> Unit,
    onNavigateToData: () -> Unit,
    onEnableAutoReconnect: () -> Unit,
    onDisableAutoReconnect: () -> Unit,
    navController: NavController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var hasNavigatedToData by remember { mutableStateOf(false) }
    LaunchedEffect(isConnected, currentRoute) {
        if (isConnected && currentRoute == "scan") {
            onNavigateToData()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 제목
        Text(
            text = "LinkBand 블루투스 스캐너",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        // 자동연결 토글
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "자동 재연결",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isAutoReconnectEnabled) "연결이 끊어지면 자동으로 재연결됩니다" else "수동으로 재연결해야 합니다",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAutoReconnectEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            onEnableAutoReconnect()
                        } else {
                            onDisableAutoReconnect()
                        }
                    }
                )
            }
        }
        
        // 스캔 버튼
        Button(
            onClick = if (isScanning) onStopScan else onStartScan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isScanning) "스캔 중지" else "스캔 시작")
        }
        
        // 스캔 상태
        if (isScanning) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Text("LXB- 디바이스를 검색 중...")
            }
        }
        
        // 디바이스 목록
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "발견된 디바이스 (${scannedDevices.size}개)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (scannedDevices.isEmpty()) {
                    Text(
                        text = "디바이스를 찾을 수 없습니다. 스캔을 시작하여 LXB- 디바이스를 찾아보세요.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(scannedDevices) { device ->
                            DeviceItem(
                                device = device,
                                isScanning = isScanning,
                                onConnect = { onConnect(device) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isScanning: Boolean,
    onConnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name ?: "알 수 없는 디바이스",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = device.address,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            Button(
                onClick = onConnect,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("연결")
            }
        }
    }
}
```

### 3. 블루투스 연결 상태 확인 및 연결 해제

```kotlin
// MainViewModel.kt - 연결 상태 관리 (위의 MainViewModel.kt에 포함됨)
// 연결 상태 관련 StateFlow들
val isConnected: StateFlow<Boolean> = bleManager.isConnected
val connectedDeviceName: StateFlow<String?> = bleManager.connectedDeviceName
val isAutoReconnectEnabled: StateFlow<Boolean> = bleManager.isAutoReconnectEnabled

// 디바이스 연결
fun connectToDevice(device: BluetoothDevice) {
    viewModelScope.launch {
        bleManager.connectToDevice(device)
    }
}

// 연결 해제
fun disconnect() {
    viewModelScope.launch {
        bleManager.disconnect()
    }
}

// 자동 재연결 설정
fun enableAutoReconnect() {
    viewModelScope.launch {
        bleManager.enableAutoReconnect()
    }
}

fun disableAutoReconnect() {
    viewModelScope.launch {
        bleManager.disableAutoReconnect()
    }
}
```

### 4. 센서 활성화 후, 수신 데이터를 콘솔에 실시간 출력

```kotlin
// MainViewModel.kt - 센서 데이터 관리 (위의 MainViewModel.kt에 포함됨)
// 센서 데이터 StateFlow (실시간 콘솔 출력은 SDK 내부에서 처리)
val eegData: StateFlow<List<EegData>> = bleManager.eegData
val ppgData: StateFlow<List<PpgData>> = bleManager.ppgData
val accData: StateFlow<List<AccData>> = bleManager.accData
val batteryData: StateFlow<BatteryData?> = bleManager.batteryData

// 센서 선택 상태
val selectedSensors: StateFlow<Set<SensorType>> = bleManager.selectedSensors
val isReceivingData: StateFlow<Boolean> = bleManager.isReceivingData

// 센서 선택/해제
fun selectSensor(sensor: SensorType) {
    viewModelScope.launch {
        bleManager.selectSensor(sensor)
    }
}

fun deselectSensor(sensor: SensorType) {
    viewModelScope.launch {
        bleManager.deselectSensor(sensor)
    }
}

// 선택된 센서들 시작/중지
fun startSelectedSensors() {
    Log.d("MainViewModel", "선택된 센서들 시작 요청")
    viewModelScope.launch {
        bleManager.startSelectedSensors()
    }
}

fun stopSelectedSensors() {
    Log.d("MainViewModel", "선택된 센서들 중지 요청")
    viewModelScope.launch {
        bleManager.stopSelectedSensors()
    }
}
```

### 5. 수신된 센서 데이터를 카드 형태로 앱 인터페이스에 실시간 출력

```kotlin
// DataScreen.kt - 센서 데이터 실시간 표시
package com.example.test.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.linkbandsdk.SensorType
import com.example.linkbandsdk.AccData
import com.example.linkbandsdk.BatteryData
import com.example.linkbandsdk.EegData
import com.example.linkbandsdk.PpgData
import com.example.linkbandsdk.AccelerometerMode
import com.example.linkbandsdk.ProcessedAccData
import com.example.linkbandsdk.CollectionMode
import com.example.linkbandsdk.SensorBatchConfiguration
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ArrowForward
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    eegData: List<EegData>,
    ppgData: List<PpgData>,
    accData: List<AccData>,
    batteryData: BatteryData?,
    isConnected: Boolean,
    isEegStarted: Boolean,
    isPpgStarted: Boolean, 
    isAccStarted: Boolean,
    selectedSensors: Set<SensorType>,
    isReceivingData: Boolean,
    isRecording: Boolean,
    isAutoReconnectEnabled: Boolean,
    connectedDeviceName: String?,
    accelerometerMode: AccelerometerMode,
    processedAccData: List<ProcessedAccData>,
    selectedCollectionMode: CollectionMode,
    getSensorConfiguration: (SensorType) -> SensorBatchConfiguration?,
    onDisconnect: () -> Unit,
    onNavigateToScan: () -> Unit,
    onSelectSensor: (SensorType) -> Unit,
    onDeselectSensor: (SensorType) -> Unit,
    onStartSelectedSensors: () -> Unit,
    onStopSelectedSensors: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onShowFileList: () -> Unit,
    onToggleAutoReconnect: () -> Unit,
    onSetAccelerometerMode: (AccelerometerMode) -> Unit,
    onCollectionModeChange: (CollectionMode) -> Unit,
    onSampleCountChange: (SensorType, Int, String) -> Unit,
    onSecondsChange: (SensorType, Int, String) -> Unit,
    onMinutesChange: (SensorType, Int, String) -> Unit,
    navController: NavController
) {
    // 경고 다이얼로그 상태
    var showStopCollectionDialog by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    
    // 연결이 끊어지면 자동으로 스캔 화면으로 이동
    var hasNavigatedToScan by remember { mutableStateOf(false) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(isConnected, currentRoute) {
        if (!isConnected && currentRoute == "data") {
            onNavigateToScan()
        }
    }
    
    // 수집 시작 시점의 선택된 센서 스냅샷
    var startedSensors by remember { mutableStateOf<Set<SensorType>>(emptySet()) }
    
    // 수집 시작/중지 시점에 스냅샷 갱신
    LaunchedEffect(isReceivingData) {
        if (isReceivingData) {
            startedSensors = selectedSensors.toSet()
        } else {
            startedSensors = emptySet()
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LinkBand 데이터",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(
                        onClick = onShowFileList
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "저장된 파일 보기",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // 연결 상태
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(end = 8.dp)
                        ) {
                            // 연결 상태 인디케이터
                        }
                        Text(
                            text = if (isConnected) {
                                connectedDeviceName?.let { "연결됨: $it" } ?: "연결됨"
                            } else "연결 해제됨",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (isConnected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "샘플링 레이트 \n EEG 250Hz \n PPG 50Hz \n ACC 25Hz",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // 배터리 정보
        batteryData?.let { battery ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            battery.level > 50 -> MaterialTheme.colorScheme.primaryContainer
                            battery.level > 20 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "배터리",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${battery.level}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
        
        // EEG 데이터
        if (startedSensors.contains(SensorType.EEG)) {
            item {
                SensorDataCard(
                    title = "EEG 데이터",
                    content = {
                        if (eegData.isNotEmpty()) {
                            val latest = eegData.takeLast(3)
                            latest.forEach { data ->
                                Text(
                                    text = "timestamp: ${data.timestamp.time}, ch1uV: ${data.channel1.roundToInt()}µV, ch2uV: ${data.channel2.roundToInt()}µV, leadOff: ${if (data.leadOff) "1" else "0"}",
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                text = "EEG 데이터를 수신하지 못했습니다",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
        
        // PPG 데이터
        if (startedSensors.contains(SensorType.PPG)) {
            item {
                SensorDataCard(
                    title = "PPG 데이터",
                    content = {
                        if (ppgData.isNotEmpty()) {
                            val latest = ppgData.takeLast(3)
                            latest.forEach { data ->
                                Text(
                                    text = "timestamp: ${data.timestamp.time}, red: ${data.red}, ir: ${data.ir}",
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                text = "PPG 데이터를 수신하지 못했습니다",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
        
        // ACC 데이터
        if (startedSensors.contains(SensorType.ACC)) {
            item {
                SensorDataCard(
                    title = "ACC 데이터",
                    content = {
                        if (accData.isNotEmpty()) {
                            val latest = accData.takeLast(3)
                            latest.forEach { data ->
                                Text(
                                    text = "timestamp: ${data.timestamp.time}, x: ${data.x}, y: ${data.y}, z: ${data.z}",
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                text = "ACC 데이터를 수신하지 못했습니다",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SensorDataCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            content()
        }
    }
}
```

### 6. 데이터 기록 (CSV 저장) 구현

```kotlin
// MainViewModel.kt - CSV 기록 기능 (위의 MainViewModel.kt에 포함됨)
// CSV 기록 상태
val isRecording: StateFlow<Boolean> = bleManager.isRecording

// CSV 기록 시작
fun startRecording() {
    Log.d("MainViewModel", "CSV 기록 시작 요청")
    viewModelScope.launch {
        bleManager.startRecording()
    }
}

// CSV 기록 중지
fun stopRecording() {
    Log.d("MainViewModel", "CSV 기록 중지 요청")
    viewModelScope.launch {
        bleManager.stopRecording()
    }
}
```

이 코드 예시들은 Android에서 LinkBand 디바이스와의 Bluetooth 연결 및 센서 데이터 수집을 구현하는 실제 구조를 보여줍니다. 실제 구현에서는 BleManager 클래스가 SDK와의 실제 통신을 담당하며, 이 예시에서는 UI 레이어와 ViewModel 레이어의 구조를 중심으로 설명했습니다. 