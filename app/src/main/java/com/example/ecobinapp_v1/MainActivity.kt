package com.example.ecobinapp_v1

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecobinapp_v1.ui.AddBinScreen
import com.example.ecobinapp_v1.ui.BinDetailScreen
import com.example.ecobinapp_v1.ui.BinListScreen
import com.example.ecobinapp_v1.ui.theme.EcobinApp_v1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcobinApp_v1Theme {
                EcoBinApp()
            }
        }
    }
}

private sealed interface Screen {
    data object List : Screen
    data object AddBin : Screen
    data class Detail(val binId: String) : Screen
}

@Composable
fun EcoBinApp(vm: BinViewModel = viewModel()) {
    val context = LocalContext.current
    var screen by remember { mutableStateOf<Screen>(Screen.List) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val btEnableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val action = pendingAction
        pendingAction = null
        if (vm.ble.isBluetoothEnabled()) action?.invoke()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        val action = pendingAction
        pendingAction = null
        if (granted && action != null) {
            if (vm.ble.demoMode.value || vm.ble.isBluetoothEnabled()) {
                action()
            } else {
                pendingAction = action
                btEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    // Runs [action] once BLE prerequisites (permissions + Bluetooth on) are satisfied.
    fun runWithBle(action: () -> Unit) {
        if (vm.ble.demoMode.value) {
            action()
            return
        }
        val needed = vm.ble.requiredPermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            pendingAction = action
            permissionLauncher.launch(needed.toTypedArray())
            return
        }
        if (!vm.ble.isBluetoothEnabled()) {
            pendingAction = action
            btEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }
        action()
    }

    when (val s = screen) {
        is Screen.List -> BinListScreen(
            vm = vm,
            onAddBin = { screen = Screen.AddBin },
            onOpenBin = { screen = Screen.Detail(it.id) }
        )

        is Screen.AddBin -> AddBinScreen(
            vm = vm,
            onScanRequested = { runWithBle { vm.startScan() } },
            onRegistered = { bin -> screen = Screen.Detail(bin.id) },
            onBack = { screen = Screen.List }
        )

        is Screen.Detail -> BinDetailScreen(
            vm = vm,
            binId = s.binId,
            onConnect = {
                val bin = vm.binById(s.binId)
                if (bin != null) runWithBle { vm.connectToBin(bin) }
            },
            onBack = { screen = Screen.List }
        )
    }
}
