package com.mashvpn.android.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mashvpn.android.data.models.VpnNode
import com.mashvpn.android.data.repository.VpnRepository
import com.mashvpn.android.vpn.MashVpnService
import com.mashvpn.android.vpn.PingTester
import com.mashvpn.android.vpn.VpnState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class ScreenState {
    object Splash : ScreenState()
    object LicenseEntry : ScreenState()
    object Home : ScreenState()
}

data class UiState(
    val currentScreen: ScreenState = ScreenState.Splash,
    val licenseKey: String = "",
    val customerName: String = "کاربر مش وی پی ان",
    val remainingDays: Int = 0,
    val expiresAt: String? = null,
    val maxDevices: Int = 1,
    val hwid: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val nodes: List<VpnNode> = emptyList(),
    val selectedNode: VpnNode? = null,
    val vpnState: VpnState = VpnState.Disconnected,
    val connectionDurationSeconds: Long = 0L,
    val isServersBottomSheetVisible: Boolean = false,
    val isSettingsDialogVisible: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application)
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var connectionTimerJob: Job? = null
    private var heartbeatJob: Job? = null

    init {
        _uiState.value = _uiState.value.copy(hwid = repository.hwid)

        // Observe VPN State
        MashVpnService.addStateListener { state ->
            _uiState.value = _uiState.value.copy(vpnState = state)
            when (state) {
                is VpnState.Connected -> startConnectionTimer()
                is VpnState.Disconnected -> stopConnectionTimer()
                is VpnState.Error -> {
                    stopConnectionTimer()
                    _uiState.value = _uiState.value.copy(errorMessage = state.message)
                }
                else -> {}
            }
        }

        checkSavedSession()
    }

    private fun checkSavedSession() {
        viewModelScope.launch {
            if (repository.prefs.isLoggedIn) {
                val key = repository.prefs.licenseKey.orEmpty()
                val name = repository.prefs.customerName ?: "کاربر مش وی پی ان"
                val days = repository.prefs.remainingDays

                _uiState.value = _uiState.value.copy(
                    currentScreen = ScreenState.Home,
                    licenseKey = key,
                    customerName = name,
                    remainingDays = days
                )

                loadServers()
                startHeartbeatLoop()
            } else {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenState.LicenseEntry)
            }
        }
    }

    fun activateLicense(inputKey: String) {
        if (inputKey.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "لطفاً کد لایسنس را وارد کنید.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.verifyAndActivateLicense(inputKey)

            if (result.isSuccess) {
                val res = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentScreen = ScreenState.Home,
                    licenseKey = inputKey.trim().uppercase(),
                    customerName = res.customerName ?: "کاربر مش وی پی ان",
                    remainingDays = res.remainingDays ?: 0,
                    expiresAt = res.expiresAt,
                    maxDevices = res.maxDevices ?: 1,
                    successMessage = "اشتراک با موفقیت فعال شد"
                )
                loadServers()
                startHeartbeatLoop()
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطا در بررسی لایسنس"
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = err)
            }
        }
    }

    fun loadServers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.fetchActiveNodes()
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                val lastId = repository.prefs.lastSelectedNodeId
                val defaultSelected = list.find { it.id == lastId } ?: list.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nodes = list,
                    selectedNode = defaultSelected
                )

                // Trigger background ping test
                refreshPing(list)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "خطا در بارگذاری سرورها"
                )
            }
        }
    }

    fun refreshPing(currentNodes: List<VpnNode> = _uiState.value.nodes) {
        viewModelScope.launch {
            val updated = PingTester.testNodesPing(currentNodes)
            val currSelected = _uiState.value.selectedNode
            val newSelected = updated.find { it.id == currSelected?.id } ?: currSelected

            _uiState.value = _uiState.value.copy(
                nodes = updated,
                selectedNode = newSelected
            )
        }
    }

    fun selectNode(node: VpnNode) {
        repository.prefs.lastSelectedNodeId = node.id
        _uiState.value = _uiState.value.copy(
            selectedNode = node,
            isServersBottomSheetVisible = false
        )
    }

    fun toggleVpnConnection(context: Context) {
        val state = _uiState.value.vpnState
        if (state is VpnState.Connected || state is VpnState.Connecting) {
            disconnectVpn(context)
        } else {
            connectVpn(context)
        }
    }

    private fun connectVpn(context: Context) {
        val selected = _uiState.value.selectedNode
        if (selected == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "لطفاً ابتدا یک سرور را انتخاب کنید.")
            return
        }

        val intent = Intent(context, MashVpnService::class.java).apply {
            action = MashVpnService.ACTION_CONNECT
            putExtra(MashVpnService.EXTRA_SERVER_NAME, selected.cleanTitle)
            putExtra(MashVpnService.EXTRA_RAW_CONFIG, selected.rawConfigString)
            putExtra(MashVpnService.EXTRA_HOST, selected.remoteHost)
            putExtra(MashVpnService.EXTRA_PORT, selected.remotePort)
            putExtra(MashVpnService.EXTRA_PROTO, selected.protocol)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun disconnectVpn(context: Context) {
        val intent = Intent(context, MashVpnService::class.java).apply {
            action = MashVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    private fun startConnectionTimer() {
        connectionTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(connectionDurationSeconds = 0L)
        connectionTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    connectionDurationSeconds = _uiState.value.connectionDurationSeconds + 1
                )
            }
        }
    }

    private fun stopConnectionTimer() {
        connectionTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(connectionDurationSeconds = 0L)
    }

    private fun startHeartbeatLoop() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(10 * 60 * 1000) // 10 minutes
                val result = repository.checkHeartbeat()
                result.getOrNull()?.let { hb ->
                    if (!hb.isValid) {
                        logout()
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "اعتبار اشتراک شما به پایان رسیده است."
                        )
                    } else {
                        hb.remainingDays?.let { days ->
                            _uiState.value = _uiState.value.copy(remainingDays = days)
                        }
                    }
                }
            }
        }
    }

    fun setServersBottomSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isServersBottomSheetVisible = visible)
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsDialogVisible = visible)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun logout() {
        heartbeatJob?.cancel()
        stopConnectionTimer()
        repository.prefs.clearSession()
        _uiState.value = UiState(
            currentScreen = ScreenState.LicenseEntry,
            hwid = repository.hwid
        )
    }
}
