package com.mashvpn.android.data.models

import com.google.gson.annotations.SerializedName

data class LicenseVerifyRequest(
    @SerializedName("p_license_key") val licenseKey: String,
    @SerializedName("p_hwid") val hwid: String,
    @SerializedName("p_machine_name") val machineName: String
)

data class LicenseVerifyResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("remaining_days") val remainingDays: Int? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("customer_name") val customerName: String? = null,
    @SerializedName("max_devices") val maxDevices: Int? = null,
    @SerializedName("current_devices") val currentDevices: Int? = null
)

data class GetNodesRequest(
    @SerializedName("p_license_key") val licenseKey: String,
    @SerializedName("p_hwid") val hwid: String
)

data class HeartbeatRequest(
    @SerializedName("p_license_key") val licenseKey: String,
    @SerializedName("p_hwid") val hwid: String
)

data class HeartbeatResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("is_valid") val isValid: Boolean = false,
    @SerializedName("remaining_days") val remainingDays: Int? = null,
    @SerializedName("expires_at") val expiresAt: String? = null
)
