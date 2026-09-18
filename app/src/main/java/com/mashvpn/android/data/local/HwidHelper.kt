package com.mashvpn.android.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

object HwidHelper {

    @SuppressLint("HardwareIds")
    fun getHwid(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "UNKNOWN_ANDROID_ID"
            
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(androidId.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "MASHVPN_" + Build.FINGERPRINT.hashCode().toString()
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }
}
