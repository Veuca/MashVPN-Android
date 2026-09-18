package com.mashvpn.android.data.repository

import android.content.Context
import com.mashvpn.android.data.local.EncryptedPreferencesManager
import com.mashvpn.android.data.local.HwidHelper
import com.mashvpn.android.data.models.*
import com.mashvpn.android.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VpnRepository(private val context: Context) {

    private val api = RetrofitClient.apiService
    val prefs = EncryptedPreferencesManager(context)

    val hwid: String
        get() = HwidHelper.getHwid(context)

    val deviceName: String
        get() = HwidHelper.getDeviceName()

    suspend fun verifyAndActivateLicense(key: String): Result<LicenseVerifyResponse> = withContext(Dispatchers.IO) {
        try {
            val cleanKey = key.trim().uppercase()
            val req = LicenseVerifyRequest(
                licenseKey = cleanKey,
                hwid = hwid,
                machineName = deviceName
            )
            val response = api.verifyLicense(req)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    prefs.licenseKey = cleanKey
                    prefs.customerName = res.customerName
                    prefs.remainingDays = res.remainingDays ?: 0
                    prefs.expiresAt = res.expiresAt
                    prefs.maxDevices = res.maxDevices ?: 1
                    Result.success(res)
                } else {
                    val errMsg = mapErrorCode(res.errorCode, res.message)
                    Result.failure(Exception(errMsg))
                }
            } else {
                Result.failure(Exception("خطا در برقراری ارتباط با سرور (کد ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطای اتصال به اینترنت: ${e.localizedMessage}"))
        }
    }

    suspend fun fetchActiveNodes(): Result<List<VpnNode>> = withContext(Dispatchers.IO) {
        try {
            val key = prefs.licenseKey ?: return@withContext Result.failure(Exception("لایسنس وارد نشده است"))
            val req = GetNodesRequest(licenseKey = key, hwid = hwid)
            val response = api.getActiveNodes(req)
            if (response.isSuccessful && response.body() != null) {
                val nodes = response.body()!!.sortedBy { it.orderIndex }
                Result.success(nodes)
            } else {
                Result.failure(Exception("خطا در دریافت لیست سرورها"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkHeartbeat(): Result<HeartbeatResponse> = withContext(Dispatchers.IO) {
        try {
            val key = prefs.licenseKey ?: return@withContext Result.failure(Exception("لایسنس موجود نیست"))
            val req = HeartbeatRequest(licenseKey = key, hwid = hwid)
            val response = api.sendHeartbeat(req)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.isValid) {
                    res.remainingDays?.let { prefs.remainingDays = it }
                    res.expiresAt?.let { prefs.expiresAt = it }
                }
                Result.success(res)
            } else {
                Result.failure(Exception("خطای پالس لایسنس"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapErrorCode(code: String?, defaultMsg: String?): String {
        return when (code) {
            "LICENSE_NOT_FOUND" -> "کد لایسنس وارد شده در سیستم یافت نشد."
            "LICENSE_EXPIRED" -> "اعتبار زمانی این لایسنس به پایان رسیده است."
            "DEVICE_LIMIT_REACHED" -> "ظرفیت تعداد دستگاه‌های مجاز برای این لایسنس پر شده است."
            "LICENSE_INACTIVE" -> "این لایسنس توسط مدیریت غیرفعال شده است."
            else -> defaultMsg ?: "خطای ناشناخته در بررسی لایسنس"
        }
    }
}
