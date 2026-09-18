package com.mashvpn.android.data.models

import com.google.gson.annotations.SerializedName

data class VpnNode(
    @SerializedName("id") val id: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("country_code") val countryCode: String = "NL",
    @SerializedName("country_name") val countryName: String = "هلند",
    @SerializedName("flag_emoji") val flagEmoji: String = "🇳🇱",
    @SerializedName("remote_host") val remoteHost: String,
    @SerializedName("remote_port") val remotePort: Int = 1194,
    @SerializedName("protocol") val protocol: String = "udp",
    @SerializedName("ovpn_raw_config") val ovpnRawConfig: String?,
    @SerializedName("ovpn_config") val ovpnConfig: String?,
    @SerializedName("auth_username") val authUsername: String?,
    @SerializedName("auth_password") val authPassword: String?,
    @SerializedName("order_index") val orderIndex: Int = 0,
    
    // UI Transient fields
    var pingMs: Long = -1L,
    var isTestingPing: Boolean = false
) {
    val cleanTitle: String
        get() = displayName?.takeIf { it.isNotBlank() } ?: name ?: "سرور مش وی پی ان"

    val rawConfigString: String
        get() = ovpnRawConfig?.takeIf { it.isNotBlank() } ?: ovpnConfig ?: ""
}
