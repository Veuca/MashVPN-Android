package com.mashvpn.android.vpn

sealed class VpnState {
    object Disconnected : VpnState()
    data class Connecting(val serverName: String) : VpnState()
    data class Connected(val serverName: String, val connectedAtMillis: Long = System.currentTimeMillis()) : VpnState()
    data class Error(val message: String) : VpnState()
}
