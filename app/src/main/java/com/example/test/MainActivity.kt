package com.example.test

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.*

import androidx.compose.runtime.getValue
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.github.looxidlabs.sdkandroid.*

class MainActivity : ComponentActivity() {
    private lateinit var sdk: LinkBandSdk
    
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdk = LinkBandSdk(this)
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
                                val scannedDevices by sdk.scannedDevices.collectAsState(initial = emptyList())
                                val isScanning by sdk.isScanning.collectAsState(initial = false)
                                val isConnected by sdk.isConnected.collectAsState(initial = false)
                                val isAutoReconnectEnabled by sdk.isAutoReconnectEnabled.collectAsState(initial = false)
                                
                                ScanScreen(
                                    scannedDevices = scannedDevices,
                                    isScanning = isScanning,
                                    isConnected = isConnected,
                                    isAutoReconnectEnabled = isAutoReconnectEnabled,
                                    onStartScan = { sdk.startScan() },
                                    onStopScan = { sdk.stopScan() },
                                    onConnect = { device -> sdk.connectToDevice(device) },
                                    onNavigateToData = { 
                                        navController.navigate("data") {
                                            popUpTo("scan") { inclusive = true }
                                        }
                                    },
                                    onEnableAutoReconnect = { sdk.enableAutoReconnect() },
                                    onDisableAutoReconnect = { sdk.disableAutoReconnect() },
                                    navController = navController
                                )
                            }
                            
                            composable("data") {
                                val eegData by sdk.eegData.collectAsState(initial = emptyList())
                                val ppgData by sdk.ppgData.collectAsState(initial = emptyList())
                                val accData by sdk.accData.collectAsState(initial = emptyList())
                                val batteryData by sdk.batteryData.collectAsState(initial = null)
                                val isConnected by sdk.isConnected.collectAsState(initial = false)
                                val isEegStarted by sdk.isEegStarted.collectAsState(initial = false)
                                val isPpgStarted by sdk.isPpgStarted.collectAsState(initial = false)
                                val isAccStarted by sdk.isAccStarted.collectAsState(initial = false)
                                val selectedSensors by sdk.selectedSensors.collectAsState(initial = emptySet())
                                val isReceivingData by sdk.isReceivingData.collectAsState(initial = false)
                                val isRecording by sdk.isRecording.collectAsState(initial = false)
                                val isAutoReconnectEnabled by sdk.isAutoReconnectEnabled.collectAsState(initial = false)
                                val connectedDeviceName by sdk.connectedDeviceName.collectAsState(initial = null)
                                val accelerometerMode by sdk.accelerometerMode.collectAsState(initial = AccelerometerMode.RAW)
                                val processedAccData by sdk.processedAccData.collectAsState(initial = emptyList())
                                // 배치 모니터링 관련 상태들 추가
                                val selectedCollectionMode by sdk.selectedCollectionMode.collectAsState(initial = CollectionMode.SAMPLE_COUNT)
                                
                                DataScreen(
                                    eegData = eegData,
                                    ppgData = ppgData,
                                    accData = accData,
                                    batteryData = batteryData,
                                    isConnected = isConnected,
                                    isEegStarted = isEegStarted,
                                    isPpgStarted = isPpgStarted,
                                    isAccStarted = isAccStarted,
                                    selectedSensors = selectedSensors,
                                    isReceivingData = isReceivingData,
                                    isRecording = isRecording,
                                    isAutoReconnectEnabled = isAutoReconnectEnabled,
                                    connectedDeviceName = connectedDeviceName,
                                    accelerometerMode = accelerometerMode,
                                    processedAccData = processedAccData,
                                    // 배치 모니터링 관련 매개변수들 추가
                                    selectedCollectionMode = selectedCollectionMode,
                                    getSensorConfiguration = { sensorType ->
                                        sdk.getSensorConfiguration(sensorType)
                                    },
                                    onDisconnect = { sdk.disconnect() },
                                    onNavigateToScan = { 
                                        navController.navigate("scan") {
                                            popUpTo("data") { inclusive = true }
                                        }
                                    },
                                    onSelectSensor = { sensor -> sdk.selectSensor(sensor) },
                                    onDeselectSensor = { sensor -> sdk.deselectSensor(sensor) },
                                    onStartSelectedSensors = { sdk.startSelectedSensors() },
                                    onStopSelectedSensors = { sdk.stopSelectedSensors() },
                                    onStartRecording = { sdk.startRecording() },
                                    onStopRecording = { sdk.stopRecording() },
                                    onShowFileList = { navController.navigate("files") },
                                    onToggleAutoReconnect = { 
                                        if (isAutoReconnectEnabled) {
                                            sdk.disableAutoReconnect()
                                        } else {
                                            sdk.enableAutoReconnect()
                                        }
                                    },
                                    onSetAccelerometerMode = { mode -> sdk.setAccelerometerMode(mode) },
                                    // 배치 모니터링 콜백 함수들 추가
                                    onCollectionModeChange = { mode ->
                                        sdk.setCollectionMode(mode)
                                    },
                                    onSampleCountChange = { sensorType, count, text ->
                                        sdk.updateSensorSampleCount(sensorType, count, text)
                                    },
                                    onSecondsChange = { sensorType, seconds, text ->
                                        sdk.updateSensorSeconds(sensorType, seconds, text)
                                    },
                                    onMinutesChange = { sensorType, minutes, text ->
                                        sdk.updateSensorMinutes(sensorType, minutes, text)
                                    },
                                    navController = navController
                                )
                            }
                            
                            composable("files") {
                                FileListScreen(
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onFileClick = { file ->
                                        // 파일 경로를 URL 인코딩하여 네비게이션에 전달
                                        val encodedPath = java.net.URLEncoder.encode(file.absolutePath, "UTF-8")
                                        navController.navigate("csvViewer/$encodedPath")
                                    }
                                )
                            }
                            
                            composable("csvViewer/{filePath}") { backStackEntry ->
                                val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
                                val filePath = java.net.URLDecoder.decode(encodedPath, "UTF-8")
                                val file = java.io.File(filePath)
                                
                                CsvViewerScreen(
                                    file = file,
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::sdk.isInitialized) {
            sdk.cleanup()
        }
    }
}