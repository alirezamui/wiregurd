package com.example.vpn

import android.content.Context
import android.content.Intent
import com.example.data.VpnProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class NetworkDiagnosticInfo(
    val publicIp: String = "Not Connected",
    val localIp: String = "N/A",
    val endpoint: String = "N/A",
    val pingMs: String = "--"
)

object VpnManager {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _networkInfo = MutableStateFlow(NetworkDiagnosticInfo())
    val networkInfo = _networkInfo.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    var activeProfile: VpnProfile? = null
        private set

    fun updateState(state: ConnectionState) {
        _connectionState.value = state
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.ERROR) {
            _networkInfo.value = NetworkDiagnosticInfo()
        }
    }

    fun updateNetworkInfo(info: NetworkDiagnosticInfo) {
        _networkInfo.value = info
    }

    fun setError(error: String?) {
        _errorFlow.value = error
        if (error != null) {
            _connectionState.value = ConnectionState.ERROR
        }
    }

    fun startVpn(context: Context, profile: VpnProfile) {
        activeProfile = profile
        setError(null)
        updateState(ConnectionState.CONNECTING)
        
        val intent = Intent(context, WireGuardVpnService::class.java).apply {
            action = WireGuardVpnService.ACTION_CONNECT
            putExtra(WireGuardVpnService.EXTRA_PROFILE_ID, profile.id)
        }
        context.startService(intent)
    }

    fun stopVpn(context: Context) {
        val intent = Intent(context, WireGuardVpnService::class.java).apply {
            action = WireGuardVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }
}
