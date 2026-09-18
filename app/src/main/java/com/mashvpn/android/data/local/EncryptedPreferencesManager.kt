package com.mashvpn.android.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = try {
            EncryptedSharedPreferences.create(
                context,
                "mashvpn_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for edge cases
            context.getSharedPreferences("mashvpn_secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    var licenseKey: String?
        get() = prefs.getString("license_key", null)
        set(value) = prefs.edit().putString("license_key", value).apply()

    var customerName: String?
        get() = prefs.getString("customer_name", null)
        set(value) = prefs.edit().putString("customer_name", value).apply()

    var remainingDays: Int
        get() = prefs.getInt("remaining_days", 0)
        set(value) = prefs.edit().putInt("remaining_days", value).apply()

    var expiresAt: String?
        get() = prefs.getString("expires_at", null)
        set(value) = prefs.edit().putString("expires_at", value).apply()

    var maxDevices: Int
        get() = prefs.getInt("max_devices", 1)
        set(value) = prefs.edit().putInt("max_devices", value).apply()

    var lastSelectedNodeId: String?
        get() = prefs.getString("last_selected_node_id", null)
        set(value) = prefs.edit().putString("last_selected_node_id", value).apply()

    val isLoggedIn: Boolean
        get() = !licenseKey.isNullOrBlank()

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
